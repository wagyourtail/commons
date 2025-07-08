package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.parsers.impl.constant.BooleanConstant
import xyz.wagyourtail.commonskt.reader.CharReader

class Json5Value(content: SingleContent<*>) : StringData.OnlyParsed<Data.SingleContent<*>>(content) {

    companion object : StringDataBuilder<Json5Value, Data.SingleContent<*>> {

        override fun invoke(rawContent: CharReader<*>): Json5Value {
            return Json5Value(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<*> {
            return SingleContent<Any?>(reader.parse(
                "json5 value",
                Json5Object::invoke,
                Json5Array::invoke,
                Json5StringConstant::invoke,
                BooleanConstant::invoke,
                Json5Number::invoke,
                {
                    expect("null")
                    null
                }
            ))
        }

    }

}