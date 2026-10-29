package xyz.wagyourtail.commonskt.parsers.impl

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.semver.Build
import xyz.wagyourtail.commonskt.parsers.impl.semver.PreRelease
import xyz.wagyourtail.commonskt.parsers.impl.semver.VersionCore
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class SemVer : RawStringData, Comparable<SemVer> {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: SemVerContent) : super(content)

    companion object : StringDataCompanion<SemVer>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return reader.peek()?.isDigit() == true
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(VersionCore.read(reader))
            if (reader.peek() == '-') {
                append(reader.take()!!)
                append(PreRelease.read(reader))
            }
            if (reader.peek() == '+') {
                append(reader.take()!!)
                append(Build.read(reader))
            }
        }

        override fun unchecked(value: String): SemVer = SemVer(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): SemVer =
            SemVer(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): SemVer = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): SemVer = checked(raw)

    }

    val core: VersionCore get() = (content as SemVerContent).core
    val preRelease: PreRelease? get() = (content as SemVerContent).preRelease
    val build: Build? get() = (content as SemVerContent).build

    override fun compareTo(other: SemVer): Int {
        val c = core.compareTo(other.core)
        if (c != 0) return c
        if ((preRelease != null) != (other.preRelease != null)) {
            // NOTE: mirrors the java impl — a version without a pre-release
            // sorts before one with (spec-inverted, preserved intentionally)
            return if (preRelease == null) -1 else 1
        }
        if (preRelease != null) {
            return preRelease!!.compareTo(other.preRelease!!)
        }
        return 0
    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return SemVerContent(
            list[0] as VersionCore,
            list.filterIsInstance<PreRelease>().firstOrNull(),
            list.filterIsInstance<Build>().firstOrNull()
        )
    }

    class SemVerContent(
        val core: VersionCore,
        val preRelease: PreRelease?,
        val build: Build?
    ) : Content() {

        override val entries: Iterable<Any>
            get() = iterable {
                yield(core)
                if (preRelease != null) {
                    yield('-')
                    yield(preRelease)
                }
                if (build != null) {
                    yield('+')
                    yield(build)
                }
            }

    }

}
