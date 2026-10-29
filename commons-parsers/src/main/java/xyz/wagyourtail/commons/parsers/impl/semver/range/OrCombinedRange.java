package xyz.wagyourtail.commons.parsers.impl.semver.range;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.ArrayList;
import java.util.List;

public class OrCombinedRange extends RangeData {

    protected OrCombinedRange(String rawContent) {
        super(rawContent);
    }

    private OrCombinedRange(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public OrCombinedRange(Content.ListContentWithDelimiter content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<OrCombinedRange> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return AndCombinedRange.COMPANION.shouldRead(reader);
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            while (!reader.exhausted()) {
                append.append(reader.takeNonNewlineWhitespace());
                append.append(AndCombinedRange.COMPANION.read(reader));
                append.append(reader.takeNonNewlineWhitespace());
                if (reader.peek() == '|') {
                    append.append(reader.expect("||"));
                } else {
                    break;
                }
            }
        }

        @Override
        public OrCombinedRange unchecked(String value) {
            return new OrCombinedRange(value);
        }

        @Override
        public OrCombinedRange uncheckedView(CharSequence buffer, int start, int end) {
            return new OrCombinedRange(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public OrCombinedRange parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public OrCombinedRange parse(String raw) {
            return checked(raw);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static OrCombinedRange parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static OrCombinedRange parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
    }

    @Override
    public boolean contains(SemVer version) {
        for (Object entry : ((Content.ListContentWithDelimiter) getContent()).getEntriesWithoutDelimiters()) {
            if (((RangeData) entry).contains(version)) return true;
        }
        return false;
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
            if (o instanceof AndCombinedRange) entries.add(o);
        }
        return new Content.ListContentWithDelimiter(entries, " || ");
    }

}
