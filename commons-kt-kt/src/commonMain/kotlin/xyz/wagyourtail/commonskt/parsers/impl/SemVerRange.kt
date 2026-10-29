package xyz.wagyourtail.commonskt.parsers.impl

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.SingleContent
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.semver.range.OrCombinedRange
import xyz.wagyourtail.commonskt.parsers.impl.semver.range.RangeData
import xyz.wagyourtail.commonskt.reader.CharReader

class SemVerRange : RangeData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: SingleContent) : super(content)

    companion object : StringDataCompanion<SemVerRange>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return reader.peek() != null
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(OrCombinedRange.read(reader))
        }

        override fun unchecked(value: String): SemVerRange = SemVerRange(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): SemVerRange =
            SemVerRange(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): SemVerRange = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): SemVerRange = checked(raw)

    }

    override fun contains(version: SemVer): Boolean {
        return ((content as SingleContent).value as RangeData).contains(version)
    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return SingleContent(list.first())
    }

}
