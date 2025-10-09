package xyz.wagyourtail.commons.parsers.impl.semver.range;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.ArrayList;
import java.util.List;

public class OrCombinedRange extends RangeData<Data.ListContentWithDelimiter<RangeData<?>>> {

    public OrCombinedRange(ListContentWithDelimiter<RangeData<?>> content) {
        super(content);
    }

    public static OrCombinedRange parse(String rawContent) {
        val reader = new StringCharReader(rawContent);
        val content = contentBuilder(reader);
        reader.expectEOS();
        return new OrCombinedRange(content);
    }

    public static OrCombinedRange parse(CharReader<?> reader) {
        return new OrCombinedRange(contentBuilder(reader));
    }

    public static ListContentWithDelimiter<RangeData<?>> contentBuilder(CharReader<?> reader) {
        List<RangeData<?>> entries = new ArrayList<>();
        while (!reader.exhausted()) {
            reader.takeNonNewlineWhitespace();
            val entry = AndCombinedRange.parse(reader);
            entries.add(entry);
            reader.takeNonNewlineWhitespace();
            if (reader.peek() == '|') {
                reader.expect("||");
            } else {
                break;
            }
        }
        return new ListContentWithDelimiter<>(entries, " || ");
    }

    @Override
    public boolean contains(SemVer version) {
        for (val entry : getContent().getEntriesWithoutDelimiters()) {
            if (entry.contains(version)) return true;
        }
        return false;
    }
}
