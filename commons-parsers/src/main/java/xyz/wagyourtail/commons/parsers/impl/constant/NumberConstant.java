package xyz.wagyourtail.commons.parsers.impl.constant;

import xyz.wagyourtail.commons.core.NumberUtils;
import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.constant.number.*;

public class NumberConstant extends RawStringData {

    protected NumberConstant(String rawContent) {
        super(rawContent);
    }

    private NumberConstant(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public NumberConstant(Content content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<NumberConstant> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c == '-' || c == '.' || c == 'N' || c == 'I' || (c >= '0' && c <= '9');
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.peek();
            if (first == '-') {
                append.append((char) reader.take());
            }
            int next = reader.peek();
            if (next > '0' && next <= '9') {
                append.append(WholePart.COMPANION.read(reader));
                next = reader.peek();
                if (next == 'l' || next == 'L') {
                    append.append((char) reader.take());
                    return;
                }
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
                    next = reader.peek();
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
                            next = reader.peek();
                        }
                        break;
                    case 'x':
                    case 'X':
                        append.append((char) reader.take());
                        append.append(HexPart.COMPANION.read(reader));
                        next = reader.peek();
                        if (next == 'l' || next == 'L') {
                            append.append((char) reader.take());
                        }
                        return;
                    case 'b':
                    case 'B':
                        append.append((char) reader.take());
                        append.append(BinaryPart.COMPANION.read(reader));
                        next = reader.peek();
                        if (next == 'l' || next == 'L') {
                            append.append((char) reader.take());
                        }
                        return;
                    case -1:
                        break;
                    default:
                        if (next >= '0' && next < '8') {
                            append.append(OctalPart.COMPANION.read(reader));
                            next = reader.peek();
                            if (next == 'l' || next == 'L') {
                                append.append((char) reader.take());
                            }
                            return;
                        }
                }
            } else if (next == '.') {
                append.append((char) reader.take());
                append.append(DecimalPart.COMPANION.read(reader));
                next = reader.peek();
                if (next == 'e' || next == 'E') {
                    append.append((char) reader.take());
                    append.append(ExponentPart.COMPANION.read(reader));
                    next = reader.peek();
                }
            } else if (next == 'I') {
                append.append(reader.expect("Infinity"));
                next = reader.peek();
            } else if (next == 'N') {
                append.append(reader.expect("NaN"));
                next = reader.peek();
            } else {
                throw reader.createException("Not a number character: " + (char) next);
            }

            if (next == -1) return;
            if (next == 'f' || next == 'F'
                    || next == 'd' || next == 'D') {
                append.append((char) reader.take());
            }
        }

        @Override
        public NumberConstant unchecked(String value) {
            return new NumberConstant(value);
        }

        @Override
        public NumberConstant uncheckedView(CharSequence buffer, int start, int end) {
            return new NumberConstant(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public NumberConstant(CharReader<?> reader) {
        this(COMPANION.read(reader).getRawContent());
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static NumberConstant parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#unchecked(String)} */
    @Deprecated
    public static NumberConstant unchecked(String rawContent) {
        return COMPANION.unchecked(rawContent);
    }

    public boolean isNegative() {
        return getRawContent().charAt(0) == '-';
    }

    public NumberConstant asPositive() {
        String raw = getRawContent();
        if (raw.charAt(0) == '-') {
            return unchecked(raw.substring(1));
        }
        return this;
    }

    public NumberConstant asNegative() {
        String raw = getRawContent();
        if (raw.charAt(0) != '-') {
            return unchecked("-" + raw);
        }
        return this;
    }

    public boolean isWhole() {
        String positive = asPositive().getRawContent();

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
        String positive = asPositive().getRawContent();
        char last = positive.charAt(positive.length() - 1);

        if (positive.charAt(0) == '0') {
            if (positive.length() == 1) {
                return false;
            }

            char next = positive.charAt(1);
            return next == '.' || next == 'e' || next == 'E';
        }

        if (last == 'd' || last == 'D' || last == 'f' || last == 'F') {
            return true;
        }

        if (last == 'l' || last == 'L') {
            return false;
        }

        return positive.contains(".") || positive.contains("e") || positive.contains("E");
    }

    public boolean isHex() {
        String positive = asPositive().getRawContent();
        return positive.startsWith("0x") || positive.startsWith("0X");
    }

    public boolean isBinary() {
        String positive = asPositive().getRawContent();
        return positive.startsWith("0b") || positive.startsWith("0B");
    }

    public boolean isOctal() {
        String positive = asPositive().getRawContent();

        if (positive.length() == 1) {
            return false;
        }

        char second = positive.charAt(1);
        return positive.charAt(0) == '0' && (second >= '0' && second <= '7');
    }

    public boolean isFloat() {
        String raw = getRawContent();
        char last = raw.charAt(raw.length() - 1);
        return (last == 'f' || last == 'F') && !isHex();
    }

    public boolean isLong() {
        String raw = getRawContent();
        char last = raw.charAt(raw.length() - 1);
        return last == 'l' || last == 'L';
    }

    public boolean isDouble() {
        String positive = asPositive().getRawContent();

        if (positive.startsWith("I") || positive.startsWith("N")) {
            return true;
        }

        if (isDecimal()) {
            char last = positive.charAt(positive.length() - 1);
            return last != 'f' && last != 'F';
        }
        return false;
    }

    public boolean isInteger() {
        return isWhole() && !isLong();
    }

    public Number getValue() {
        if (isNegative()) {
            return NumberUtils.negate(asPositive().getValue());
        }
        String raw = getRawContent();
        if (isHex()) {
            if (isLong()) {
                return Long.parseLong(raw.substring(2, raw.length() - 1), 16);
            } else {
                return Integer.parseInt(raw.substring(2), 16);
            }
        }
        if (isBinary()) {
            if (isLong()) {
                return Long.parseLong(raw.substring(2, raw.length() - 1), 2);
            } else {
                return Integer.parseInt(raw.substring(2), 2);
            }
        }
        if (isOctal()) {
            if (isLong()) {
                return Long.parseLong(raw.substring(1, raw.length() - 1), 8);
            } else {
                return Integer.parseInt(raw.substring(1), 8);
            }
        }
        if (isFloat()) {
            return Float.parseFloat(raw.substring(0, raw.length() - 1));
        }
        if (isLong()) {
            return Long.parseLong(raw.substring(0, raw.length() - 1));
        }
        if (isDouble()) {
            char last = raw.charAt(raw.length() - 1);
            if (last == 'd' || last == 'D') {
                return Double.parseDouble(raw.substring(0, raw.length() - 1));
            } else {
                return Double.parseDouble(raw);
            }
        }
        if (isInteger()) {
            return Integer.parseInt(raw);
        }
        throw new IllegalStateException();
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        return COMPANION.buildContent(reader);
    }

}
