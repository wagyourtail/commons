package xyz.wagyourtail.commons.parsers.impl.constant.number;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

public class OctalPart extends StringData.OnlyRaw<Data.SingleContent<String>> {

    public OctalPart(String rawContent) {
        super(rawContent, OctalPart::getContentChecked);
    }

    public OctalPart(CharReader<?> reader) {
        super(reader, OctalPart::getContentChecked);
    }

    public static OctalPart parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new OctalPart(reader);
        reader.expectEOS();
        return number;
    }

    public static OctalPart unchecked(String rawContent) {
        return new OctalPart(rawContent);
    }

    private static SingleContent<String> getContentChecked(CharReader<?> reader) {
        val first = reader.peek();
        if (first < '0' || first > '7') {
            throw reader.createException("Not an octal number: " + (char) first);
        }

        return new SingleContent<>(reader.takeWhile(e -> e >= '0' && e <= '7'));
    }
}
