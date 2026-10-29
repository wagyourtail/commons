package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.translateEscapes

class Json5StringConstant : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<Json5StringConstant>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek()
            return c == '"' || c == '\''
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val quote = reader.peek()
            if (quote == '\"' || quote == '\'') {
                append(
                    reader.takeString(
                        leinient = true,
                        escapeNewline = true,
                        noTranslateEscapes = true,
                        quote = quote.toString()
                    )
                )
                return
            }
            throw reader.createException("Expected string start char but got: $quote")
        }

        override fun unchecked(value: String): Json5StringConstant = Json5StringConstant(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5StringConstant =
            Json5StringConstant(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5StringConstant = read(rawContent)

    }

    val value: String
        get() {
            val content = rawContent
            return content.substring(1, content.length - 1).replace("\\\n", "").translateEscapes()
        }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
