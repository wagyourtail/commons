package xyz.wagyourtail.commonskt.parsers.impl.constant

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.translateEscapes

class StringConstant : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<StringConstant>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return reader.peek() == '"'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(reader.takeString(noTranslateEscapes = true, quote = "\""))
        }

        override fun unchecked(value: String): StringConstant = StringConstant(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): StringConstant =
            StringConstant(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): StringConstant = read(rawContent)

    }

    val value: String
        get() = rawContent.substring(1, rawContent.length - 1).translateEscapes()

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
