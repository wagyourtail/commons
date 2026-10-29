package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class Json5Whitespace extends RawStringData {

    protected Json5Whitespace(String rawContent) {
        super(rawContent);
    }

    private Json5Whitespace(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5Whitespace(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5Whitespace> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return Character.isWhitespace(reader.peek());
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.peek();
            if (!Character.isWhitespace(first)) {
                throw reader.createException("Expected whitespace but got '" + (char) first + "'");
            }
            append.append(reader.takeWhile(Character::isWhitespace));
        }

        @Override
        public Json5Whitespace unchecked(String value) {
            return new Json5Whitespace(value);
        }

        @Override
        public Json5Whitespace uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5Whitespace(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public Json5Whitespace(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5Whitespace parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static Json5Whitespace unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
