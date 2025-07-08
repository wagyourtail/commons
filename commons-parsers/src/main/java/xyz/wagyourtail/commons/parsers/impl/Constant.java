package xyz.wagyourtail.commons.parsers.impl;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;
import xyz.wagyourtail.commons.parsers.impl.constant.BooleanConstant;
import xyz.wagyourtail.commons.parsers.impl.constant.NumberConstant;
import xyz.wagyourtail.commons.parsers.impl.constant.StringConstant;

import java.util.Locale;

public class Constant extends StringData.OnlyRaw<Data.SingleContent<?>> {

    private Constant(String rawContent) {
        super(rawContent, Constant::getContentChecked);
    }

    public Constant(CharReader<?> content) {
        super(content, Constant::getContentChecked);
    }

    public static Constant parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val constant = new Constant(reader);
        reader.expectEOS();
        return constant;
    }

    public static Constant unchecked(String rawContent) {
        return new Constant(rawContent);
    }

    public boolean isNull() {
        return getRawContent().equals("null");
    }

    public boolean isString() {
        return getRawContent().charAt(0) == '"';
    }

    public boolean isBoolean() {
        val raw = getRawContent().toLowerCase(Locale.ROOT);
        return raw.equals("true") || raw.equals("false");
    }

    public boolean isNumber() {
        val first = getRawContent().charAt(0);

        if (first == '-') {
            return true;
        }

        if (first >= '0' && first <= '9') {
            return true;
        }

        if (first == 'N' || first == 'I') {
            return true;
        }

        return first == '.';
    }

    public Object getValue() {
        if (isNull()) {
            return null;
        }
        if (isString()) {
            return StringConstant.unchecked(getRawContent()).getValue();
        }
        if (isBoolean()) {
            return BooleanConstant.unchecked(getRawContent()).getValue();
        }
        if (isNumber()) {
            return NumberConstant.unchecked(getRawContent()).getValue();
        }
        throw new IllegalStateException();
    }

    private static SingleContent<?> getContentChecked(CharReader<?> reader) {
        return new SingleContent<>(reader.parse(
                "constant",
                BooleanConstant::new,
                StringConstant::new,
                NumberConstant::new,
                r -> {
                    r.expect("null");
                    return null;
                }
        ));
    }



}
