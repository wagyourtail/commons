package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader

class DecimalPart(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<String>>(rawContent) {

    constructor(content: SingleContent<String>) : this(content.toString())

    companion object : StringDataBuilder<DecimalPart, SingleContent<String>> {

        override fun invoke(rawContent: CharReader<*>): DecimalPart {
            return DecimalPart(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
            val first = reader.peek()
            if (first == null || !first.isDigit()) {
                throw reader.createException("Not a decimal number: $first")
            }
            return SingleContent(reader.takeWhile { it.isDigit() })
        }

    }

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
        return Companion.checkedBuildContent(reader)
    }


}