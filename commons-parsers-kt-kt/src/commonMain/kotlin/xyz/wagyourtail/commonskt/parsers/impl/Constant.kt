package xyz.wagyourtail.commonskt.parsers.impl

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.parsers.impl.constant.BooleanConstant
import xyz.wagyourtail.commonskt.parsers.impl.constant.NumberConstant
import xyz.wagyourtail.commonskt.parsers.impl.constant.StringConstant
import xyz.wagyourtail.commonskt.reader.CharReader

class Constant(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<*>>(rawContent) {

    constructor(content: SingleContent<*>) : this(content.toString())

    companion object : StringDataBuilder<Constant, SingleContent<*>> {

        override fun invoke(rawContent: CharReader<*>): Constant {
            return Constant(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<*> {
            return SingleContent(
                reader.parse(
                "constant",
                BooleanConstant::invoke,
                StringConstant::invoke,
                NumberConstant::invoke,
                {
                    expect("null")
                    null
                }
            ))
        }

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

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<*> {
        return Companion.checkedBuildContent(reader)
    }

}