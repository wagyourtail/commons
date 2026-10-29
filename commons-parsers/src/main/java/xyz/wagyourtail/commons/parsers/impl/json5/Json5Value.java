package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.constant.BooleanConstant;

public class Json5Value extends RawStringData {

    protected Json5Value(String rawContent) {
        super(rawContent);
    }

    private Json5Value(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5Value(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5Value> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            if (c == '{' || c == '[' || c == '"' || c == '\'') return true;
            return BooleanConstant.COMPANION.shouldRead(reader) ||
                Json5Number.COMPANION.shouldRead(reader) || c == 'n';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(reader.parse(
                    "json5 value",
                    Json5Object.COMPANION::read,
                    Json5Array.COMPANION::read,
                    Json5StringConstant.COMPANION::read,
                    BooleanConstant.COMPANION::read,
                    Json5Number.COMPANION::read,
                    (r) -> {
                        r.expect("null");
                        return "null";
                    }
            ));
        }

        @Override
        public Json5Value unchecked(String value) {
            return new Json5Value(value);
        }

        @Override
        public Json5Value uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5Value(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public Json5Value(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5Value parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static Json5Value parse(CharReader<?> reader) {
        return COMPANION.read(reader);
    }

    public Object getValue() {
        return ((Content.SingleContent) getContent()).getValue();
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
