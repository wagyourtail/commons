package xyz.wagyourtail.commons.parsers.impl.semver.range;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.ArrayList;
import java.util.List;

public class AndCombinedRange extends RangeData {

    protected AndCombinedRange(String rawContent) {
        super(rawContent);
    }

    private AndCombinedRange(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public AndCombinedRange(Content.ListContentWithDelimiter content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<AndCombinedRange> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return PrefixedRange.COMPANION.shouldRead(reader) ||
                HyphenRange.COMPANION.shouldRead(reader) ||
                BracketRange.COMPANION.shouldRead(reader);
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            boolean first = true;
            while (!reader.exhausted()) {
                String ws = reader.takeNonNewlineWhitespace();
                Object entry = reader.parseOrNull(
                        PrefixedRange.COMPANION::read,
                        HyphenRange.COMPANION::read,
                        BracketRange.COMPANION::read
                );
                if (entry == null) {
                    if (!first && !ws.isEmpty()) append.append(ws);
                    break;
                }
                if (!first) append.append(ws);
                append.append(entry);
                first = false;
            }
        }

        @Override
        public AndCombinedRange unchecked(String value) {
            return new AndCombinedRange(value);
        }

        @Override
        public AndCombinedRange uncheckedView(CharSequence buffer, int start, int end) {
            return new AndCombinedRange(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public AndCombinedRange parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public AndCombinedRange parse(String raw) {
            return checked(raw);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static AndCombinedRange parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static AndCombinedRange parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
    }

    @Override
    public boolean contains(SemVer version) {
        for (Object entry : ((Content.ListContentWithDelimiter) getContent()).getEntriesWithoutDelimiters()) {
            if (!((RangeData) entry).contains(version)) return false;
        }
        return true;
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        final List<Object> list = new ArrayList<>();
        COMPANION.read(reader, new DataCompanion.Append() {
            @Override
            public void append(Object o) {
                list.add(o);
            }
        });
        List<Object> entries = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof RangeData) entries.add(o);
        }
        return new Content.ListContentWithDelimiter(entries, " ");
    }

}
