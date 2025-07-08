package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader

class WholePart(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<String>>(rawContent) {

    constructor(content: SingleContent<String>) : this(content.toString())

    companion object : StringDataBuilder<WholePart, SingleContent<String>> {

        override fun invoke(rawContent: CharReader<*>): WholePart {
            return WholePart(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
            val first = reader.peek()
            if (first == '0') {
                throw reader.createException("Whole number cannot start with 0");
            }
            if (first != null && first.isDigit()) {
                return SingleContent(reader.takeWhile { it.isDigit() })
            }

            throw reader.createException("Not a whole number: $first")
        }

    }

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
        return Companion.checkedBuildContent(reader)
    }

}