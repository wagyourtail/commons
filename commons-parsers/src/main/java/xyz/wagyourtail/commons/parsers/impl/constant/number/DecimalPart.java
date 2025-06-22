package xyz.wagyourtail.commons.parsers.impl.constant.number;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

public class DecimalPart extends StringData.OnlyRaw<Data.SingleContent<String>> {

    private DecimalPart(String content) {
        super(content, DecimalPart::getContentChecked);
    }

    public DecimalPart(CharReader<?> content) {
        super(content, DecimalPart::getContentChecked);
    }

    public static DecimalPart parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new DecimalPart(reader);
        reader.expectEOS();
        return number;
    }

    public static DecimalPart unchecked(String rawContent) {
        return new DecimalPart(rawContent);
    }

    private static SingleContent<String> getContentChecked(CharReader<?> reader) {
        val first = reader.peek();
        if (first < '0' || first > '9') {
            throw reader.createException("Not a decimal number: " + (char) first);
        }

        return new SingleContent<>(reader.takeWhile(e -> e >= '0' && e <= '9'));
    }
}
