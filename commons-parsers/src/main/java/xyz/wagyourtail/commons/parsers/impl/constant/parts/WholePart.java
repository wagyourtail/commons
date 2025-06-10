package xyz.wagyourtail.commons.parsers.impl.constant.parts;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.Collections;

public class WholePart extends StringData.OnlyRaw {

    public WholePart(String rawContent) {
        super(rawContent, WholePart::getContentChecked);
    }

    public WholePart(CharReader<?> reader) {
        super(reader, WholePart::getContentChecked);
    }

    public static WholePart parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new WholePart(reader);
        reader.expectEOS();
        return number;
    }

    public static WholePart unchecked(String rawContent) {
        return new WholePart(rawContent);
    }

    private static Content getContentChecked(CharReader<?> reader) {
        val first = reader.peek();
        if (first == '0') {
            throw reader.createException("Whole number cannot start with 0");
        }
        if (first < '0' || first > '9') {
            throw reader.createException("Not a whole number: " + (char) first);
        }

        return new DefaultContent(Collections.singleton(reader.takeWhile(e -> e >= '0' && e <= '9')));
    }

}
