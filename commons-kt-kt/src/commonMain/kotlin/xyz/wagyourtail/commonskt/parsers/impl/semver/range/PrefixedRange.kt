package xyz.wagyourtail.commonskt.parsers.impl.semver.range

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.SemVer
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class PrefixedRange : RangeData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: PrefixContent) : super(content)

    companion object : StringDataCompanion<PrefixedRange>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek()
            return c == '=' || c == '>' || c == '<' || c == '~' || c == '^' || c == '!'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val first = reader.take()
            if (first == '=' || first == '>' || first == '<' || first == '~' || first == '^' || first == '!') {
                val second = reader.peek()
                val prefix: String
                if (second == '=') {
                    reader.take()
                    prefix = "$first="
                } else {
                    prefix = first.toString()
                }
                val p = Prefix.bySymbol[prefix]
                    ?: throw reader.createException("Invalid prefix: $prefix")
                append(p)
                append(reader.takeNonNewlineWhitespace())
                append(ExactVersionRange.read(reader))
            } else {
                throw reader.createException("Invalid prefix: $first")
            }
        }

        override fun unchecked(value: String): PrefixedRange = PrefixedRange(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): PrefixedRange =
            PrefixedRange(buffer, start, end)

        /** compat for the old parse(CharReader) call shape */
        fun parse(reader: CharReader<*>): PrefixedRange = read(reader)

        /** compat for the old parse(String) call shape */
        fun parse(raw: String): PrefixedRange = checked(raw)

    }

    override fun contains(version: SemVer): Boolean {
        val content = content as PrefixContent
        return content.prefix.compare(content.range, version)
    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return PrefixContent(
            list.filterIsInstance<Prefix>().first(),
            list.filterIsInstance<ExactVersionRange>().first()
        )
    }

    enum class Prefix(
        val symbol: String,
        val compare: (ExactVersionRange, SemVer) -> Boolean
    ) {
        EQ("=", { r, v -> r.compare(v) == 0 }),
        NEQ("!=", { r, v -> r.compare(v) != 0 }),
        GT(">", { r, v -> r.compare(v) > 0 }),
        GTE(">=", { r, v -> r.compare(v) >= 0 }),
        LT("<", { r, v -> r.compare(v) < 0 }),
        LTE("<=", { r, v -> r.compare(v) <= 0 }),
        SIMILAR("~", ExactVersionRange::similar),
        COMPATIBLE("^", ExactVersionRange::compatible);

        override fun toString(): String {
            return symbol
        }

        companion object {
            val bySymbol: Map<String, Prefix> = entries.associateBy { it.symbol }
        }
    }

    class PrefixContent(
        val prefix: Prefix,
        val range: ExactVersionRange
    ) : Content() {

        override val entries: Iterable<Any>
            get() = iterable {
                yield(prefix)
                yield(range)
            }

    }

}
