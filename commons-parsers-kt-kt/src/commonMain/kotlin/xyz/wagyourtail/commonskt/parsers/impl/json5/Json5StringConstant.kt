package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.translateEscapes

class Json5StringConstant(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<String>>(rawContent) {

    constructor(content: SingleContent<String>) : this(content.toString())

    companion object : StringDataBuilder<Json5StringConstant, Data.SingleContent<String>> {

        override fun invoke(rawContent: CharReader<*>): Json5StringConstant {
            return Json5StringConstant(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
            val quote = reader.peek()
            if (quote == '\"' || quote == '\'') {
                return SingleContent(
                    reader.takeString(
                        leinient = true,
                        escapeNewline = true,
                        noTranslateEscapes = true,
                        quote = quote.toString()
                    )
                )
            }
            throw reader.createException("Expected string start char but got: $quote")
        }

    }

    val value: String
        get() {
            val content = rawContent
            return content.substring(1, content.length - 1).replace("\\\n", "").translateEscapes()
        }

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
        return Companion.checkedBuildContent(reader)
    }

}