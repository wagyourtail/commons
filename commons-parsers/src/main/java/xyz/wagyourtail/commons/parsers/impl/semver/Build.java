package xyz.wagyourtail.commons.parsers.impl.semver;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharAccepter;
import xyz.wagyourtail.commons.core.reader.CharAccepters;
import xyz.wagyourtail.commons.core.reader.CharReader;

import java.util.ArrayList;
import java.util.List;

public class Build extends RawStringData {
    private static final CharAccepter IDENTIFIER_CHARS = CharAccepters.or(CharAccepters.ALPHANUMERIC, CharAccepters.of('-'));

    protected Build(String rawContent) {
        super(rawContent);
    }

    private Build(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Build(Content.ListContentWithDelimiter content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Build> {

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
                                throw e.createException("Alphanumeric identifier can't have only digits");
                            }
                            if (s.isEmpty()) {
                                throw e.createException("Empty identifier");
                            }
                            return s;
                        },
                        (e) -> e.takeWholeNumber(true)
                ));
                if (reader.peek() != '.') break;
            }
        }

        @Override
        public Build unchecked(String value) {
            return new Build(value);
        }

        @Override
        public Build uncheckedView(CharSequence buffer, int start, int end) {
            return new Build(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public Build parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public Build parse(String raw) {
            return checked(raw);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Build parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static Build parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
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
