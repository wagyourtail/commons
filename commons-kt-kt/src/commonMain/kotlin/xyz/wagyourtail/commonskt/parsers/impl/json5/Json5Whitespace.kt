package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class Json5Whitespace : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<Json5Whitespace>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return reader.peek()?.isWhitespace() == true
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val first = reader.peek()
            if (first == null || !first.isWhitespace()) {
                throw reader.createException("Expected whitespace but got '$first'")
            }
            append(reader.takeWhile { it.isWhitespace() })
        }

        override fun unchecked(value: String): Json5Whitespace = Json5Whitespace(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5Whitespace =
            Json5Whitespace(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5Whitespace = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
