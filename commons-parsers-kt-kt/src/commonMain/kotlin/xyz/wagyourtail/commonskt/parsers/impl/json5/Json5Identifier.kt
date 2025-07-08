package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.isAlphabetic
import xyz.wagyourtail.commonskt.utils.toUnicode

class Json5Identifier(rawContent: String) : StringData.OnlyRaw<Data.SingleContent<String>>(rawContent) {

    constructor(content: SingleContent<String>) : this(content.toString())

    companion object : StringDataBuilder<Json5Identifier, SingleContent<String>> {

        override fun invoke(rawContent: CharReader<*>): Json5Identifier {
            return Json5Identifier(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
            return SingleContent(buildString {
                var first = reader.take()
                if (first == null) {
                    throw reader.createException("Unexpected end of input")
                }
                if (first == '\\') {
                    val s = takeUnicodeEscapeSequence(reader)
                    if (s.length != 1) {
                        throw reader.createException("Invalid identifier start: $s")
                    }
                    first = s[0]
                }
                if (!first.isAlphabetic() && first != '$' && first != '_') {
                    throw reader.createException("Invalid identifier start: $first")
                }
                append(first)

                while (!reader.exhausted()) {
                    reader.mark()
                    var c = reader.take()!!
                    if (c == '\\') {
                        val s = takeUnicodeEscapeSequence(reader)
                        if (s.length != 1) {
                            throw reader.createException("Invalid identifier character: $s")
                        }
                        c = s[0]
                    }
                    if (c.isAlphabetic() || c == '$' || c == '_') {
                        append(c)
                        continue
                    }
                    val type = c.category
                    if (
                        type == CharCategory.NON_SPACING_MARK ||
                        type == CharCategory.COMBINING_SPACING_MARK ||
                        type == CharCategory.DECIMAL_DIGIT_NUMBER ||
                        type == CharCategory.CONNECTOR_PUNCTUATION ||
                        c == '\u200C' ||
                        c == '\u200D'
                    ) {
                        append(c)
                        continue
                    }
                    reader.reset()
                    break
                }


            })
        }

        fun takeUnicodeEscapeSequence(reader: CharReader<*>): String {
            reader.expect('u')

            val hex = buildString(4) {
                for (i in 0..3) {
                    append(reader.expect("hex") { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' })
                }
            }

            return hex.toInt(16).toUnicode()
        }

    }

    override fun checkedBuildContent(reader: CharReader<*>): SingleContent<String> {
        return Companion.checkedBuildContent(reader)
    }

}