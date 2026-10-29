package xyz.wagyourtail.commonskt.parsers.impl.semver.range

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.ListContentWithDelimiter
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.SemVer
import xyz.wagyourtail.commonskt.reader.CharReader

class AndCombinedRange : RangeData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: ListContentWithDelimiter) : super(content)

    companion object : StringDataCompanion<AndCombinedRange>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return PrefixedRange.shouldRead(reader) ||
                HyphenRange.shouldRead(reader) ||
                BracketRange.shouldRead(reader)
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            var first = true
            while (!reader.exhausted()) {
                val ws = reader.takeNonNewlineWhitespace()
                val entry = reader.parseOrNull(
                    { PrefixedRange.read(this) },
                    { HyphenRange.read(this) },
                    { BracketRange.read(this) }
                )
                if (entry == null) {
                    if (!first && ws.isNotEmpty()) append(ws)
                    break
                }
                if (!first) append(ws)
                append(entry)
                first = false
            }
        }

        override fun unchecked(value: String): AndCombinedRange = AndCombinedRange(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): AndCombinedRange =
            AndCombinedRange(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): AndCombinedRange = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): AndCombinedRange = checked(raw)

    }

    override fun contains(version: SemVer): Boolean {
        for (entry in (content as ListContentWithDelimiter).entriesWithoutDelimiters) {
            if (!(entry as RangeData).contains(version)) return false
        }
        return true
    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return ListContentWithDelimiter(list.filter { it is RangeData }, " ")
    }

}
