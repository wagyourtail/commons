package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.isAlphabetic
import xyz.wagyourtail.commonskt.utils.times
import xyz.wagyourtail.commonskt.utils.toUnicode

class Json5Identifier : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<Json5Identifier>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return c == '\\' || c.isAlphabetic() || c == '$' || c == '_'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val raw = StringBuilder()
            var c = reader.take()
            if (c == null) {
                throw reader.createException("Unexpected end of input")
            }
            raw.append(c)
            var check = c
            if (c == '\\') {
                val seq = takeUnicodeEscapeSequence(reader)
                if (seq.first.length != 1) {
                    throw reader.createException("Invalid identifier start: ${seq.first}")
                }
                raw.append(seq.second)
                check = seq.first[0]
            }
            if (!check.isAlphabetic() && check != '$' && check != '_') {
                throw reader.createException("Invalid identifier start: $check")
            }

            while (!reader.exhausted()) {
                reader.mark()
                c = reader.take()!!
                check = c
                var seqRaw: String? = null
                if (c == '\\') {
                    val seq = takeUnicodeEscapeSequence(reader)
                    if (seq.first.length != 1) {
                        throw reader.createException("Invalid identifier character: ${seq.first}")
                    }
                    seqRaw = seq.second
                    check = seq.first[0]
                }
                if (check.isAlphabetic() || check == '$' || check == '_') {
                    if (seqRaw == null) raw.append(c) else raw.append('\\').append(seqRaw)
                    continue
                }
                val type = check.category
                if (
                    type == CharCategory.NON_SPACING_MARK ||
                    type == CharCategory.COMBINING_SPACING_MARK ||
                    type == CharCategory.DECIMAL_DIGIT_NUMBER ||
                    type == CharCategory.CONNECTOR_PUNCTUATION ||
                    check == '\u200C' ||
                    check == '\u200D'
                ) {
                    if (seqRaw == null) raw.append(c) else raw.append('\\').append(seqRaw)
                    continue
                }
                reader.reset()
                break
            }
            append(raw.toString())
        }

        /**
         * reads a `\uXXXX` escape (decoded value, raw seq) — note: legacy
         * grammar consumed exactly 3 hex digits after the 'u'.
         */
        private fun takeUnicodeEscapeSequence(reader: CharReader<*>): Pair<String, String> {
            reader.expect('u')

            val hex = buildString(4) {
                3.times {
                    append(reader.expect("hex") { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' })
                }
            }

            return hex.toInt(16).toUnicode() to "u$hex"
        }

        override fun unchecked(value: String): Json5Identifier = Json5Identifier(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5Identifier =
            Json5Identifier(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5Identifier = read(rawContent)

    }

    /** identifier name with escape sequences decoded */
    val name: String
        get() = cached(rawContent) { raw ->
            buildString {
                var i = 0
                while (i < raw.length) {
                    if (raw[i] == '\\' && i + 4 < raw.length && raw[i + 1] == 'u') {
                        append(raw.substring(i + 2, i + 5).toInt(16).toUnicode())
                        i += 5
                    } else {
                        append(raw[i++])
                    }
                }
            }
        }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
