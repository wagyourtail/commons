package xyz.wagyourtail.commons.parsers.impl.json5;

import lombok.val;
import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;
import xyz.wagyourtail.commons.parsers.impl.constant.StringConstant;

public class Json5ObjectKey extends StringData.OnlyRaw<Data.SingleContent<?>> {

    public Json5ObjectKey(String rawContent) {
        super(rawContent, Json5ObjectKey::getContentChecked);
    }

    public Json5ObjectKey(CharReader<?> reader) {
        super(reader, Json5ObjectKey::getContentChecked);
    }

    public static Json5ObjectKey parse(String reader) {
        StringCharReader charReader = new StringCharReader(reader);
        val content = new Json5ObjectKey(charReader);
        charReader.expectEOS();
        return content;
    }

    public static Json5ObjectKey unchecked(String rawContent) {
        return new Json5ObjectKey(rawContent);
    }

    public String getValue() {
        String raw = getRawContent();
        if (raw.startsWith("\"")) {
            return StringUtils.translateEscapes(raw.substring(1, raw.length() - 1));
        } else {
            return StringUtils.translateEscapes(raw);
        }
    }

    public static SingleContent<?> getContentChecked(CharReader<?> reader) {
        return new SingleContent<>(reader.parse(
            "json5 object key",
            StringConstant::new,
            Json5Identifier::new
        ));
    }

}
