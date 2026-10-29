package xyz.wagyourtail.commonskt.data

import xyz.wagyourtail.commonskt.utils.iterable
import xyz.wagyourtail.commonskt.utils.withDelimiter

/**
 * One node of structured data: a lazily-derived pair of canonical
 * [rawContent] and [content] (child entries).
 *
 * This is a contract, not a storage policy — three storage flavors exist:
 * - **raw-backed** ([RawStringData]/[RawBinaryData]) — wraps a known-raw
 *   token or view; [content] parses lazily, once, and is cached.
 * - **view** — the same classes via their `(buffer, start, end)` protected
 *   constructor: a zero-copy range into a shared backing buffer.
 * - **synthetic** ([ParsedStringData]/[ParsedBinaryData]) — built from
 *   children; [rawContent] materializes on serialize.
 *
 * Atomic leaf types that cannot carry the lazy machinery (e.g. Kotlin
 * `@JvmInline` value classes) may implement this interface directly and
 * source their parsed parts from [ParseCache].
 */
interface StructuredData<R : Any> {

    /**
     * canonical serialized form (`String` for text, `ByteArray` for binary);
     * materialized lazily for view/synthetic nodes.
     */
    val rawContent: R

    /**
     * child entries — sub-nodes, raw slices, or literals — in order.
     * Derived once per node and cached.
     */
    val content: Content

    /**
     * walk this node and its children with [visitor]; a `visit` returning
     * `false` skips that node's children.
     */
    fun accept(visitor: DataVisitor) {
        if (visitor.visit(this)) {
            for (entry in content.entries) {
                if (entry is StructuredData<*>) {
                    entry.accept(visitor)
                } else {
                    visitor.visit(entry)
                }
            }
        }
        visitor.visitEnd(this)
    }

}

/**
 * The ordered children of a [StructuredData] node. Entries are arbitrary
 * values: child nodes, raw slices, or literals (the concrete grammar decides).
 */
abstract class Content {

    abstract val entries: Iterable<Any>

    override fun toString(): String {
        return entries.joinToString("")
    }

    companion object {

        /**
         * shared empty content for leaf nodes with no children.
         */
        val EMPTY: Content = EmptyContent

    }

}

/**
 * [Content] for a leaf node with no children.
 */
object EmptyContent : Content() {

    override val entries: Iterable<Any>
        get() = emptyList()

    override fun toString(): String = ""

}

/**
 * [Content] holding a single entry.
 */
open class SingleContent(val value: Any) : Content() {

    override val entries: Iterable<Any>
        get() = iterable { yield(value) }

}

/**
 * [Content] holding a list of entries.
 */
open class ListContent(override val entries: List<Any>) : Content()

/**
 * [Content] that interleaves [delimiter] between each of [content].
 */
open class ListContentWithDelimiter(
    val content: List<Any>,
    val delimiter: Any
) : Content() {

    override val entries: Iterable<Any>
        get() = content.withDelimiter(delimiter)

    val entriesWithoutDelimiters: List<Any>
        get() = content

}

interface DataVisitor {

    /**
     * called for each node (before its children) and each non-node entry.
     * Return `false` to skip a node's children.
     */
    fun visit(o: Any?): Boolean

    /**
     * called after a node's children have been visited.
     */
    fun visitEnd(o: Any?) {}

}
