package xyz.wagyourtail.commonskt.parsers.impl

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.constant.BooleanConstant
import xyz.wagyourtail.commonskt.parsers.impl.constant.NumberConstant
import xyz.wagyourtail.commonskt.parsers.impl.constant.StringConstant
import xyz.wagyourtail.commonskt.reader.CharReader

class Constant : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<Constant>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return c == '"' || c == '-' || c == '.' || c in '0'..'9' ||
                c == 't' || c == 'f' || c == 'n' || c == 'N' || c == 'I'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(
                reader.parse(
                    "constant",
                    { BooleanConstant.read(this) },
                    { StringConstant.read(this) },
                    { NumberConstant.read(this) },
                    {
                        expect("null")
                        "null"
                    }
                )
            )
        }

        override fun unchecked(value: String): Constant = Constant(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Constant =
            Constant(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Constant = read(rawContent)

    }

    val isNull: Boolean
        get() = rawContent == "null"

    val isString: Boolean
        get() = rawContent[0] == '"'

    val isBoolean: Boolean
        get() {
            val raw = rawContent.lowercase()
            return raw == "true" || raw == "false"
        }

    val isNumber: Boolean
        get() {
            val first = rawContent[0]

            if (first == '-') {
                return true
            }

            if (first >= '0' && first <= '9') {
                return true
            }

            if (first == 'N' || first == 'I') {
                return true
            }

            return first == '.'
        }

    val value: Any?
        get() {
            return when {
                isNull -> null
                isString -> StringConstant(rawContent).value
                isBoolean -> BooleanConstant(rawContent).value
                isNumber -> NumberConstant(rawContent).value
                else -> throw IllegalStateException()
            }
        }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
