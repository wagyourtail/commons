package xyz.wagyourtail.commons.parsers.impl.constant.number;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class OctalPart extends RawStringData {

    protected OctalPart(String rawContent) {
        super(rawContent);
    }

    private OctalPart(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public OctalPart(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<OctalPart> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c != -1 && c >= '0' && c <= '7';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.peek();
            if (first != -1 && first >= '0' && first <= '7') {
                append.append(reader.takeWhile(e -> e >= '0' && e <= '7'));
                return;
            }
            throw reader.createException("Not a octal number: " + first);
        }

        @Override
        public OctalPart unchecked(String value) {
            return new OctalPart(value);
        }

        @Override
        public OctalPart uncheckedView(CharSequence buffer, int start, int end) {
            return new OctalPart(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public OctalPart(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static OctalPart parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static OctalPart unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
