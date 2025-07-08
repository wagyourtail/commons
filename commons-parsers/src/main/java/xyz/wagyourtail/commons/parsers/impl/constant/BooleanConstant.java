package xyz.wagyourtail.commons.parsers.impl.constant;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.Locale;

public class BooleanConstant extends StringData.OnlyRaw<Data.SingleContent<Boolean>> {

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

    private static SingleContent<Boolean> getContentChecked(CharReader<?> reader) {
        val value = reader.parse(
                "boolean",
                (r) -> {
                    r.expect("true");
                    return true;
                },
                (r) -> {
                    r.expect("false");
                    return false;
                }
        );
        return new SingleContent<>(value);
    }

}
