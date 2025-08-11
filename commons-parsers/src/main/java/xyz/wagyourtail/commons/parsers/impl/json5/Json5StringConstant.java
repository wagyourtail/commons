package xyz.wagyourtail.commons.parsers.impl.json5;

import lombok.val;
import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

public class Json5StringConstant extends StringData.OnlyRaw<Data.SingleContent<String>> {


    private Json5StringConstant(String content) {
        super(content, Json5StringConstant::getContentChecked);
    }

    public Json5StringConstant(CharReader<?> content) {
        super(content, Json5StringConstant::getContentChecked);
    }

    public static Json5StringConstant parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val number = new Json5StringConstant(reader);
        reader.expectEOS();
        return number;
    }

    public static Json5StringConstant unchecked(String rawContent) {
        return new Json5StringConstant(rawContent);
    }

    private static SingleContent<String> getContentChecked(CharReader<?> reader) {
        int quote = reader.peek();
        if (quote == '"' || quote == '\'') {
            return new SingleContent<>(
                    reader.takeString(CharReader.TAKE_STRING_NO_TRANSLATE_ESCAPES | CharReader.TAKE_STRING_LEINIENT | CharReader.TAKE_STRING_ESCAPE_NEWLINE, String.valueOf((char) quote))
            );
        }
        throw reader.createException("Expected string start char but got: " + (char) quote);
    }

    public String getValue() {
        val content = getRawContent();
        return StringUtils.translateEscapes(content.substring(1, content.length() - 1).replace("\\\n", ""));
    }

}
