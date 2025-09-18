package xyz.wagyourtail.commons.parsers.impl.semver.range;

import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

public abstract class RangeData<T extends Data.Content<?>> extends StringData.OnlyParsed<T> {

    public RangeData(T content) {
        super(content);
    }

    public abstract boolean contains(SemVer version);
}
