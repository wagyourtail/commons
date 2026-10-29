package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class WholePart : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<WholePart>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return c != '0' && c.isDigit()
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val first = reader.peek()
            if (first == '0') {
                throw reader.createException("Whole number cannot start with 0")
            }
            if (first != null && first.isDigit()) {
                append(reader.takeWhile { it.isDigit() })
                return
            }

            throw reader.createException("Not a whole number: $first")
        }

        override fun unchecked(value: String): WholePart = WholePart(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): WholePart =
            WholePart(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): WholePart = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
