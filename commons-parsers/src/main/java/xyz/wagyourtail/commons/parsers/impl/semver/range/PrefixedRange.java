package xyz.wagyourtail.commons.parsers.impl.semver.range;

import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

public class PrefixedRange extends RangeData<Data.Content<Object>> {

    public PrefixedRange(Content content) {
        super(content);
    }

    @Override
    public boolean contains(SemVer version) {
        return false;
    }

    public static class PrefixContent {

    }

}
