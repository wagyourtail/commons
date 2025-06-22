package xyz.wagyourtail.commonskt.parsers

import xyz.wagyourtail.commonskt.reader.CharReader

abstract class StringData<E: Data.Content> protected constructor(rawContent: String?, content: E?) : Data<String, E>(rawContent, content) {

    constructor(rawContent: String) : this(rawContent, null)

    constructor(content: E) : this(null, content)

    override fun buildRawContent(): String {
        return content.toString()
    }

    override fun toString(): String {
        return rawContent
    }

    abstract class OnlyRaw<E: Content>(rawContent: String, val contentBuilder: (CharReader<*>) -> E) : StringData<E>(rawContent) {

        constructor(reader: CharReader<*>, contentBuilder: (CharReader<*>) -> E) : this(contentBuilder(reader).toString(), contentBuilder)

        override val content: E
            get() = buildContent()

        override fun buildContent(): E {
            val reader = CharReader(rawContent)
            val content = contentBuilder(reader)
            reader.expectEOS()
            return content
        }

        override fun buildRawContent(): String {
            throw IllegalStateException("raw content should always be present")
        }

    }

    abstract class OnlyParsed<E: Content>(content: E) : StringData<E>(content) {

        override fun buildContent(): E {
            throw IllegalStateException("content should always be present")
        }

    }

}