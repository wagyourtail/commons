package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.NumberUtils;
import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.constant.number.BinaryPart;
import xyz.wagyourtail.commons.parsers.impl.constant.number.DecimalPart;
import xyz.wagyourtail.commons.parsers.impl.constant.number.ExponentPart;
import xyz.wagyourtail.commons.parsers.impl.constant.number.HexPart;

public class Json5Number extends RawStringData {

    protected Json5Number(String rawContent) {
        super(rawContent);
    }

    private Json5Number(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5Number(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5Number> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c == '-' || c == '+' || c == '.' || c == 'N' || c == 'I' || (c >= '0' && c <= '9');
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.peek();
            if (first == '-' || first == '+') {
                append.append((char) reader.take());
            }

            int next = reader.peek();
            if (next > '0' && next <= '9') {
                append.append(DecimalPart.COMPANION.read(reader));
                next = reader.peek();
                if (next == '.') {
                    append.append((char) reader.take());
                    next = reader.peek();
                    if (next >= '0' && next <= '9') {
                        append.append(DecimalPart.COMPANION.read(reader));
                    }
                    next = reader.peek();
                }
                if (next == 'e' || next == 'E') {
                    append.append((char) reader.take());
                    append.append(ExponentPart.COMPANION.read(reader));
                }
            } else if (next == '0') {
                append.append((char) reader.take());
                next = reader.peek();
                switch (next) {
                    case '.':
                        append.append((char) reader.take());
                        next = reader.peek();
                        if (next >= '0' && next <= '9') {
                            append.append(DecimalPart.COMPANION.read(reader));
                        }
                        next = reader.peek();
                        if (next == 'e' || next == 'E') {
                            append.append((char) reader.take());
                            append.append(ExponentPart.COMPANION.read(reader));
                        }
                        break;
                    case 'x':
                    case 'X':
                        append.append((char) reader.take());
                        append.append(HexPart.COMPANION.read(reader));
                        return;
                    case 'b':
                    case 'B':
                        append.append((char) reader.take());
                        append.append(BinaryPart.COMPANION.read(reader));
                        return;
                    case -1:
                    default:
                        break;
                }
            } else if (next == '.') {
                append.append((char) reader.take());
                append.append(DecimalPart.COMPANION.read(reader));
                next = reader.peek();
                if (next == 'e' || next == 'E') {
                    append.append((char) reader.take());
                    append.append(ExponentPart.COMPANION.read(reader));
                }
            } else if (next == 'I') {
                append.append(reader.expect("Infinity"));
            } else if (next == 'N') {
                append.append(reader.expect("NaN"));
            } else {
                throw reader.createException("Not a number character: " + (char) next);
            }
        }

        @Override
        public Json5Number unchecked(String value) {
            return new Json5Number(value);
        }

        @Override
        public Json5Number uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5Number(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public Json5Number(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5Number parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static Json5Number unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    public boolean isNegative() {
        return getRawContent().charAt(0) == '-';
    }

    public Json5Number asPositive() {
        String raw = getRawContent();
        if (raw.charAt(0) == '-') {
            return unchecked(raw.substring(1));
        }
        return this;
    }

    public Json5Number asNegative() {
        String raw = getRawContent();
        if (raw.charAt(0) == '-') {
            return this;
        } else if (raw.charAt(0) == '+') {
            return unchecked("-" + raw.substring(1));
        } else {
            return unchecked("-" + raw);
        }
    }

    public boolean isWhole() {
        String positive = asPositive().getRawContent().replaceFirst("^\\+", "");

        if (positive.length() == 1) {
            return true;
        }

        if (positive.charAt(0) == '0') {
            return false;
        }

        char last = positive.charAt(positive.length() - 1);
        if (last == 'd' || last == 'D' || last == 'f' || last == 'F') {
            return false;
        }

        return !positive.contains(".") && !positive.contains("e") && !positive.contains("E");
    }

    public boolean isDecimal() {
        String raw = getRawContent();

        if (raw.contains(".") || raw.contains("e") || raw.contains("E")) {
            return true;
        }

        String positive = asPositive().getRawContent().replaceFirst("^\\+", "");

        return positive.startsWith("I") || positive.startsWith("N");
    }

    public boolean isHex() {
        String positive = asPositive().getRawContent().replaceFirst("^\\+", "");
        return positive.startsWith("0x") || positive.startsWith("0X");
    }

    public boolean isBinary() {
        String positive = asPositive().getRawContent().replaceFirst("^\\+", "");
        return positive.startsWith("0b") || positive.startsWith("0B");
    }

    public Number getValue() {
        if (isNegative()) {
            return NumberUtils.negate(asPositive().getValue());
        }
        String raw = asPositive().getRawContent().replaceFirst("^\\+", "");
        if (isHex()) {
            return Long.parseLong(raw.substring(2), 16);
        }
        if (isBinary()) {
            return Long.parseLong(raw.substring(2), 2);
        }
        if (isDecimal()) {
            return Double.parseDouble(raw);
        }
        if (isWhole()) {
            return Long.parseLong(raw);
        }
        throw new IllegalStateException();
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
