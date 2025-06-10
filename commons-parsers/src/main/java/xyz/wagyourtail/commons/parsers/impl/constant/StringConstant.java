package xyz.wagyourtail.commons.parsers.impl.constant;

import lombok.val;
import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.Collections;

public class StringConstant extends StringData.OnlyRaw {


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

    private static Content getContentChecked(CharReader<?> reader) {
        return new DefaultContent(Collections.singleton("\"" + StringUtils.escape(reader.takeString(), true) + "\""));
    }

}
