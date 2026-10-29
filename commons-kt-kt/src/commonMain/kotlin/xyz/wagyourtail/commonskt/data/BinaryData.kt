package xyz.wagyourtail.commonskt.data

import xyz.wagyourtail.commonskt.properties.internallyNullable
import xyz.wagyourtail.commonskt.reader.ByteArrayByteReader
import xyz.wagyourtail.commonskt.reader.ByteReader
import xyz.wagyourtail.commonskt.writer.ByteArrayByteWriter
import xyz.wagyourtail.commonskt.writer.ByteWriter

/**
 * [StructuredData] over bytes — the binary mirror of [StringData].
 * Storage is one of:
 * - a known canonical [rawContent] array,
 * - a `(buffer, start, end)` view into a shared backing array (zero-copy),
 * - a [Content] tree (synthetic; raw materializes via [BuildBytesVisitor]).
 */
abstract class BinaryData : StructuredData<ByteArray> {

    private var viewBuffer: ByteArray? = null
    private var viewStart = 0
    private var viewEnd = 0

    protected constructor(rawContent: ByteArray) {
        this.rawContent = rawContent
    }

    /**
     * view-backed node over `(buffer, start, end)` — no slicing or reparsing
     * until [rawContent]/[content] are actually read.
     */
    protected constructor(viewBuffer: ByteArray, start: Int, end: Int) {
        require(start >= 0 && start <= end && end <= viewBuffer.size) {
            "invalid view range $start..$end for buffer of size ${viewBuffer.size}"
        }
        this.viewBuffer = viewBuffer
        this.viewStart = start
        this.viewEnd = end
    }

    protected constructor(content: Content) {
        this.content = content
    }

    final override var rawContent: ByteArray by internallyNullable<ByteArray>(null) { it ?: buildRawContent() }
        private set

    final override var content: Content by internallyNullable<Content>(null) { it ?: buildContent() }
        private set

    /**
     * materialize this node's canonical bytes — the view range slice for
     * view-backed nodes, or a serialize of the content tree otherwise.
     */
    protected open fun buildRawContent(): ByteArray {
        val buffer = viewBuffer
        return if (buffer != null) {
            buffer.copyOfRange(viewStart, viewEnd)
        } else {
            BuildBytesVisitor.apply(this)
        }
    }

    protected abstract fun buildContent(): Content

    /**
     * a reader over exactly this node's raw range — zero-copy for views,
     * whole array otherwise.
     */
    protected fun rawRangeReader(): ByteReader<*> {
        val buffer = viewBuffer
        return if (buffer != null) {
            ByteArrayByteReader(buffer, viewStart, viewEnd)
        } else {
            ByteReader(rawContent)
        }
    }

    /**
     * write this node's canonical bytes to [writer] — for view-backed nodes
     * this streams the range without materializing a slice.
     */
    open fun writeBytesTo(writer: ByteWriter) {
        val buffer = viewBuffer
        if (buffer != null) {
            writer.put(buffer, viewStart, viewEnd - viewStart)
        } else {
            writer.put(rawContent)
        }
    }

    override fun toString(): String {
        val raw = rawContent
        val n = minOf(raw.size, 16)
        val hex = buildString(n * 2 + 24) {
            append("0x")
            for (i in 0 until n) {
                append(HEX[(raw[i].toInt() ushr 4) and 0xF])
                append(HEX[raw[i].toInt() and 0xF])
            }
            if (raw.size > n) append("…")
        }
        return "$hex (${raw.size} bytes)"
    }

    override fun equals(other: Any?): Boolean =
        other is BinaryData && rawContent.contentEquals(other.rawContent)

    override fun hashCode(): Int = rawContent.contentHashCode()

    private companion object {
        const val HEX = "0123456789ABCDEF"
    }

}

/**
 * A node whose raw is known up front — either a canonical array or a
 * `(buffer, start, end)` view. [buildContent] parses the raw range lazily
 * via [checkedBuildContent] and validates full consumption.
 */
abstract class RawBinaryData : BinaryData {

    protected constructor(rawContent: ByteArray) : super(rawContent)

    protected constructor(viewBuffer: ByteArray, start: Int, end: Int) :
        super(viewBuffer, start, end)

    protected constructor(content: Content) : super(content)

    final override fun buildContent(): Content {
        val reader = rawRangeReader()
        val content = checkedBuildContent(reader)
        reader.expectEOS()
        return content
    }

    /**
     * parse this node's content from a reader positioned at (and bounded by)
     * the raw range.
     */
    protected abstract fun checkedBuildContent(reader: ByteReader<*>): Content

}

/**
 * A synthetic node built from a [Content] tree; [rawContent] serializes
 * lazily via [BuildBytesVisitor].
 */
abstract class ParsedBinaryData(content: Content) : BinaryData(content) {

    final override fun buildContent(): Content =
        throw IllegalStateException("content is always present")

}

/**
 * serialize one emitted piece to bytes — the shared emission contract of
 * [BuildBytesVisitor] and canonical reconstruction in
 * [BinaryDataCompanion.read]: `ByteArray` → raw bytes, `Byte`/`Int` in
 * `0..255` → one byte, `String` → UTF-8, [BinaryData] → `writeBytesTo`.
 */
fun writeRawPiece(writer: ByteWriter, piece: Any?) {
    when (piece) {
        null -> {}
        is BinaryData -> piece.writeBytesTo(writer)
        is ByteArray -> writer.put(piece)
        is Byte -> writer.put(piece.toInt() and 0xFF)
        is Int -> {
            require(piece in 0..255) { "int piece $piece out of single-byte range" }
            writer.put(piece)
        }
        is String -> writer.putUtf8(piece)
        else -> throw IllegalArgumentException(
            "cannot serialize entry of ${piece::class} to bytes: $piece"
        )
    }
}

/**
 * Rebuilds a node's canonical bytes by walking its entries via [accept]:
 * each non-node entry is emitted per [writeRawPiece].
 */
class BuildBytesVisitor(val writer: ByteWriter = ByteArrayByteWriter()) : DataVisitor {

    override fun visit(o: Any?): Boolean {
        if (o !is StructuredData<*>) {
            writeRawPiece(writer, o)
        }
        return true
    }

    fun build(): ByteArray {
        val w = writer
        require(w is ByteArrayByteWriter) { "build() requires a ByteArrayByteWriter" }
        return w.toByteArray()
    }

    companion object {

        fun apply(data: StructuredData<*>): ByteArray {
            val visitor = BuildBytesVisitor()
            data.accept(visitor)
            return visitor.build()
        }

    }

}
