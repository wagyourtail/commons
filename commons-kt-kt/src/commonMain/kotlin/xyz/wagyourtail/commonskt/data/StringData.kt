package xyz.wagyourtail.commonskt.data

import xyz.wagyourtail.commonskt.properties.internallyNullable
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.reader.StringCharReader

/**
 * [StructuredData] over text. Storage is one of:
 * - a known canonical [rawContent] string,
 * - a `(buffer, start, end)` view into a shared backing buffer (zero-copy),
 * - a [Content] tree (synthetic; raw materializes via [BuildStringVisitor]).
 *
 * The view constructor is how the companion's `uncheckedView` produces
 * range-backed nodes of a concrete type — parse-once laziness and
 * `rawRangeReader` handle the rest.
 */
abstract class StringData : StructuredData<String> {

    private var viewBuffer: CharSequence? = null
    private var viewStart = 0
    private var viewEnd = 0

    protected constructor(rawContent: String) {
        this.rawContent = rawContent
    }

    /**
     * view-backed node over `(buffer, start, end)` — no slicing or reparsing
     * until [rawContent]/[content] are actually read.
     */
    protected constructor(viewBuffer: CharSequence, start: Int, end: Int) {
        require(start >= 0 && start <= end && end <= viewBuffer.length) {
            "invalid view range $start..$end for buffer of length ${viewBuffer.length}"
        }
        this.viewBuffer = viewBuffer
        this.viewStart = start
        this.viewEnd = end
    }

    protected constructor(content: Content) {
        this.content = content
    }

    final override var rawContent: String by internallyNullable<String>(null) { it ?: buildRawContent() }
        private set

    final override var content: Content by internallyNullable<Content>(null) { it ?: buildContent() }
        private set

    /**
     * materialize this node's canonical raw — the view range slice for
     * view-backed nodes, or a serialize of the content tree otherwise.
     */
    protected open fun buildRawContent(): String {
        val buffer = viewBuffer
        return if (buffer != null) {
            buffer.subSequence(viewStart, viewEnd).toString()
        } else {
            BuildStringVisitor.apply(this)
        }
    }

    protected abstract fun buildContent(): Content

    /**
     * a reader over exactly this node's raw range — zero-copy for views
     * (`StringCharReader` over `(buffer, start, end)`), whole string otherwise.
     */
    protected fun rawRangeReader(): CharReader<*> {
        val buffer = viewBuffer
        return if (buffer != null) {
            StringCharReader(buffer, viewStart, viewEnd)
        } else {
            CharReader(rawContent)
        }
    }

    override fun toString(): String = rawContent

    override fun equals(other: Any?): Boolean =
        other is StringData && rawContent == other.rawContent

    override fun hashCode(): Int = rawContent.hashCode()

}

/**
 * A node whose raw is known up front — either a canonical string or a
 * `(buffer, start, end)` view. [buildContent] parses the raw range lazily
 * via [checkedBuildContent] and validates full consumption.
 */
abstract class RawStringData : StringData {

    protected constructor(rawContent: String) : super(rawContent)

    protected constructor(viewBuffer: CharSequence, start: Int, end: Int) :
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
    protected abstract fun checkedBuildContent(reader: CharReader<*>): Content

}

/**
 * A synthetic node built from a [Content] tree; [rawContent] serializes
 * lazily via [BuildStringVisitor].
 */
abstract class ParsedStringData(content: Content) : StringData(content) {

    final override fun buildContent(): Content =
        throw IllegalStateException("content is always present")

}

/**
 * Rebuilds a node's canonical text by walking its entries via [accept]:
 * every non-node entry is appended with `toString`.
 */
class BuildStringVisitor : DataVisitor {

    private val sb = StringBuilder()

    override fun visit(o: Any?): Boolean {
        if (o !is StructuredData<*>) {
            sb.append(o)
        }
        return true
    }

    fun build(): String = sb.toString()

    companion object {

        fun apply(data: StructuredData<*>): String {
            val visitor = BuildStringVisitor()
            data.accept(visitor)
            return visitor.build()
        }

    }

}
