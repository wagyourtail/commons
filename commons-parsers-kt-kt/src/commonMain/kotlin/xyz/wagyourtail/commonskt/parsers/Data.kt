package xyz.wagyourtail.commonskt.parsers

import xyz.wagyourtail.commonskt.properties.internallyNullable
import xyz.wagyourtail.commonskt.utils.iterable
import xyz.wagyourtail.commonskt.utils.withDelimiter

abstract class Data<T: Any, E : Data.Content<*>> protected constructor(
    rawContent: T?,
    content: E?
) {

    open val rawContent: T by internallyNullable(rawContent) { field ->
        field ?: buildRawContent()
    }

    open val content: E by internallyNullable(content) { field ->
        field ?: buildContent()
    }

    constructor(rawContent: T) : this(rawContent, null)

    constructor(content: E) : this(null, content)

    protected abstract fun buildRawContent(): T

    protected abstract fun buildContent(): E

    override fun toString(): String {
        return content.toString()
    }

    fun accept(visitor: DataVisitor) {
        if (visitor.visit(this)) {
            for (entry in content.entries) {
                if (entry is Data<*, *>) {
                    entry.accept(visitor)
                } else {
                    visitor.visit(entry)
                }
            }
        }
        visitor.visitEnd(this)
    }

    abstract class OnlyRaw<T: Any, E : Content<*>>(rawContent: T) : Data<T, E>(rawContent) {

        override val content: E
            get() = buildContent()

        override fun buildRawContent(): T {
            throw IllegalStateException("raw content should always be present")
        }

    }

    abstract class OnlyParsed<T: Any, E : Content<*>>(content: E) : Data<T, E>(content) {

        override val rawContent: T
            get() = buildRawContent()

        override fun buildContent(): E {
            throw IllegalStateException("content should always be present")
        }

    }

    abstract class Content<T> {

        abstract val entries: Iterable<T>

        override fun toString(): String {
            return entries.joinToString("")
        }

    }

    open class SingleContent<T>(val value: T) : Content<T>() {

        override val entries: Iterable<T>
            get() = iterable { yield(value) }

    }

    open class ListContent<T>(override val entries: List<T>) : Content<T>()

    open class ListContentWithDelimiter<T>(
        val content: List<T>,
        val delimiter: Any
    ) : Content<Any>() {


        override val entries: Iterable<Any>
            get() = (content as List<Any>).withDelimiter(delimiter)

    }

    interface DataVisitor {

        fun visit(o: Any?): Boolean
        fun visitEnd(o: Any?) {}

    }

}