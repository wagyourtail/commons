package xyz.wagyourtail.commons.parsers.impl.constant.parts;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.Collections;

public class OctalPart extends StringData.OnlyRaw {

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

    private static Content getContentChecked(CharReader<?> reader) {
        val first = reader.peek();
        if (first < '0' || first > '7') {
            throw reader.createException("Not an octal number: " + (char) first);
        }

        return new DefaultContent(Collections.singleton(reader.takeWhile(e -> e >= '0' && e <= '7')));
    }
}
