package xyz.wagyourtail.commons.parsers.impl.json5;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;
import xyz.wagyourtail.commons.parsers.impl.constant.BooleanConstant;

public class Json5Value extends StringData.OnlyParsed<Data.SingleContent<?>> {

    protected Json5Value(SingleContent<?> content) {
        super(content);
    }

    public static Json5Value parse(String raw) {
        StringCharReader charReader = new StringCharReader(raw);
        val content = contentBuilder(charReader);
        return new Json5Value(content);
    }

    public static Json5Value parse(CharReader<?> reader) {
        return new Json5Value(contentBuilder(reader));
    }

    public static SingleContent<?> contentBuilder(CharReader<?> reader) {
        return new SingleContent<>(reader.<Object>parse(
                "json5 value",
                Json5Object::parse,
                Json5Array::parse,
                Json5StringConstant::new,
                BooleanConstant::new,
                Json5Number::new,
                r -> {
                    r.expect("null");
                    return null;
                }
        ));
    }

    public Object getValue() {
        return getContent().getValue();
    }

}
