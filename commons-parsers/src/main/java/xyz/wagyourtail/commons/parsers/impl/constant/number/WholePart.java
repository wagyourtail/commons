package xyz.wagyourtail.commons.parsers.impl.constant.number;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class WholePart extends RawStringData {

    protected WholePart(String rawContent) {
        super(rawContent);
    }

    private WholePart(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public WholePart(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<WholePart> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c != -1 && c != '0' && c >= '0' && c <= '9';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.peek();
            if (first != -1 && first == '0') {
                throw reader.createException("Whole number cannot start with 0");
            }
            if (first != -1 && first >= '0' && first <= '9') {
                append.append(reader.takeWhile(e -> e >= '0' && e <= '9'));
                return;
            }
            throw reader.createException("Not a whole number: " + first);
        }

        @Override
        public WholePart unchecked(String value) {
            return new WholePart(value);
        }

        @Override
        public WholePart uncheckedView(CharSequence buffer, int start, int end) {
            return new WholePart(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public WholePart(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static WholePart parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static WholePart unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
