package xyz.wagyourtail.commons.parsers.impl.constant;

import lombok.val;
import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

public class StringConstant extends StringData.OnlyRaw<Data.SingleContent<String>> {


    private StringConstant(String content) {
        super(content, StringConstant::getContentChecked);
    }

    public StringConstant(CharReader<?> content) {
        super(content, StringConstant::getContentChecked);
    }

    public static StringConstant parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new StringConstant(reader);
        reader.expectEOS();
        return number;
    }

    public static StringConstant unchecked(String rawContent) {
        return new StringConstant(rawContent);
    }

    public String getValue() {
        val content = getRawContent();
        return StringUtils.translateEscapes(content.substring(1, content.length() - 1));
    }

    private static SingleContent<String> getContentChecked(CharReader<?> reader) {
        return new SingleContent<>(reader.takeString(CharReader.TAKE_STRING_NO_TRANSLATE_ESCAPES, "\""));
    }

}
