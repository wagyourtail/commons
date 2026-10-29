package xyz.wagyourtail.commonskt.parsers.impl.semver.range

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.parsers.impl.SemVer

abstract class RangeData : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    protected constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content)

    abstract fun contains(version: SemVer): Boolean

}
