package xyz.wagyourtail.commonskt.parsers.impl.semver.range

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.SemVer
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class HyphenRange : RangeData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: HyphenContent) : super(content)

    companion object : StringDataCompanion<HyphenRange>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return ExactVersionRange.shouldRead(reader)
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(ExactVersionRange.read(reader))
            reader.parseOrNull({
                append(takeNonNewlineWhitespace())
                append(expect('-'))
                append(takeNonNewlineWhitespace())
                append(ExactVersionRange.read(this))
            })
        }

        override fun unchecked(value: String): HyphenRange = HyphenRange(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): HyphenRange =
            HyphenRange(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): HyphenRange = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): HyphenRange = checked(raw)

    }

    override fun contains(version: SemVer): Boolean {
        // start <= version <= end
        val content = content as HyphenContent
        val start = content.start.compare(version)
        val end = content.end.compare(version)
        return start >= 0 && end <= 0
    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        val ranges = list.filterIsInstance<ExactVersionRange>()
        return HyphenContent(ranges.first(), ranges.getOrElse(1) { ranges.first() })
    }

    class HyphenContent(
        val start: ExactVersionRange,
        val end: ExactVersionRange
    ) : Content() {

        override val entries: Iterable<Any>
            get() = iterable {
                yield(start)
                if (end !== start) {
                    yield(" - ")
                    yield(end)
                }
            }

    }

}
