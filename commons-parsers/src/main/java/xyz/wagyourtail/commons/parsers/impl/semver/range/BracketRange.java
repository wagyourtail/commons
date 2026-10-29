package xyz.wagyourtail.commons.parsers.impl.semver.range;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.ArrayList;
import java.util.List;

public class BracketRange extends RangeData {

    protected BracketRange(String rawContent) {
        super(rawContent);
    }

    private BracketRange(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public BracketRange(BracketContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<BracketRange> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c == '(' || c == '[';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(reader.expect("open bracket", c -> c == '(' || c == '['));
            append.append(reader.takeNonNewlineWhitespace());
            append.append(ExactVersionRange.COMPANION.read(reader));
            append.append(reader.takeNonNewlineWhitespace());
            append.append(reader.expect(','));
            append.append(reader.takeNonNewlineWhitespace());
            int end = reader.peek();
            if (end == ')' || end == ']') {
                append.append((char) reader.take());
                return;
            }
            append.append(ExactVersionRange.COMPANION.read(reader));
            append.append(reader.takeNonNewlineWhitespace());
            append.append(reader.expect("close bracket", c -> c == ')' || c == ']'));
        }

        @Override
        public BracketRange unchecked(String value) {
            return new BracketRange(value);
        }

        @Override
        public BracketRange uncheckedView(CharSequence buffer, int start, int end) {
            return new BracketRange(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public BracketRange parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public BracketRange parse(String raw) {
            return checked(raw);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static BracketRange parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static BracketRange parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
    }

    @Override
    public boolean contains(SemVer version) {
        BracketContent content = (BracketContent) getContent();
        int start = content.from.compare(version);
        boolean includeStart = content.includeLeft;
        if (start > 0 || (start == 0 && !includeStart)) return false;
        if (content.to == null) return true;
        int end = content.to.compare(version);
        boolean includeEnd = content.includeRight;
        return end < 0 || (end == 0 && includeEnd);
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
        List<Object> brackets = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof ExactVersionRange) ranges.add((ExactVersionRange) o);
            else if (o instanceof Character && ("()[]".indexOf((Character) o) >= 0)) brackets.add(o);
        }
        return new BracketContent(
            '[' == (Character) brackets.get(0),
            ranges.get(0),
            ranges.size() > 1 ? ranges.get(1) : null,
            ']' == (Character) brackets.get(brackets.size() - 1)
        );
    }

    public static class BracketContent extends Content {
        private final boolean includeLeft;
        private final ExactVersionRange from;
        private final ExactVersionRange to;
        private final boolean includeRight;

        public BracketContent(boolean includeLeft, ExactVersionRange from, ExactVersionRange to, boolean includeRight) {
            this.includeLeft = includeLeft;
            this.from = from;
            this.to = to;
            this.includeRight = includeRight;
        }

        public boolean isIncludeLeft() {
            return includeLeft;
        }

        public ExactVersionRange getFrom() {
            return from;
        }

        public ExactVersionRange getTo() {
            return to;
        }

        public boolean isIncludeRight() {
            return includeRight;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            entries.add(includeLeft ? '[' : '(');
            entries.add(from);
            entries.add(',');
            if (to != null) entries.add(to);
            entries.add(includeRight ? ']' : ')');
            return entries;
        }
    }

}
