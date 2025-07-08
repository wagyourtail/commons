package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader

class ExponentPart(rawContent: String) : StringData.OnlyRaw<Data.ListContent>(rawContent) {

    constructor(content: Data.ListContent) : this(content.toString())

    companion object : StringDataBuilder<ExponentPart, Data.ListContent> {

        override fun invoke(rawContent: CharReader<*>): ExponentPart {
            return ExponentPart(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): ListContent {
            val content = mutableListOf<Any>()

            val first = reader.peek()
            if (first == '-' || first == '+') {
                content.add(reader.take()!!)
            }

            content.add(DecimalPart(reader))

            return ListContent(content)
        }

    }

    override fun checkedBuildContent(reader: CharReader<*>): ListContent {
        return Companion.checkedBuildContent(reader)
    }

}