package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.data.ParseCache;

public class Json5Identifier extends RawStringData {

    protected Json5Identifier(String rawContent) {
        super(rawContent);
    }

    private Json5Identifier(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5Identifier(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5Identifier> {

        private static boolean isStartChar(int c) {
            return Character.isAlphabetic(c) || c == '$' || c == '_';
        }

        private static boolean isContinueChar(int c) {
            if (Character.isAlphabetic(c) || c == '$' || c == '_') return true;
            int type = Character.getType(c);
            return type == Character.NON_SPACING_MARK ||
                type == Character.COMBINING_SPACING_MARK ||
                type == Character.DECIMAL_DIGIT_NUMBER ||
                type == Character.CONNECTOR_PUNCTUATION ||
                c == '\u200C' || c == '\u200D';
        }

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c == '\\' || isStartChar(c);
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            StringBuilder raw = new StringBuilder();
            int c = reader.take();
            if (c == -1) {
                throw reader.createException("Unexpected end of input");
            }
            raw.append((char) c);
            int check = c;
            if (c == '\\') {
                String[] seq = takeUnicodeEscapeSequence(reader);
                if (seq[0].length() != 1) {
                    throw reader.createException("Invalid identifier start: " + seq[0]);
                }
                raw.append(seq[1]);
                check = seq[0].charAt(0);
            }
            if (!isStartChar(check)) {
                throw reader.createException("Invalid identifier start: " + (char) check);
            }

            while (!reader.exhausted()) {
                reader.mark();
                c = reader.take();
                check = c;
                String seqRaw = null;
                if (c == '\\') {
                    String[] seq = takeUnicodeEscapeSequence(reader);
                    if (seq[0].length() != 1) {
                        throw reader.createException("Invalid identifier character: " + seq[0]);
                    }
                    seqRaw = seq[1];
                    check = seq[0].charAt(0);
                }
                if (isContinueChar(check)) {
                    if (seqRaw == null) raw.append((char) c);
                    else raw.append('\\').append(seqRaw);
                    continue;
                }
                reader.reset();
                break;
            }
            append.append(raw.toString());
        }

        /**
         * reads a backslash-u escape — returns {decoded, rawSeq}. note: legacy
         * grammar consumed exactly 3 hex digits after the 'u'.
         */
        private static String[] takeUnicodeEscapeSequence(CharReader<?> reader) {
            reader.expect('u');
            StringBuilder hex = new StringBuilder(4);
            for (int i = 0; i < 3; i++) {
                hex.append(reader.expect("hex", c -> Character.isDigit(c) ||
                    (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')));
            }
            return new String[]{
                new String(Character.toChars(Integer.parseInt(hex.toString(), 16))),
                "u" + hex
            };
        }

        @Override
        public Json5Identifier unchecked(String value) {
            return new Json5Identifier(value);
        }

        @Override
        public Json5Identifier uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5Identifier(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public Json5Identifier(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5Identifier parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static Json5Identifier unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    /** identifier name with escape sequences decoded */
    public String getValue() {
        return COMPANION.cached(getRawContent(), new ParseCache.Compute<String>() {
            @Override
            public String compute() {
                String raw = getRawContent();
                StringBuilder sb = new StringBuilder();
                int i = 0;
                while (i < raw.length()) {
                    if (raw.charAt(i) == '\\' && i + 4 < raw.length() && raw.charAt(i + 1) == 'u') {
                        sb.append(new String(Character.toChars(
                            Integer.parseInt(raw.substring(i + 2, i + 5), 16))));
                        i += 5;
                    } else {
                        sb.append(raw.charAt(i++));
                    }
                }
                return sb.toString();
            }
        });
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
