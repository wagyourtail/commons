package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.translateEscapes

class Json5ObjectKey : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<Json5ObjectKey>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return Json5StringConstant.shouldRead(reader) || Json5Identifier.shouldRead(reader)
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(
                reader.parse(
                    "json5 object key",
                    { Json5StringConstant.read(this) },
                    { Json5Identifier.read(this) }
                )
            )
        }

        override fun unchecked(value: String): Json5ObjectKey = Json5ObjectKey(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5ObjectKey =
            Json5ObjectKey(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5ObjectKey = read(rawContent)

    }

    val value: String
        get() {
            val raw = rawContent
            if (raw.startsWith("\"") || raw.endsWith("'")) {
                return raw.substring(1, raw.length - 1).translateEscapes()
            }
            return raw.translateEscapes()
        }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }
}
