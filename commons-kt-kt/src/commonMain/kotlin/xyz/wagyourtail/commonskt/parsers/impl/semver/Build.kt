package xyz.wagyourtail.commonskt.parsers.impl.semver

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.ListContentWithDelimiter
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class Build : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: ListContentWithDelimiter) : super(content)

    companion object : StringDataCompanion<Build>() {

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
                                throw createException("Alphanumeric identifier can't have only digits")
                            }
                            if (s.isEmpty()) {
                                throw createException("Empty identifier")
                            }
                            s
                        },
                        { takeWholeNumber(true) }
                    )
                )
                if (reader.peek() != '.') break
            }
        }

        override fun unchecked(value: String): Build = Build(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Build =
            Build(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): Build = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): Build = checked(raw)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return ListContentWithDelimiter(list.filter { it != '.' }, '.')
    }

}
