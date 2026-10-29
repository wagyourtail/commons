package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

public class Json5Comment extends RawStringData {

    protected Json5Comment(String rawContent) {
        super(rawContent);
    }

    private Json5Comment(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5Comment(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5Comment> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            if (reader.peek() != '/') return false;
            CharReader<?> copy = reader.copy(1);
            int n = copy.take();
            return n == '/' || n == '*';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            reader.parse(
                    "comment",
                    (r) -> {
                        append.append(r.expect("//"));
                        append.append(r.takeUntil("\n"));
                        if (!r.exhausted()) {
                            append.append(r.expect('\n'));
                        }
                        return null;
                    },
                    (r) -> {
                        append.append(r.expect("/*"));
                        append.append(r.takeUntil("*/"));
                        append.append(r.expect("*/"));
                        return null;
                    }
            );
        }

        @Override
        public Json5Comment unchecked(String value) {
            return new Json5Comment(value);
        }

        @Override
        public Json5Comment uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5Comment(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public Json5Comment(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5Comment parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static Json5Comment unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
