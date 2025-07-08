package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader

class BinaryPart(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<String>>(rawContent) {

    constructor(content: SingleContent<String>) : this(content.toString())

    companion object : StringDataBuilder<BinaryPart, SingleContent<String>> {

        override fun invoke(rawContent: CharReader<*>): BinaryPart {
            return BinaryPart(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
            val first = reader.peek()
            if (first != '0' && first != '1') {
                throw reader.createException("Not a binary number: $first")
            }
            return SingleContent(reader.takeWhile { it == '0' || it == '1' })
        }

    }

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
        return Companion.checkedBuildContent(reader)
    }

}