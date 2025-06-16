package xyz.wagyourtail.commons.parsers.impl.constant.parts;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.Collections;

public class HexPart extends StringData.OnlyRaw<Data.SingleContent<String>> {

    private HexPart(String rawContent) {
        super(rawContent, HexPart::getContentChecked);
    }

    public HexPart(CharReader<?> reader) {
        super(reader, HexPart::getContentChecked);
    }

    public static HexPart parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new HexPart(reader);
        reader.expectEOS();
        return number;
    }

    public static HexPart unchecked(String rawContent) {
        return new HexPart(rawContent);
    }

    private static SingleContent<String> getContentChecked(CharReader<?> reader) {
        val first = reader.peek();

        if ((first >= '0' && first <= '9') || (first >= 'a' && first <= 'f') || (first >= 'A' && first <= 'F')) {
            return new SingleContent<>(reader.takeWhile(e -> (e >= '0' && e <= '9') || (e >= 'a' && e <= 'f') || (e >= 'A' && e <= 'F')));
        }
        throw reader.createException("Not a hex number: " + (char) first);
    }
}
