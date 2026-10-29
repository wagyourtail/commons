package xyz.wagyourtail.commons.parsers.impl.constant.number;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class DecimalPart extends RawStringData {

    protected DecimalPart(String rawContent) {
        super(rawContent);
    }

    private DecimalPart(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public DecimalPart(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<DecimalPart> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c != -1 && c >= '0' && c <= '9';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.peek();
            if (first == -1 || first < '0' || first > '9') {
                throw reader.createException("Not a decimal number: " + first);
            }
            append.append(reader.takeWhile(e -> e >= '0' && e <= '9'));
        }

        @Override
        public DecimalPart unchecked(String value) {
            return new DecimalPart(value);
        }

        @Override
        public DecimalPart uncheckedView(CharSequence buffer, int start, int end) {
            return new DecimalPart(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public DecimalPart(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static DecimalPart parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static DecimalPart unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
