package xyz.wagyourtail.commonskt.parsers.impl.constant

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class BooleanConstant : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<BooleanConstant>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek()
            return c == 't' || c == 'f'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(
                reader.parse(
                    "boolean",
                    {
                        expect("true")
                        "true"
                    },
                    {
                        expect("false")
                        "false"
                    }
                )
            )
        }

        override fun unchecked(value: String): BooleanConstant = BooleanConstant(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): BooleanConstant =
            BooleanConstant(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): BooleanConstant = read(rawContent)

    }

    val value: Boolean
        get() = rawContent.lowercase().toBoolean()

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
