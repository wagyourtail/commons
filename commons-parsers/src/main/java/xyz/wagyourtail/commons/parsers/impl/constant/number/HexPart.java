package xyz.wagyourtail.commons.parsers.impl.constant.number;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class HexPart extends RawStringData {

    protected HexPart(String rawContent) {
        super(rawContent);
    }

    private HexPart(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public HexPart(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<HexPart> {

        private static boolean isHex(int c) {
            return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
        }

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c != -1 && isHex(c);
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.peek();
            if (first != -1 && isHex(first)) {
                append.append(reader.takeWhile(HexPart.Companion::isHex));
                return;
            }
            throw reader.createException("Not a hex number: " + first);
        }

        @Override
        public HexPart unchecked(String value) {
            return new HexPart(value);
        }

        @Override
        public HexPart uncheckedView(CharSequence buffer, int start, int end) {
            return new HexPart(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public HexPart(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static HexPart parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static HexPart unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
