package xyz.wagyourtail.commonskt.parsers.impl.semver.range

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.SemVer
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class ExactVersionRange : RangeData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: VersionContent) : super(content)

    companion object : StringDataCompanion<ExactVersionRange>() {

        private val SEGMENT: CharReader<*>.() -> Any = {
            parse(
                { takeWholeNumber(false).toString() },
                { expect("wildcard") { it == '*' || it == 'x' || it == 'X' }.toString() }
            )
        }

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return c.isDigit() || c == '*' || c == 'x' || c == 'X'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(reader.run(SEGMENT))
            if (reader.peek() != '.') return
            append(reader.take()!!)
            append(reader.run(SEGMENT))
            if (reader.peek() != '.') return
            append(reader.take()!!)
            append(reader.run(SEGMENT))
        }

        override fun unchecked(value: String): ExactVersionRange = ExactVersionRange(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): ExactVersionRange =
            ExactVersionRange(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): ExactVersionRange = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): ExactVersionRange = checked(raw)

    }

    private val segmentAt: (Int) -> String? = { i ->
        val list = (content as VersionContent)
        when (i) {
            0 -> list.major
            1 -> list.minor
            2 -> list.patch
            else -> null
        }
    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return VersionContent(
            list[0].toString(),
            list.getOrNull(2)?.toString(),
            list.getOrNull(4)?.toString()
        )
    }

    override fun contains(version: SemVer): Boolean {
        return compare(version) == 0
    }

    fun equals(version: SemVer): Boolean {
        return contains(version)
    }

    fun compare(version: SemVer): Int {
        val content = content as VersionContent
        var major = content.major
        var minor = content.minor
        var patch = content.patch

        if (major != null && (major == "*" || major.equals("x", true))) major = null
        if (minor != null && (minor == "*" || minor.equals("x", true))) minor = null
        if (patch != null && (patch == "*" || patch.equals("x", true))) patch = null

        val versionCore = version.core
        val versionMajor = versionCore.major

        if (major == null) return 0
        val majorLong = major.toLong()
        if (versionMajor < majorLong) return -1
        if (versionMajor > majorLong) return 1

        if (minor == null) return 0
        val versionMinor = versionCore.minor
        val minorLong = minor.toLong()
        if (versionMinor < minorLong) return -1
        if (versionMinor > minorLong) return 1

        if (patch == null) return 0
        val versionPatch = versionCore.patch
        val patchLong = patch.toLong()
        val patchResult = patchLong.compareTo(versionPatch)
        if (patchResult == 0 && version.preRelease != null) {
            return -1
        }
        return patchResult
    }

    fun similar(version: SemVer): Boolean {
        val content = content as VersionContent
        var major = content.major
        var minor = content.minor
        var patch = content.patch

        if (major != null && (major == "*" || major.equals("x", true))) major = null
        if (minor != null && (minor == "*" || minor.equals("x", true))) minor = null
        if (patch != null && (patch == "*" || patch.equals("x", true))) patch = null

        val versionCore = version.core

        if (major == null) return true
        val majorLong = major.toLong()
        if (versionCore.major != majorLong) return false

        if (minor == null) return true
        val minorLong = minor.toLong()
        if (versionCore.minor != minorLong) return false

        if (patch == null) return true
        val patchLong = patch.toLong()
        return versionCore.patch >= patchLong
    }

    fun compatible(version: SemVer): Boolean {
        val content = content as VersionContent
        var major = content.major
        var minor = content.minor
        var patch = content.patch

        if (major != null && (major == "*" || major.equals("x", true))) major = null
        if (minor != null && (minor == "*" || minor.equals("x", true))) minor = null
        if (patch != null && (patch == "*" || patch.equals("x", true))) patch = null

        val versionCore = version.core

        if (major == null) return true
        val majorLong = major.toLong()
        if (versionCore.major != majorLong) return false

        if (minor == null) return true
        val versionMinor = versionCore.minor
        val minorLong = minor.toLong()
        if (majorLong == 0L && versionMinor != minorLong) return false
        if (versionMinor < minorLong) return false
        if (versionMinor > minorLong) return true

        if (patch == null) return true
        val versionPatch = versionCore.patch
        val patchLong = patch.toLong()
        if (minorLong == 0L && versionPatch != patchLong) return false
        return versionPatch >= patchLong
    }

    class VersionContent(
        val major: String?,
        val minor: String?,
        val patch: String?
    ) : Content() {

        override val entries: Iterable<Any>
            get() = iterable {
                if (major != null) yield(major)
                if (minor != null) {
                    yield('.')
                    yield(minor)
                }
                if (patch != null) {
                    yield('.')
                    yield(patch)
                }
            }

    }

}
