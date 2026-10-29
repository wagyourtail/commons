package xyz.wagyourtail.commonskt.parsers.impl.semver

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.ListContentWithDelimiter
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class PreRelease : RawStringData, Comparable<PreRelease> {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: ListContentWithDelimiter) : super(content)

    companion object : StringDataCompanion<PreRelease>() {

        private val IDENTIFIER_CHARS: (Char) -> Boolean = { it.isLetterOrDigit() || it == '-' }

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return IDENTIFIER_CHARS(c)
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            var first = true
            while (true) {
                if (first) first = false else append(reader.expect('.'))
                append(
                    reader.parse(
                        {
                            val s = takeWhile(IDENTIFIER_CHARS)
                            // ensure one is non-digit
                            if (s.all { it.isDigit() }) {
                                throw createException("Numeric identifier can't have leading 0: ")
                            }
                            if (s.isEmpty()) {
                                throw createException("Empty identifier")
                            }
                            s
                        },
                        { takeWholeNumber() }
                    )
                )
                if (reader.peek() != '.') break
            }
        }

        override fun unchecked(value: String): PreRelease = PreRelease(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): PreRelease =
            PreRelease(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): PreRelease = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): PreRelease = checked(raw)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return ListContentWithDelimiter(list.filter { it != '.' }, '.')
    }

    override fun compareTo(other: PreRelease): Int {
        val self = (content as ListContentWithDelimiter).entriesWithoutDelimiters.iterator()
        val otherIt = (other.content as ListContentWithDelimiter).entriesWithoutDelimiters.iterator()
        while (self.hasNext() && otherIt.hasNext()) {
            val selfNext = self.next()
            val otherNext = otherIt.next()
            if ((selfNext is Long) != (otherNext is Long)) {
                return if (selfNext is Long) -1 else 1
            }
            val c = if (selfNext is Long) {
                selfNext.compareTo(otherNext as Long)
            } else {
                (selfNext as String).compareTo(otherNext as String)
            }
            if (c != 0) return c
        }
        if (self.hasNext() != otherIt.hasNext()) {
            return if (self.hasNext()) -1 else 1
        }
        return 0
    }

}
