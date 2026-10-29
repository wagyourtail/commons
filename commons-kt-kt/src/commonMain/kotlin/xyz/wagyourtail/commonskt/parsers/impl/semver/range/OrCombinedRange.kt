package xyz.wagyourtail.commonskt.parsers.impl.semver.range

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.ListContentWithDelimiter
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.SemVer
import xyz.wagyourtail.commonskt.reader.CharReader

class OrCombinedRange : RangeData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: ListContentWithDelimiter) : super(content)

    companion object : StringDataCompanion<OrCombinedRange>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return AndCombinedRange.shouldRead(reader)
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            while (!reader.exhausted()) {
                append(reader.takeNonNewlineWhitespace())
                append(AndCombinedRange.read(reader))
                append(reader.takeNonNewlineWhitespace())
                if (reader.peek() == '|') {
                    append(reader.expect("||"))
                } else {
                    break
                }
            }
        }

        override fun unchecked(value: String): OrCombinedRange = OrCombinedRange(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): OrCombinedRange =
            OrCombinedRange(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): OrCombinedRange = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): OrCombinedRange = checked(raw)

    }

    override fun contains(version: SemVer): Boolean {
        for (entry in (content as ListContentWithDelimiter).entriesWithoutDelimiters) {
            if ((entry as RangeData).contains(version)) return true
        }
        return false
    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return ListContentWithDelimiter(list.filter { it is AndCombinedRange }, " || ")
    }

}
