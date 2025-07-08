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

    abstract class OnlyRaw<E: Content>(rawContent: String) : StringData<E>(rawContent) {

        override val content: E
            get() = buildContent()

        override fun buildContent(): E {
            val reader = CharReader(rawContent)
            val content = checkedBuildContent(reader)
            reader.expectEOS()
            return content
        }

        abstract fun checkedBuildContent(reader: CharReader<*>): E

        override fun buildRawContent(): String {
            throw IllegalStateException("raw content should always be present")
        }

    }

    abstract class OnlyParsed<E: Content>(content: E) : StringData<E>(content) {

        override fun buildContent(): E {
            throw IllegalStateException("content should always be present")
        }

    }

    interface StringDataBuilder<T: StringData<E>, E: Data.Content> {

        operator fun invoke(rawContent: CharReader<*>): T

        fun checked(rawContent: String): T {
            val reader = CharReader(rawContent)
            val data = invoke(reader)
            reader.expectEOS()
            return data
        }

        fun checkedBuildContent(reader: CharReader<*>): E

    }

    class BuildStringVisitor : DataVisitor {
        private val sb = StringBuilder()

        override fun visit(o: Any?): Boolean {
            if (o !is Data<*, *>) {
                sb.append(o)
            }
            return true
        }

        fun build(): String {
            return sb.toString()
        }

        companion object {
            fun apply(data: Data<*, *>): String {
                val visitor = BuildStringVisitor()
                data.accept(visitor)
                return visitor.build()
            }
        }
    }

}