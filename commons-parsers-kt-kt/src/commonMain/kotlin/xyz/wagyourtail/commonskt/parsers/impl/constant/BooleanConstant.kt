package xyz.wagyourtail.commonskt.parsers.impl.constant

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader

class BooleanConstant(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<Boolean>>(rawContent) {

    constructor(content: SingleContent<Boolean>) : this(content.toString())

    companion object : StringDataBuilder<BooleanConstant, SingleContent<Boolean>> {

        override fun invoke(rawContent: CharReader<*>): BooleanConstant {
            return BooleanConstant(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<Boolean> {
            val value = reader.parse("boolean",
                {
                    expect("true")
                    true
                },
                {
                    expect("false")
                    false
                }
            )
            return SingleContent(value)
        }
    }

    val value: Boolean
        get() = rawContent.lowercase().toBoolean()

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<Boolean> {
        return Companion.checkedBuildContent(reader)
    }

}