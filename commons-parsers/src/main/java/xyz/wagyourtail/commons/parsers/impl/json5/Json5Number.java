package xyz.wagyourtail.commons.parsers.impl.json5;

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

public class Json5Number extends StringData.OnlyRaw<Data.ListContent> {

    private Json5Number(String rawContent) {
        super(rawContent, Json5Number::getContentChecked);
    }

    public Json5Number(CharReader<?> reader) {
        super(reader, Json5Number::getContentChecked);
    }

    public static Json5Number parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new Json5Number(reader);
        reader.expectEOS();
        return number;
    }

    public static Json5Number unchecked(String rawContent) {
        return new Json5Number(rawContent);
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
        } else if (raw.charAt(0) != '+') {
            return unchecked("-" + raw.substring(1));
        } else {
            return unchecked("-" + raw);
        }
    }

    public boolean isWhole() {
        var positive = asPositive().getRawContent();

        if (positive.startsWith("+")) {
            positive = positive.substring(1);
        }

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
        val raw = getRawContent();

        if (raw.contains(".") || raw.contains("e") || raw.contains("E")) {
            return true;
        }

        var positive = asPositive().getRawContent();
        if (positive.startsWith("+")) {
            positive = positive.substring(1);
        }

        return positive.startsWith("I") || positive.startsWith("N");
    }

    public boolean isHex() {
        var positive = asPositive().getRawContent();
        if (positive.startsWith("+")) {
            positive = positive.substring(1);
        }
        return positive.startsWith("0x") || positive.startsWith("0X");
    }

    public boolean isBinary() {
        var positive = asPositive().getRawContent();
        if (positive.startsWith("+")) {
            positive = positive.substring(1);
        }
        return positive.startsWith("0b") || positive.startsWith("0B");
    }

    public Number getValue() {
        if (isNegative()) {
            return NumberUtils.negate(asPositive().getValue());
        }
        var raw = getRawContent();
        if (raw.startsWith("+")) {
            raw = raw.substring(1);
        }
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

    private static ListContent getContentChecked(CharReader<?> reader) {
        List<Object> content = new ArrayList<>();

        val first = reader.peek();
        if (first == '-' || first == '+') {
            content.add((char) reader.take());
        }

        var next = reader.peek();
        if (next > '0' && next <= '9') {
            content.add(new DecimalPart(reader));
            next = reader.peek();
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
                    }
                    break;
                case 'x':
                case 'X':
                    content.add((char) reader.take());
                    content.add(new HexPart(reader));
                    return new ListContent(content);
                case 'b':
                case 'B':
                    content.add((char) reader.take());
                    content.add(new BinaryPart(reader));
                    return new ListContent(content);
                case -1:
                default:
                    break;
            }
        } else if (next == '.') {
            content.add((char) reader.take());
            content.add(new DecimalPart(reader));
            next = reader.peek();
            if (next == 'e' || next == 'E') {
                content.add((char) reader.take());
                content.add(new ExponentPart(reader));
            }
        } else if (next == 'I') {
            content.add(reader.expect("Infinity"));
        } else if (next == 'N') {
            content.add(reader.expect("NaN"));
        } else {
            throw reader.createException("Not a number character: " + (char) next);
        }

        return new ListContent(content);
    }
}
