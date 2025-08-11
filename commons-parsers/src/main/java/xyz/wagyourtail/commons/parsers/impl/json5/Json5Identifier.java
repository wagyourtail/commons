package xyz.wagyourtail.commons.parsers.impl.json5;

import lombok.val;
import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

public class Json5Identifier extends StringData.OnlyRaw<Data.SingleContent<String>> {

    public Json5Identifier(String rawContent) {
        super(rawContent, Json5Identifier::getContentChecked);
    }

    public Json5Identifier(CharReader<?> reader) {
        super(reader, Json5Identifier::getContentChecked);
    }

    public static Json5Identifier parse(String reader) {
        StringCharReader charReader = new StringCharReader(reader);
        val content = new Json5Identifier(charReader);
        charReader.expectEOS();
        return content;
    }

    public static Json5Identifier unchecked(String reader) {
        return new Json5Identifier(reader);
    }

    public static Data.SingleContent<String> getContentChecked(CharReader<?> r) {
        StringBuilder sb = new StringBuilder();
        char first = (char) r.take();
        if (first == '\\') {
            String s = takeUnicodeEscapeSequence(r);
            if (s.length() != 1) {
                throw r.createException("Invalid identifier start: " + s);
            }
            first = s.charAt(0);
        }
        if (
                !Character.isAlphabetic(first) &&
                        first != '$' &&
                        first != '_'
        ) {
            throw r.createException("Invalid identifier start: " + first);
        }
        sb.append(first);

        while (!r.exhausted()) {
            r.mark();
            char c = (char) r.take();
            if (c == '\\') {
                String s = takeUnicodeEscapeSequence(r);
                if (s.length() != 1) {
                    throw r.createException("Invalid identifier character: " + s);
                }
                c = s.charAt(0);
            }
            if (
                    Character.isAlphabetic(c) ||
                            c == '$' ||
                            c == '_'
            ) {
                sb.append(c);
                continue;
            }
            int type = Character.getType(c);
            if (
                    type == Character.NON_SPACING_MARK ||
                            type == Character.COMBINING_SPACING_MARK ||
                            type == Character.DECIMAL_DIGIT_NUMBER ||
                            type == Character.CONNECTOR_PUNCTUATION ||
                            c == '\u200C' ||
                            c == '\u200D'
            ) {
                sb.append(c);
                continue;
            }
            r.reset();
            break;
        }

        return new SingleContent<>(sb.toString());
    }

    private static String takeUnicodeEscapeSequence(CharReader<?> reader) {
        reader.expect('u');

        StringBuilder hex = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            hex.append(reader.expect("hex", c -> (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')));
        }

        return new String(Character.toChars(Integer.parseInt(hex.toString(), 16)));
    }

    public String getValue() {
        return StringUtils.translateEscapes(getRawContent());
    }

}
