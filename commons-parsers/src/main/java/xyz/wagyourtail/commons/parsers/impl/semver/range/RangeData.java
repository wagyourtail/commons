package xyz.wagyourtail.commons.parsers.impl.semver.range;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

public abstract class RangeData extends RawStringData {

    protected RangeData(String rawContent) {
        super(rawContent);
    }

    protected RangeData(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    protected RangeData(Content content) {
        super(content);
    }

    public abstract boolean contains(SemVer version);

}
