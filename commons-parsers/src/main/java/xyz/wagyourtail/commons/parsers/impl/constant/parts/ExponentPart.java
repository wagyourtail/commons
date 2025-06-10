package xyz.wagyourtail.commons.parsers.impl.constant.parts;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.LinkedHashSet;
import java.util.Set;

public class ExponentPart extends StringData.OnlyRaw {

    public ExponentPart(String rawContent) {
        super(rawContent, ExponentPart::getContentChecked);
    }

    public ExponentPart(CharReader<?> reader) {
        super(reader, ExponentPart::getContentChecked);
    }

    public static ExponentPart parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new ExponentPart(reader);
        reader.expectEOS();
        return number;
    }

    public static ExponentPart unchecked(String rawContent) {
        return new ExponentPart(rawContent);
    }

    private static Content getContentChecked(CharReader<?> reader) {
        Set<Object> content = new LinkedHashSet<>(2);

        val first = reader.peek();
        if (first == '-' || first == '+') {
            content.add((char) reader.take());
        }

        content.add(new DecimalPart(reader));

        return new DefaultContent(content);
    }
}
