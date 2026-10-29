package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class Json5StringConstant extends RawStringData {

    protected Json5StringConstant(String rawContent) {
        super(rawContent);
    }

    private Json5StringConstant(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5StringConstant(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5StringConstant> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c == '"' || c == '\'';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int quote = reader.peek();
            if (quote == '\"' || quote == '\'') {
                append.append(reader.takeString(
                        CharReader.TAKE_STRING_LEINIENT |
                            CharReader.TAKE_STRING_ESCAPE_NEWLINE |
                            CharReader.TAKE_STRING_NO_TRANSLATE_ESCAPES,
                        String.valueOf((char) quote)
                ));
                return;
            }
            throw reader.createException("Expected string start char but got: " + (char) quote);
        }

        @Override
        public Json5StringConstant unchecked(String value) {
            return new Json5StringConstant(value);
        }

        @Override
        public Json5StringConstant uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5StringConstant(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public Json5StringConstant(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5StringConstant parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static Json5StringConstant unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    public String getValue() {
        String content = getRawContent();
        return StringUtils.translateEscapes(content.substring(1, content.length() - 1).replace("\\\n", ""));
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
