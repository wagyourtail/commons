package xyz.wagyourtail.commons.parsers.impl.semver;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharAccepter;
import xyz.wagyourtail.commons.core.reader.CharAccepters;
import xyz.wagyourtail.commons.core.reader.CharReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PreRelease extends RawStringData implements Comparable<PreRelease> {
    private static final CharAccepter IDENTIFIER_CHARS = CharAccepters.or(CharAccepters.ALPHANUMERIC, CharAccepters.of('-'));

    protected PreRelease(String rawContent) {
        super(rawContent);
    }

    private PreRelease(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public PreRelease(Content.ListContentWithDelimiter content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<PreRelease> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return IDENTIFIER_CHARS.accept((char) reader.peek());
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            boolean first = true;
            while (true) {
                if (first) first = false;
                else append.append(reader.expect('.'));
                append.append(reader.parse(
                        (e) -> {
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
                if (reader.peek() != '.') break;
            }
        }

        @Override
        public PreRelease unchecked(String value) {
            return new PreRelease(value);
        }

        @Override
        public PreRelease uncheckedView(CharSequence buffer, int start, int end) {
            return new PreRelease(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public PreRelease parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public PreRelease parse(String raw) {
            return checked(raw);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static PreRelease parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static PreRelease parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
    }

    @Override
    public int compareTo(PreRelease o) {
        Iterator<Object> self = ((Content.ListContentWithDelimiter) getContent()).getEntriesWithoutDelimiters().iterator();
        Iterator<Object> other = ((Content.ListContentWithDelimiter) o.getContent()).getEntriesWithoutDelimiters().iterator();
        while (self.hasNext() && other.hasNext()) {
            Object selfNext = self.next();
            Object otherNext = other.next();
            if ((selfNext instanceof Long) != (otherNext instanceof Long)) {
                return selfNext instanceof Long ? -1 : 1;
            }
            int c;
            if (selfNext instanceof Long) {
                c = Long.compare((Long) selfNext, (Long) otherNext);
            } else {
                c = ((String) selfNext).compareTo((String) otherNext);
            }
            if (c != 0) return c;
        }
        if (self.hasNext() != other.hasNext()) {
            return self.hasNext() ? -1 : 1;
        }
        return 0;
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        final List<Object> list = new ArrayList<>();
        COMPANION.read(reader, new xyz.wagyourtail.commons.core.data.DataCompanion.Append() {
            @Override
            public void append(Object o) {
                list.add(o);
            }
        });
        List<Object> parts = new ArrayList<>();
        for (Object o : list) {
            if (!".".equals(String.valueOf(o))) parts.add(o);
        }
        return new Content.ListContentWithDelimiter(parts, '.');
    }

}
