package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class DecimalPart : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<DecimalPart>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return reader.peek()?.isDigit() == true
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val first = reader.peek()
            if (first == null || !first.isDigit()) {
                throw reader.createException("Not a decimal number: $first")
            }
            append(reader.takeWhile { it.isDigit() })
        }

        override fun unchecked(value: String): DecimalPart = DecimalPart(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): DecimalPart =
            DecimalPart(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): DecimalPart = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
