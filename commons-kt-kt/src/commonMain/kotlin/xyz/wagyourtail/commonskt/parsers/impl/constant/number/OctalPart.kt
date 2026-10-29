package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader

class OctalPart(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<String>>(rawContent) {

    constructor(content: SingleContent<String>) : this(content.toString())

    companion object : StringDataBuilder<OctalPart, SingleContent<String>> {

        override fun invoke(rawContent: CharReader<*>): OctalPart {
            return OctalPart(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
            val first = reader.peek()

            if (first != null && (first >= '0' && first <= '7')) {
                return SingleContent(reader.takeWhile { it >= '0' && it <= '7' })
            }

            throw reader.createException("Not a octal number: $first")
        }

    }

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
        return Companion.checkedBuildContent(reader)
    }

}