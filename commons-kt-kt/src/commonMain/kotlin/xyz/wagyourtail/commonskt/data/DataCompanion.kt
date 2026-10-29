package xyz.wagyourtail.commonskt.data

import xyz.wagyourtail.commonskt.reader.BackedByteReader
import xyz.wagyourtail.commonskt.reader.BackedCharReader
import xyz.wagyourtail.commonskt.reader.ByteArrayByteReader
import xyz.wagyourtail.commonskt.reader.ByteReader
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.writer.ByteArrayByteWriter

/**
 * The grammar/constructor half of a [StructuredData] type — UML's
 * `TypeCompanion` shape generalized over raw type, buffer type, and reader.
 *
 * - [R] canonical raw type (`String` | `ByteArray`)
 * - [B] backing buffer type for view construction (`CharSequence` | `ByteArray`)
 * - [T] the node type
 * - [RD] the reader type (`CharReader<*>` | `ByteReader<*>`)
 *
 * The two `read` overloads form the contract: `read(reader, append)` is the
 * grammar — it consumes one value and emits every piece (literals and child
 * nodes) in order; `read(reader)` derives node construction from it,
 * producing a zero-copy view node when the reader is buffer-backed.
 */
interface DataCompanion<R : Any, B : Any, T : StructuredData<R>, in RD> {

    /**
     * grammar lookahead — `true` if the reader is positioned at a value of
     * this type. Must not consume.
     */
    fun shouldRead(reader: RD): Boolean

    /**
     * read one value from [reader], emitting every consumed piece —
     * literals and child nodes — to [append] in order.
     * Throws [xyz.wagyourtail.commonskt.reader.ParseException] on bad input.
     */
    fun read(reader: RD, append: (Any) -> Unit)

    /**
     * read one value → a node. Emits a zero-copy range view via
     * [uncheckedView] when the reader is buffer-backed; otherwise rebuilds
     * the canonical raw from the emitted pieces → [unchecked].
     */
    fun read(reader: RD): T

    /**
     * wrap raw without validation — [value] must be canonical.
     */
    fun unchecked(value: R): T

    /**
     * wrap a range of a shared buffer — default materializes the slice into
     * [unchecked]; types with view-capable node ctors override to produce
     * real zero-copy nodes.
     */
    fun uncheckedView(buffer: B, start: Int, end: Int): T

    /**
     * parse-validate a whole input → a node; trailing input is an error.
     */
    fun checked(value: R): T

    /**
     * replay the grammar over [reader], collecting emitted pieces as
     * [Content] — expects the reader to be exhausted afterwards (used by
     * view-backed nodes reparsing their own range).
     */
    fun buildContent(reader: RD): Content

}

/**
 * [DataCompanion] for text nodes: `R=String`, `B=CharSequence`,
 * `RD=CharReader<*>`.
 */
abstract class StringDataCompanion<T : StructuredData<String>> :
    DataCompanion<String, CharSequence, T, CharReader<*>> {

    override fun read(reader: CharReader<*>): T {
        val backed = reader as? BackedCharReader
        return if (backed != null) {
            val start = backed.backingPosition
            read(reader) {}
            uncheckedView(backed.backingBuffer, start, backed.backingPosition)
        } else {
            unchecked(buildString { read(reader) { append(it.toString()) } })
        }
    }

    override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): T =
        unchecked(buffer.subSequence(start, end).toString())

    override fun checked(value: String): T {
        val reader = CharReader(value)
        val data = read(reader)
        reader.expectEOS()
        return data
    }

    override fun buildContent(reader: CharReader<*>): Content {
        val entries = ArrayList<Any>()
        read(reader) { entries.add(it) }
        reader.expectEOS()
        return ListContent(entries)
    }

    /**
     * [ParseCache]-backed derived-parts memoization keyed `(this, raw)` —
     * the atomic-leaf story for types that cannot store per-instance state.
     */
    fun <V : Any> cached(raw: String, compute: (String) -> V): V =
        ParseCache.getOrPut(this, raw) { compute(raw) }

}

/**
 * [DataCompanion] for binary nodes: `R=ByteArray`, `B=ByteArray`,
 * `RD=ByteReader<*>`.
 */
abstract class BinaryDataCompanion<T : StructuredData<ByteArray>> :
    DataCompanion<ByteArray, ByteArray, T, ByteReader<*>> {

    override fun read(reader: ByteReader<*>): T {
        val backed = reader as? BackedByteReader
        return if (backed != null) {
            val start = backed.backingPosition
            read(reader) {}
            uncheckedView(backed.backingBuffer, start, backed.backingPosition)
        } else {
            val out = ByteArrayByteWriter()
            read(reader) { writeRawPiece(out, it) }
            unchecked(out.toByteArray())
        }
    }

    override fun uncheckedView(buffer: ByteArray, start: Int, end: Int): T =
        unchecked(buffer.copyOfRange(start, end))

    override fun checked(value: ByteArray): T {
        val reader = ByteReader(value)
        val data = read(reader)
        reader.expectEOS()
        return data
    }

    override fun buildContent(reader: ByteReader<*>): Content {
        val entries = ArrayList<Any>()
        read(reader) { entries.add(it) }
        reader.expectEOS()
        return ListContent(entries)
    }

    /**
     * [ParseCache]-backed derived-parts memoization keyed `(this, ContentKey(raw))`.
     * The array must not be mutated while used as a cache key.
     */
    fun <V : Any> cached(raw: ByteArray, compute: (ByteArray) -> V): V =
        ParseCache.getOrPut(this, ContentKey(raw)) { compute(raw) }

    /**
     * value-equality wrapper so a `ByteArray` can key [ParseCache].
     * Holds the array by reference — do not mutate it afterwards.
     */
    class ContentKey(val bytes: ByteArray) {
        override fun equals(other: Any?): Boolean =
            other is ContentKey && bytes.contentEquals(other.bytes)

        override fun hashCode(): Int = bytes.contentHashCode()
    }

}
