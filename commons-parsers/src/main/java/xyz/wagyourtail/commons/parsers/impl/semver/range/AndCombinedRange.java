package xyz.wagyourtail.commons.parsers.impl.semver.range;

import lombok.val;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

public class AndCombinedRange extends RangeData<Data.ListContent<RangeData<?>>> {

    public AndCombinedRange(ListContent<RangeData<?>> content) {
        super(content);
    }

    @Override
    public boolean contains(SemVer version) {
        for (val entry : getContent().getEntries()) {
            if (!entry.contains(version)) return false;
        }
        return true;
    }

}
