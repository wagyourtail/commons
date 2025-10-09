package xyz.wagyourtail.commons.parsers.impl;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.semver.range.OrCombinedRange;
import xyz.wagyourtail.commons.parsers.impl.semver.range.RangeData;

public class SemVerRange extends RangeData<Data.SingleContent<RangeData<?>>> {

    public SemVerRange(SingleContent<RangeData<?>> content) {
        super(content);
    }

    public static SemVerRange parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val content = contentBuilder(reader);
        reader.expectEOS();
        return new SemVerRange(content);
    }

    public static SemVerRange parse(CharReader<?> reader) {
        return new SemVerRange(contentBuilder(reader));
    }

    private static Data.SingleContent<RangeData<?>> contentBuilder(CharReader<?> reader) {
        return new Data.SingleContent<>(OrCombinedRange.parse(reader));
    }

    @Override
    public boolean contains(SemVer version) {
        return getContent().getValue().contains(version);
    }
}
