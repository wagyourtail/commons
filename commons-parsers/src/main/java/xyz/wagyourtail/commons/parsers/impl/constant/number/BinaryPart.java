package xyz.wagyourtail.commons.parsers.impl.constant.number;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class BinaryPart extends RawStringData {

    protected BinaryPart(String rawContent) {
        super(rawContent);
    }

    private BinaryPart(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public BinaryPart(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<BinaryPart> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c != -1 && (c == '0' || c == '1');
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.peek();
            if (first == -1 || (first != '0' && first != '1')) {
                throw reader.createException("Not a binary number: " + first);
            }
            append.append(reader.takeWhile(e -> e == '0' || e == '1'));
        }

        @Override
        public BinaryPart unchecked(String value) {
            return new BinaryPart(value);
        }

        @Override
        public BinaryPart uncheckedView(CharSequence buffer, int start, int end) {
            return new BinaryPart(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public BinaryPart(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static BinaryPart parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static BinaryPart unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
