package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class HexPart : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<HexPart>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return c.isDigit() || c in 'a'..'f' || c in 'A'..'F'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val first = reader.peek()

            if (first != null && (first.isDigit() || (first >= 'a' && first <= 'f') || (first >= 'A' && first <= 'F'))) {
                append(reader.takeWhile { it.isDigit() || (it >= 'a' && it <= 'f') || (it >= 'A' && it <= 'F') })
                return
            }

            throw reader.createException("Not a hex number: $first")
        }

        override fun unchecked(value: String): HexPart = HexPart(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): HexPart =
            HexPart(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): HexPart = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
