package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader

class Json5Whitespace(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<String>>(rawContent) {

    constructor(content: SingleContent<String>) : this(content.toString())

    companion object : StringDataBuilder<Json5Whitespace, SingleContent<String>> {

        override fun invoke(rawContent: CharReader<*>): Json5Whitespace {
            return Json5Whitespace(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
            val first = reader.peek()
            if (first == null || !first.isWhitespace()) {
                throw reader.createException("Expected whitespace but got '$first'")
            }
            return SingleContent(reader.takeWhile { it.isWhitespace() })
        }

    }

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
        return Companion.checkedBuildContent(reader)
    }

}