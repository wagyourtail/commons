package xyz.wagyourtail.commons.parsers.impl.semver.range;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.Arrays;

public class BracketRange extends RangeData<BracketRange.BracketContent> {

    public BracketRange(BracketContent content) {
        super(content);
    }

    public static BracketRange parse(String rawContent) {
        val reader = new StringCharReader(rawContent);
        val content = contentBuilder(reader);
        reader.expectEOS();
        return new BracketRange(content);
    }

    public static BracketRange parse(CharReader<?> reader) {
        return new BracketRange(contentBuilder(reader));
    }

    public static BracketContent contentBuilder(CharReader<?> reader) {
        val includeLeft = reader.expect("open bracket", c -> c == '(' || c == '[') == '[';
        reader.takeNonNewlineWhitespace();
        val from = ExactVersionRange.parse(reader);
        reader.takeNonNewlineWhitespace();
        reader.expect(',');
        reader.takeNonNewlineWhitespace();
        val end = reader.peek();
        if (end == ')' || end == ']') {
            return new BracketContent(includeLeft, from, null, end == ']');
        }
        val to = ExactVersionRange.parse(reader);
        reader.takeNonNewlineWhitespace();
        val includeRight = reader.expect("close bracket", c -> c == ')' || c == ']') == ']';
        return new BracketContent(includeLeft, from, to, includeRight);
    }

    @Override
    public boolean contains(SemVer version) {
        val content = getContent();
        val start = content.getFrom().compare(version);
        val includeStart = content.isIncludeLeft();
        if (start > 0 || (start == 0 && !includeStart)) return false;
        if (content.getTo() == null) return true;
        val end = content.getTo().compare(version);
        val includeEnd = content.isIncludeRight();
        return end < 0 || (end == 0 && includeEnd);
    }

    @Getter
    @AllArgsConstructor
    public static class BracketContent extends Data.Content<Object> {
        private final boolean includeLeft;
        private final ExactVersionRange from;
        private final ExactVersionRange to;
        private final boolean includeRight;

        @Override
        public Iterable<Object> getEntries() {
            return Arrays.asList(
                includeLeft ? '[' : '(',
                from,
                ',',
                to,
                includeRight ? ']' : ')'
            );
        }
    }
}
