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

public class PreRelease extends StringData.OnlyParsed<Data.ListContentWithDelimiter<Object>> implements Comparable<PreRelease> {
    private static final CharAccepter IDENTIFIER_CHARS = CharAccepters.or(CharAccepters.ALPHANUMERIC, CharAccepters.of('-'));

    public PreRelease(Data.ListContentWithDelimiter<Object> content) {
        super(content);
    }

    public static PreRelease parse(String raw) {
        StringCharReader reader = new StringCharReader(raw);
        val content = getContentChecked(reader);
        reader.expectEOS();
        return new PreRelease(content);
    }

    public static PreRelease parse(CharReader<?> reader) {
        return new PreRelease(getContentChecked(reader));
    }

    private static Data.ListContentWithDelimiter<Object> getContentChecked(CharReader<?> reader) {
        List<Object> parts = new ArrayList<>();
        while (true) {
            parts.add(reader.parse(
                e -> {
                    String s = e.takeWhile(IDENTIFIER_CHARS);
                    // ensure one is non-digit
                    if (s.chars().allMatch(Character::isDigit)) {
                        throw e.createException("Numeric identifier can't have leading 0: ");
                    }
                    if (s.isEmpty()) {
                        throw e.createException("Empty identifier");
                    }
                    return s;
                },
                CharReader::takeWholeNumber
            ));
            if (reader.peek() == '.') reader.expect('.');
            else break;
        };
        return new Data.ListContentWithDelimiter<>(parts, '.');
    }

    @Override
    public int compareTo(PreRelease o) {
        val self = getContent().getEntriesWithoutDelimiters().iterator();
        val other = o.getContent().getEntriesWithoutDelimiters().iterator();
        while (self.hasNext() && other.hasNext()) {
            val selfNext = self.next();
            val otherNext = other.next();
            if (selfNext instanceof Integer ^ otherNext instanceof Integer) {
                return selfNext instanceof Integer ? -1 : 1;
            }
            int c;
            if (selfNext instanceof Integer) {
                c = Integer.compare((Integer) selfNext, (Integer) otherNext);
            } else {
                c = ((String) selfNext).compareTo((String) otherNext);
            }
            if (c != 0) return c;
        }
        if (self.hasNext() ^ other.hasNext()) {
            return self.hasNext() ? -1 : 1;
        }
        return 0;
    }
}
