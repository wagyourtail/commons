package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.translateEscapes

class Json5ObjectKey(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<*>>(rawContent) {

    constructor(content: Data.SingleContent<*>) : this(content.toString())

    companion object : StringDataBuilder<Json5ObjectKey, SingleContent<*>> {

        override fun invoke(rawContent: CharReader<*>): Json5ObjectKey {
            return Json5ObjectKey(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<*> {
            return SingleContent(
                reader.parse(
                    "json5 object key",
                    Json5StringConstant::invoke,
                    Json5Identifier::invoke
                )
            )
        }

    }

    val value: String
        get() {
            val raw = rawContent
            if (raw.startsWith("\"") || raw.endsWith("'")) {
                return raw.substring(1, raw.length - 1).translateEscapes()
            }
            return raw.translateEscapes()
        }

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<*> {
        return Companion.checkedBuildContent(reader)
    }
}