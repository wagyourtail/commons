package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.constant.BooleanConstant
import xyz.wagyourtail.commonskt.reader.CharReader

class Json5Value : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content)

    companion object : StringDataCompanion<Json5Value>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return c == '{' || c == '[' || c == '"' || c == '\'' ||
                BooleanConstant.shouldRead(reader) || Json5Number.shouldRead(reader) || c == 'n'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(
                reader.parse(
                    "json5 value",
                    { Json5Object.read(this) },
                    { Json5Array.read(this) },
                    { Json5StringConstant.read(this) },
                    { BooleanConstant.read(this) },
                    { Json5Number.read(this) },
                    {
                        expect("null")
                        "null"
                    }
                )
            )
        }

        override fun unchecked(value: String): Json5Value = Json5Value(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5Value =
            Json5Value(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5Value = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
