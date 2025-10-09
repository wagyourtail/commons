package xyz.wagyourtail.commons.parsers.impl.semver.range;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.ArrayList;
import java.util.List;

public class AndCombinedRange extends RangeData<Data.ListContentWithDelimiter<RangeData<?>>> {

    public AndCombinedRange(ListContentWithDelimiter<RangeData<?>> content) {
        super(content);
    }

    public static AndCombinedRange parse(String rawContent) {
        val reader = new StringCharReader(rawContent);
        val content = contentBuilder(reader);
        reader.expectEOS();
        return new AndCombinedRange(content);
    }

    public static AndCombinedRange parse(CharReader<?> rawContent) {
        return new AndCombinedRange(contentBuilder(rawContent));
    }

    public static ListContentWithDelimiter<RangeData<?>> contentBuilder(CharReader<?> reader) {
        List<RangeData<?>> entries = new ArrayList<>();
        while (!reader.exhausted()) {
            reader.takeNonNewlineWhitespace();
            val entry = reader.parseOrNull(
                PrefixedRange::parse,
                HyphenRange::parse,
                BracketRange::parse
            );
            if (entry == null) break;
            entries.add(entry);
        }
        return new ListContentWithDelimiter<>(entries, " ");
    }

    @Override
    public boolean contains(SemVer version) {
        for (val entry : getContent().getEntriesWithoutDelimiters()) {
            if (!entry.contains(version)) return false;
        }
        return true;
    }

}
