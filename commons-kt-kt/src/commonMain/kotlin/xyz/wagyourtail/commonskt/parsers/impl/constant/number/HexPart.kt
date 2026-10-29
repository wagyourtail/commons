package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader

class HexPart(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<String>>(rawContent) {

    constructor(content: SingleContent<String>) : this(content.toString())

    companion object : StringDataBuilder<HexPart, SingleContent<String>> {

        override fun invoke(rawContent: CharReader<*>): HexPart {
            return HexPart(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
            val first = reader.peek()

            if (first != null && (first.isDigit() || (first >= 'a' && first <= 'f') || (first >= 'A' && first <= 'F'))) {
                return SingleContent(reader.takeWhile { it.isDigit() || (it >= 'a' && it <= 'f') || (it >= 'A' && it <= 'F') })
            }

            throw reader.createException("Not a hex number: $first")
        }

    }

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
        return Companion.checkedBuildContent(reader)
    }

}