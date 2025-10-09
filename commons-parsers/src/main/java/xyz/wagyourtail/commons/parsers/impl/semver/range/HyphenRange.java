package xyz.wagyourtail.commons.parsers.impl.semver.range;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.Arrays;

public class HyphenRange extends RangeData<HyphenRange.HyphenContent> {
    public HyphenRange(HyphenContent content) {
        super(content);
    }

    public static HyphenRange parse(String raw) {
        val reader = new StringCharReader(raw);
        val content = contentBuilder(reader);
        reader.expectEOS();
        return new HyphenRange(content);
    }

    public static HyphenRange parse(CharReader<?> reader) {
        return new HyphenRange(contentBuilder(reader));
    }

    public static HyphenContent contentBuilder(CharReader<?> reader) {
        val start = ExactVersionRange.parse(reader);
        val end = reader.parseOrNull(r -> {
            r.takeNonNewlineWhitespace();
            r.expect('-');
            r.takeNonNewlineWhitespace();
            return ExactVersionRange.parse(r);
        });
        return new HyphenContent(start, end == null ? start : end);
    }

    @Override
    public boolean contains(SemVer version) {
        // start <= version <= end
        val content = getContent();
        val start = content.getStart().compare(version);
        val end = content.getEnd().compare(version);
        return start >= 0 && end <= 0;
    }

    @Getter
    @AllArgsConstructor
    public static class HyphenContent extends Data.Content<Object> {
        private final ExactVersionRange start;
        private final ExactVersionRange end;

        @Override
        public Iterable<Object> getEntries() {
            return Arrays.asList(
                    start,
                    " - ",
                    end
            );
        }
    }
}
