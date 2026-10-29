package xyz.wagyourtail.commons.parsers.impl.constant;

import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class StringConstant extends RawStringData {

    protected StringConstant(String rawContent) {
        super(rawContent);
    }

    private StringConstant(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public StringConstant(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<StringConstant> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return reader.peek() == '"';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(reader.takeString(CharReader.TAKE_STRING_NO_TRANSLATE_ESCAPES, "\""));
        }

        @Override
        public StringConstant unchecked(String value) {
            return new StringConstant(value);
        }

        @Override
        public StringConstant uncheckedView(CharSequence buffer, int start, int end) {
            return new StringConstant(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public StringConstant(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static StringConstant parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static StringConstant unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    public String getValue() {
        String content = getRawContent();
        return StringUtils.translateEscapes(content.substring(1, content.length() - 1));
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
