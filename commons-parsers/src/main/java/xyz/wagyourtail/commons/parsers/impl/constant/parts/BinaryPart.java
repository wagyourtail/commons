package xyz.wagyourtail.commons.parsers.impl.constant.parts;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

public class BinaryPart extends StringData.OnlyRaw<Data.SingleContent<String>> {

    private BinaryPart(String content) {
        super(content, BinaryPart::getContentChecked);
    }

    public BinaryPart(CharReader<?> reader) {
        super(reader, BinaryPart::getContentChecked);
    }

    public static BinaryPart parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new BinaryPart(reader);
        reader.expectEOS();
        return number;
    }

    public static BinaryPart unchecked(String rawContent) {
        return new BinaryPart(rawContent);
    }

    private static SingleContent<String> getContentChecked(CharReader<?> reader) {
        val first = reader.peek();
        if (first != '0' && first != '1') {
            throw reader.createException("Not a binary number: " + (char) first);
        }

        return new SingleContent<>(reader.takeWhile(e -> e == '0' || e == '1'));
    }

}
