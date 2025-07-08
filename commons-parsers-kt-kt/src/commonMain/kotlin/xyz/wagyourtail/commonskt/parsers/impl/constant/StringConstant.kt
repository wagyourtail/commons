package xyz.wagyourtail.commonskt.parsers.impl.constant

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.translateEscapes

class StringConstant(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<String>>(rawContent) {

    constructor(content: SingleContent<String>) : this(content.toString())

    companion object : StringDataBuilder<StringConstant, Data.SingleContent<String>> {

        override fun invoke(rawContent: CharReader<*>): StringConstant {
            return StringConstant(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
            return SingleContent(reader.takeString(noTranslateEscapes = true, quote = "\""))
        }

    }

    val value: String
        get() = rawContent.substring(1, rawContent.length - 1).translateEscapes()

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
        return Companion.checkedBuildContent(reader)
    }


}