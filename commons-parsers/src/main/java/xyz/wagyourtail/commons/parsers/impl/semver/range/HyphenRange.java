package xyz.wagyourtail.commons.parsers.impl.semver.range;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.ArrayList;
import java.util.List;

public class HyphenRange extends RangeData {

    protected HyphenRange(String rawContent) {
        super(rawContent);
    }

    private HyphenRange(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public HyphenRange(HyphenContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<HyphenRange> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return ExactVersionRange.COMPANION.shouldRead(reader);
        }

        @Override
        public void read(final CharReader<?> reader, final Append append) {
            append.append(ExactVersionRange.COMPANION.read(reader));
            reader.parseOrNull((r) -> {
                append.append(r.takeNonNewlineWhitespace());
                append.append(r.expect('-'));
                append.append(r.takeNonNewlineWhitespace());
                append.append(ExactVersionRange.COMPANION.read(r));
                return null;
            });
        }

        @Override
        public HyphenRange unchecked(String value) {
            return new HyphenRange(value);
        }

        @Override
        public HyphenRange uncheckedView(CharSequence buffer, int start, int end) {
            return new HyphenRange(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public HyphenRange parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public HyphenRange parse(String raw) {
            return checked(raw);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static HyphenRange parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static HyphenRange parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
    }

    @Override
    public boolean contains(SemVer version) {
        // start <= version <= end
        HyphenContent content = (HyphenContent) getContent();
        int start = content.start.compare(version);
        int end = content.end.compare(version);
        return start >= 0 && end <= 0;
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
        List<ExactVersionRange> ranges = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof ExactVersionRange) ranges.add((ExactVersionRange) o);
        }
        ExactVersionRange start = ranges.get(0);
        ExactVersionRange end = ranges.size() > 1 ? ranges.get(1) : start;
        return new HyphenContent(start, end);
    }

    public static class HyphenContent extends Content {
        private final ExactVersionRange start;
        private final ExactVersionRange end;

        public HyphenContent(ExactVersionRange start, ExactVersionRange end) {
            this.start = start;
            this.end = end;
        }

        public ExactVersionRange getStart() {
            return start;
        }

        public ExactVersionRange getEnd() {
            return end;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            entries.add(start);
            if (end != start) {
                entries.add(" - ");
                entries.add(end);
            }
            return entries;
        }
    }

}
