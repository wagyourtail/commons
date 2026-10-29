package xyz.wagyourtail.commons.parsers.impl.constant.number;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class ExponentPart extends RawStringData {

    protected ExponentPart(String rawContent) {
        super(rawContent);
    }

    private ExponentPart(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public ExponentPart(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<ExponentPart> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c != -1 && (c == '-' || c == '+' || (c >= '0' && c <= '9'));
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.peek();
            if (first != -1 && (first == '-' || first == '+')) {
                append.append((char) reader.take());
            }
            append.append(DecimalPart.COMPANION.read(reader));
        }

        @Override
        public ExponentPart unchecked(String value) {
            return new ExponentPart(value);
        }

        @Override
        public ExponentPart uncheckedView(CharSequence buffer, int start, int end) {
            return new ExponentPart(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public ExponentPart(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static ExponentPart parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static ExponentPart unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
