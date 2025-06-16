package xyz.wagyourtail.commons.parsers.impl.json5;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

public class Json5Whitespace extends StringData.OnlyRaw<Data.SingleContent<String>> {

    public Json5Whitespace(String rawContent) {
        super(rawContent, Json5Whitespace::getContentChecked);
    }

    public Json5Whitespace(CharReader<?> reader) {
        super(reader, Json5Whitespace::getContentChecked);
    }

    public static Json5Whitespace parse(String reader) {
        StringCharReader charReader = new StringCharReader(reader);
        val content = new Json5Whitespace(charReader);
        charReader.expectEOS();
        return content;
    }

    public static Json5Whitespace unchecked(String rawContent) {
        return new Json5Whitespace(rawContent);
    }

    public static Data.SingleContent<String> getContentChecked(CharReader<?> reader) {
        val first = reader.peek();
        if (!Character.isWhitespace(first)) {
            throw reader.createException("Expected whitespace but got '" + (char) first + "'");
        }
        return new Data.SingleContent<>(reader.takeWhile(Character::isWhitespace));
    }

}
