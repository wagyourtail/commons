package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class OctalPart : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<OctalPart>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return c in '0'..'7'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val first = reader.peek()

            if (first != null && (first >= '0' && first <= '7')) {
                append(reader.takeWhile { it >= '0' && it <= '7' })
                return
            }

            throw reader.createException("Not a octal number: $first")
        }

        override fun unchecked(value: String): OctalPart = OctalPart(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): OctalPart =
            OctalPart(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): OctalPart = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
