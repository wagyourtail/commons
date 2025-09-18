package xyz.wagyourtail.commons.parsers.impl.semver.range;

import lombok.val;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

public class OrCombinedRange extends RangeData<Data.ListContentWithDelimiter<RangeData<?>>> {

    public OrCombinedRange(ListContentWithDelimiter<RangeData<?>> content) {
        super(content);
    }

    @Override
    public boolean contains(SemVer version) {
        for (val entry : getContent().getEntriesWithoutDelimiters()) {
            if (entry.contains(version)) return true;
        }
        return false;
    }
}
