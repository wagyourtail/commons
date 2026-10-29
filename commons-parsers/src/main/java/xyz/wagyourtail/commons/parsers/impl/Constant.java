package xyz.wagyourtail.commons.parsers.impl;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.constant.BooleanConstant;
import xyz.wagyourtail.commons.parsers.impl.constant.NumberConstant;
import xyz.wagyourtail.commons.parsers.impl.constant.StringConstant;

import java.util.Locale;

public class Constant extends RawStringData {

    protected Constant(String rawContent) {
        super(rawContent);
    }

    private Constant(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Constant(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Constant> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c == '"' || c == '-' || c == '.' || (c >= '0' && c <= '9') ||
                c == 't' || c == 'f' || c == 'n' || c == 'N' || c == 'I';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(reader.parse(
                    "constant",
                    BooleanConstant.COMPANION::read,
                    StringConstant.COMPANION::read,
                    NumberConstant.COMPANION::read,
                    (r) -> {
                        r.expect("null");
                        return "null";
                    }
            ));
        }

        @Override
        public Constant unchecked(String value) {
            return new Constant(value);
        }

        @Override
        public Constant uncheckedView(CharSequence buffer, int start, int end) {
            return new Constant(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public Constant(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Constant parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static Constant unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    public boolean isNull() {
        return getRawContent().equals("null");
    }

    public boolean isString() {
        return getRawContent().charAt(0) == '"';
    }

    public boolean isBoolean() {
        String raw = getRawContent().toLowerCase(Locale.ROOT);
        return raw.equals("true") || raw.equals("false");
    }

    public boolean isNumber() {
        int first = getRawContent().charAt(0);

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

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
