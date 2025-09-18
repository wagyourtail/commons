package xyz.wagyourtail.commons.parsers.impl.semver;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharAccepter;
import xyz.wagyourtail.commons.core.reader.CharAccepters;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.ArrayList;
import java.util.List;

public class Build extends StringData.OnlyParsed<Data.ListContentWithDelimiter<Object>> {
    private static final CharAccepter IDENTIFIER_CHARS = CharAccepters.or(CharAccepters.ALPHANUMERIC, CharAccepters.of('-'));

    public Build(ListContentWithDelimiter<Object> content) {
        super(content);
    }

    public static Build parse(String raw) {
        StringCharReader reader = new StringCharReader(raw);
        val content = getContentChecked(reader);
        reader.expectEOS();
        return new Build(content);
    }

    public static Build parse(CharReader<?> reader) {
        return new Build(getContentChecked(reader));
    }

    private static ListContentWithDelimiter<Object> getContentChecked(CharReader<?> reader) {
        List<Object> parts = new ArrayList<>();
        while (true) {
            parts.add(reader.parse(
                    e -> {
                        String s = e.takeWhile(IDENTIFIER_CHARS);
                        // ensure one is non-digit
                        if (s.chars().allMatch(Character::isDigit)) {
                            throw e.createException("Alphanumeric identifier can't have only digits");
                        }
                        if (s.isEmpty()) {
                            throw e.createException("Empty identifier");
                        }
                        return s;
                    },
                e -> e.takeWholeNumber(true)
            ));
            if (reader.peek() == '.') reader.expect('.');
            else break;
        }
        return new ListContentWithDelimiter<>(parts, '.');
    }

}
