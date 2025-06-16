package xyz.wagyourtail.commons.parsers.impl.constant.parts;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.ArrayList;
import java.util.List;

public class ExponentPart extends StringData.OnlyRaw<Data.ListContent> {

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

    private static ListContent getContentChecked(CharReader<?> reader) {
        List<Object> content = new ArrayList<>(2);

        val first = reader.peek();
        if (first == '-' || first == '+') {
            content.add((char) reader.take());
        }

        content.add(new DecimalPart(reader));

        return new ListContent(content);
    }
}
