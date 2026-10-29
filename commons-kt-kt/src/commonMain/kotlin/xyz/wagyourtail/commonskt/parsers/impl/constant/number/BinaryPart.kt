package xyz.wagyourtail.commonskt.parsers.impl.constant.number

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class BinaryPart : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<BinaryPart>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek()
            return c == '0' || c == '1'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val first = reader.peek()
            if (first != '0' && first != '1') {
                throw reader.createException("Not a binary number: $first")
            }
            append(reader.takeWhile { it == '0' || it == '1' })
        }

        override fun unchecked(value: String): BinaryPart = BinaryPart(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): BinaryPart =
            BinaryPart(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): BinaryPart = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
