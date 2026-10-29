package xyz.wagyourtail.commonskt.parsers.impl.semver.range

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.SemVer
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class BracketRange : RangeData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: BracketContent) : super(content)

    companion object : StringDataCompanion<BracketRange>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek()
            return c == '(' || c == '['
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(reader.expect("open bracket") { it == '(' || it == '[' })
            append(reader.takeNonNewlineWhitespace())
            append(ExactVersionRange.read(reader))
            append(reader.takeNonNewlineWhitespace())
            append(reader.expect(','))
            append(reader.takeNonNewlineWhitespace())
            val end = reader.peek()
            if (end == ')' || end == ']') {
                append(reader.take()!!)
                return
            }
            append(ExactVersionRange.read(reader))
            append(reader.takeNonNewlineWhitespace())
            append(reader.expect("close bracket") { it == ')' || it == ']' })
        }

        override fun unchecked(value: String): BracketRange = BracketRange(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): BracketRange =
            BracketRange(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): BracketRange = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): BracketRange = checked(raw)

    }

    override fun contains(version: SemVer): Boolean {
        val content = content as BracketContent
        val start = content.from.compare(version)
        val includeStart = content.includeLeft
        if (start > 0 || (start == 0 && !includeStart)) return false
        if (content.to == null) return true
        val end = content.to.compare(version)
        val includeEnd = content.includeRight
        return end < 0 || (end == 0 && includeEnd)
    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        val ranges = list.filterIsInstance<ExactVersionRange>()
        val brackets = list.filter { it == '(' || it == '[' || it == ')' || it == ']' }
        return BracketContent(
            brackets.first() == '[',
            ranges.first(),
            ranges.getOrNull(1),
            brackets.last() == ']'
        )
    }

    class BracketContent(
        val includeLeft: Boolean,
        val from: ExactVersionRange,
        val to: ExactVersionRange?,
        val includeRight: Boolean
    ) : Content() {

        override val entries: Iterable<Any>
            get() = iterable {
                yield(if (includeLeft) '[' else '(')
                yield(from)
                yield(',')
                if (to != null) yield(to)
                yield(if (includeRight) ']' else ')')
            }

    }

}
