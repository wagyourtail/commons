package xyz.wagyourtail.commons.parsers.impl.constant;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.Collections;
import java.util.Locale;

public class BooleanConstant extends StringData.OnlyRaw {

    private BooleanConstant(String content) {
        super(content, BooleanConstant::getContentChecked);
    }

    public BooleanConstant(CharReader<?> content) {
        super(content, BooleanConstant::getContentChecked);
    }

    public static BooleanConstant parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new BooleanConstant(reader);
        reader.expectEOS();
        return number;
    }

    public static BooleanConstant unchecked(String rawContent) {
        return new BooleanConstant(rawContent);
    }

    public boolean getValue() {
        return Boolean.parseBoolean(getRawContent().toLowerCase(Locale.ROOT));
    }

    private static Content getContentChecked(CharReader<?> reader) {
        val value = reader.parse(
                (r) -> {
                    r.expect("true");
                    return true;
                },
                (r) -> {
                    r.expect("false");
                    return false;
                }
        );
        if (value == null) {
            throw reader.createException("Expected true/false but got: " + reader.take(5));
        }
        return new DefaultContent(Collections.singleton(value));
    }

}
