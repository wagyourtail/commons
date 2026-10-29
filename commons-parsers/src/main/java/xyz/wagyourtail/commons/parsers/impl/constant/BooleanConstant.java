package xyz.wagyourtail.commons.parsers.impl.constant;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

import java.util.Locale;

public class BooleanConstant extends RawStringData {

    protected BooleanConstant(String rawContent) {
        super(rawContent);
    }

    private BooleanConstant(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public BooleanConstant(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<BooleanConstant> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c == 't' || c == 'f';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(reader.parse(
                    "boolean",
                    (r) -> {
                        r.expect("true");
                        return "true";
                    },
                    (r) -> {
                        r.expect("false");
                        return "false";
                    }
            ));
        }

        @Override
        public BooleanConstant unchecked(String value) {
            return new BooleanConstant(value);
        }

        @Override
        public BooleanConstant uncheckedView(CharSequence buffer, int start, int end) {
            return new BooleanConstant(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public BooleanConstant(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static BooleanConstant parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static BooleanConstant unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    public boolean getValue() {
        return Boolean.parseBoolean(getRawContent().toLowerCase(Locale.ROOT));
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
