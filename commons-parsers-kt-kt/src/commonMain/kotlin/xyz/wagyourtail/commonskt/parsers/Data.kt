package xyz.wagyourtail.commonskt.parsers

import xyz.wagyourtail.commonskt.properties.InternallyNullable

abstract class Data<T, E: Data.Content> protected constructor(
    rawContent: T?,
    content: E?
) {

    open val rawContent: T by InternallyNullable(
        rawContent,
        get = { field ->
            field ?: buildRawContent()
        }
    )

    open val content: E by InternallyNullable(
        content,
        get = { field ->
            field ?: buildContent()
        }
    )

    constructor(rawContent: T) : this(rawContent, null)

    constructor(content: E) : this(null, content)

    protected abstract fun buildRawContent(): T

    protected abstract fun buildContent(): E

    override fun toString(): String {
        return content.toString()
    }

    fun accept(visitor: (Any?) -> Boolean) {
        if (visitor(this)) {
            for (entry in content.entries) {
                if (entry is Data<*, *>) {
                    entry.accept(visitor)
                } else {
                    visitor(entry)
                }
            }
        }
    }

    abstract class OnlyRaw<T, E: Content>(rawContent: T) : Data<T, E>(rawContent) {

        override val content: E
            get() = buildContent()

        override fun buildRawContent(): T {
            throw IllegalStateException("raw content should always be present")
        }

    }

    abstract class OnlyParsed<T, E: Content>(content: E) : Data<T, E>(content) {

        override val rawContent: T
            get() = buildRawContent()

        override fun buildContent(): E {
            throw IllegalStateException("content should always be present")
        }

    }

    abstract class Content {

        abstract val entries: Iterable<*>

        override fun toString(): String {
            return entries.joinToString("")
        }

    }

    open class SingleContent<T>(val value: T): Content() {

        override val entries: Iterable<*>
            get() = listOf(value)

    }

    open class ListContent(override val entries: List<*>): Content()

}