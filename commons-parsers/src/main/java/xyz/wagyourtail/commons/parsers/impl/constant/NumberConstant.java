package xyz.wagyourtail.commons.parsers.impl.constant;

import lombok.val;
import lombok.var;
import xyz.wagyourtail.commons.core.NumberUtils;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;
import xyz.wagyourtail.commons.parsers.impl.constant.number.*;

import java.util.ArrayList;
import java.util.List;

public class NumberConstant extends StringData.OnlyRaw<Data.ListContent> {

    private NumberConstant(String rawContent) {
        super(rawContent, NumberConstant::getContentChecked);
    }

    public NumberConstant(CharReader<?> reader) {
        super(reader, NumberConstant::getContentChecked);
    }

    public static NumberConstant parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new NumberConstant(reader);
        reader.expectEOS();
        return number;
    }

    public static NumberConstant unchecked(String rawContent) {
        return new NumberConstant(rawContent);
    }

    private static ListContent getContentChecked(CharReader<?> reader) {
        List<Object> content = new ArrayList<>();

        val first = reader.peek();
        if (first == '-') {
            content.add((char) reader.take());
        }
        var next = reader.peek();
        if (next > '0' && next <= '9') {
            content.add(new WholePart(reader));
            next = reader.peek();
            if (next == 'l' || next == 'L') {
                content.add((char) reader.take());
                return new ListContent(content);
            }
            if (next == '.') {
                content.add((char) reader.take());
                next = reader.peek();
                if (next >= '0' && next <= '9') {
                    content.add(new DecimalPart(reader));
                }
                next = reader.peek();
            }
            if (next == 'e' || next == 'E') {
                content.add((char) reader.take());
                content.add(new ExponentPart(reader));
                next = reader.peek();
            }
        } else if (next == '0') {
            content.add((char) reader.take());
            next = reader.peek();
            switch (next) {
                case '.':
                    content.add((char) reader.take());
                    next = reader.peek();
                    if (next >= '0' && next <= '9') {
                        content.add(new DecimalPart(reader));
                    }
                    next = reader.peek();
                    if (next == 'e' || next == 'E') {
                        content.add((char) reader.take());
                        content.add(new ExponentPart(reader));
                        next = reader.peek();
                    }
                    break;
                case 'x':
                case 'X':
                    content.add((char) reader.take());
                    content.add(new HexPart(reader));
                    next = reader.peek();
                    if (next == 'l' || next == 'L') {
                        content.add((char) reader.take());
                    }
                    return new ListContent(content);
                case 'b':
                case 'B':
                    content.add((char) reader.take());
                    content.add(new BinaryPart(reader));
                    next = reader.peek();
                    if (next == 'l' || next == 'L') {
                        content.add((char) reader.take());
                    }
                    return new ListContent(content);
                case -1:
                    break;
                default:
                    if (next >= '0' && next < '8') {
                        content.add(new OctalPart(reader));
                        next = reader.peek();
                        if (next == 'l' || next == 'L') {
                            content.add((char) reader.take());
                        }
                        return new ListContent(content);
                    }
            }
        } else if (next == '.') {
            content.add((char) reader.take());
            content.add(new DecimalPart(reader));
            next = reader.peek();
            if (next == 'e' || next == 'E') {
                content.add((char) reader.take());
                content.add(new ExponentPart(reader));
                next = reader.peek();
            }
        } else if (next == 'I') {
            content.add(reader.expect("Infinity"));
            next = reader.peek();
        } else if (next == 'N') {
            content.add(reader.expect("NaN"));
            next = reader.peek();
        } else {
            throw reader.createException("Not a number character: " + (char) next);
        }

        if (next == -1) return new ListContent(content);
        if (next == 'f' || next == 'F'
                || next == 'd' || next == 'D') {
            content.add((char) reader.take());
        }

        return new ListContent(content);
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
        val positive = asPositive().getRawContent();

        if (positive.length() == 1) {
            return true;
        }

        if (positive.charAt(0) == '0') {
            return false;
        }

        val last = positive.charAt(positive.length() - 1);
        if (last == 'd' || last == 'D' || last == 'f' || last == 'F') {
            return false;
        }

        return !positive.contains(".") && !positive.contains("e") && !positive.contains("E");
    }

    public boolean isDecimal() {
        val positive = asPositive().getRawContent();
        val last = positive.charAt(positive.length() - 1);

        if (positive.charAt(0) == '0') {
            if (positive.length() == 1) {
                return false;
            }

            val next = positive.charAt(1);
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
        val positive = asPositive().getRawContent();
        return positive.startsWith("0x") || positive.startsWith("0X");
    }

    public boolean isBinary() {
        val positive = asPositive().getRawContent();
        return positive.startsWith("0b") || positive.startsWith("0B");
    }

    public boolean isOctal() {
        val positive = asPositive().getRawContent();

        if (positive.length() == 1) {
            return false;
        }

        val second = positive.charAt(1);
        return positive.charAt(0) == '0' && (second >= '0' && second <= '7');
    }

    public boolean isFloat() {
        val raw = getRawContent();
        val last = raw.charAt(raw.length() - 1);
        return (last == 'f' || last == 'F') && !isHex();
    }

    public boolean isLong() {
        val raw = getRawContent();
        val last = raw.charAt(raw.length() - 1);
        return last == 'l' || last == 'L';
    }

    public boolean isDouble() {
        val positive = asPositive().getRawContent();

        if (positive.startsWith("I") || positive.startsWith("N")) {
            return true;
        }

        if (isDecimal()) {
            val last = positive.charAt(positive.length() - 1);
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
        val raw = getRawContent();
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
            return Long.parseLong(raw.substring(raw.length() - 1));
        }
        if (isDouble()) {
            val last = raw.charAt(raw.length() - 1);
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
}
