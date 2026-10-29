package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class Json5ObjectKey extends RawStringData {

    protected Json5ObjectKey(String rawContent) {
        super(rawContent);
    }

    private Json5ObjectKey(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5ObjectKey(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5ObjectKey> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return Json5StringConstant.COMPANION.shouldRead(reader) ||
                Json5Identifier.COMPANION.shouldRead(reader);
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(reader.parse(
                    "json5 object key",
                    Json5StringConstant.COMPANION::read,
                    Json5Identifier.COMPANION::read
            ));
        }

        @Override
        public Json5ObjectKey unchecked(String value) {
            return new Json5ObjectKey(value);
        }

        @Override
        public Json5ObjectKey uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5ObjectKey(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public Json5ObjectKey(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5ObjectKey parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static Json5ObjectKey unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    public String getValue() {
        String raw = getRawContent();
        if (raw.startsWith("\"") || raw.endsWith("'")) {
            return StringUtils.translateEscapes(raw.substring(1, raw.length() - 1));
        }
        return StringUtils.translateEscapes(raw);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
