package xyz.wagyourtail.commonskt.parsers.impl.semver

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class VersionCore : RawStringData, Comparable<VersionCore> {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: VersionCoreContent) : super(content)

    companion object : StringDataCompanion<VersionCore>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return reader.peek()?.isDigit() == true
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(reader.takeWholeNumber())
            append(reader.expect('.'))
            append(reader.takeWholeNumber())
            append(reader.expect('.'))
            append(reader.takeWholeNumber())
        }

        override fun unchecked(value: String): VersionCore = VersionCore(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): VersionCore =
            VersionCore(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): VersionCore = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): VersionCore = checked(raw)

    }

    val major: Long get() = (content as VersionCoreContent).major
    val minor: Long get() = (content as VersionCoreContent).minor
    val patch: Long get() = (content as VersionCoreContent).patch

    override fun compareTo(other: VersionCore): Int {
        val c = major.compareTo(other.major)
        if (c != 0) return c
        val c2 = minor.compareTo(other.minor)
        if (c2 != 0) return c2
        return patch.compareTo(other.patch)
    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return VersionCoreContent(list[0] as Long, list[2] as Long, list[4] as Long)
    }

    class VersionCoreContent(
        val major: Long,
        val minor: Long,
        val patch: Long
    ) : Content() {

        override val entries: Iterable<Any>
            get() = listOf(major, '.', minor, '.', patch)

    }

}
