package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.constant.number.BinaryPart
import xyz.wagyourtail.commonskt.parsers.impl.constant.number.DecimalPart
import xyz.wagyourtail.commonskt.parsers.impl.constant.number.ExponentPart
import xyz.wagyourtail.commonskt.parsers.impl.constant.number.HexPart
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.unaryMinus


class Json5Number : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<Json5Number>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return c == '-' || c == '+' || c == '.' || c == 'N' || c == 'I' || c.isDigit()
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val first = reader.peek()
            if (first == '-' || first == '+') {
                append(reader.take()!!)
            }

            var next = reader.peek()
            if (next != null && next > '0' && next <= '9') {
                append(DecimalPart.read(reader))
                next = reader.peek()
                if (next == '.') {
                    append(reader.take()!!)
                    next = reader.peek()
                    if (next != null && next >= '0' && next <= '9') {
                        append(DecimalPart.read(reader))
                    }
                    next = reader.peek()
                }
                if (next == 'e' || next == 'E') {
                    append(reader.take()!!)
                    append(ExponentPart.read(reader))
                }
            } else if (next == '0') {
                append(reader.take()!!)
                next = reader.peek()
                when (next) {
                    '.' -> {
                        append(reader.take()!!)
                        next = reader.peek()
                        if (next != null && next >= '0' && next <= '9') {
                            append(DecimalPart.read(reader))
                        }
                        next = reader.peek()
                        if (next == 'e' || next == 'E') {
                            append(reader.take()!!)
                            append(ExponentPart.read(reader))
                        }
                    }

                    'x', 'X' -> {
                        append(reader.take()!!)
                        append(HexPart.read(reader))
                        return
                    }

                    'b', 'B' -> {
                        append(reader.take()!!)
                        append(BinaryPart.read(reader))
                        return
                    }
                }
            } else if (next == '.') {
                append(reader.take()!!)
                append(DecimalPart.read(reader))
                next = reader.peek()
                if (next == 'e' || next == 'E') {
                    append(reader.take()!!)
                    append(ExponentPart.read(reader))
                }
            } else if (next == 'I') {
                append(reader.expect("Infinity"))
            } else if (next == 'N') {
                append(reader.expect("NaN"))
            } else {
                throw reader.createException("Not a number character: $next")
            }
        }

        override fun unchecked(value: String): Json5Number = Json5Number(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5Number =
            Json5Number(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5Number = read(rawContent)

    }

    val isNegative: Boolean
        get() = rawContent[0] == '-'

    val positive: Json5Number
        get() {
            val raw = rawContent
            return if (raw[0] == '-') {
                Json5Number(raw.substring(1))
            } else {
                this
            }
        }

    val negative: Json5Number
        get() {
            val raw = rawContent
            return if (raw[0] == '-') {
                this
            } else if (raw[0] == '+') {
                Json5Number("-" + raw.substring(1))
            } else {
                Json5Number("-$raw")
            }
        }

    val isWhole: Boolean
        get() {
            val positive = positive.rawContent.removePrefix("+")

            if (positive.length == 1) {
                return true
            }

            if (positive[0] == '0') {
                return false
            }

            val last = positive[positive.length - 1]
            if (last == 'd' || last == 'D' || last == 'f' || last == 'F') {
                return false
            }

            return !positive.contains(".") && !positive.contains("e") && !positive.contains("E")
        }

    val isDecimal: Boolean
        get() {
            val raw = rawContent

            if (raw.contains(".") || raw.contains("e") || raw.contains("E")) {
                return true
            }

            val positive = positive.rawContent.removePrefix("+")

            return positive.startsWith("I") || positive.startsWith("N")
        }

    val isHex: Boolean
        get() {
            val positive = positive.rawContent.removePrefix("+")
            return positive.startsWith("0x") || positive.startsWith("0X")
        }

    val isBinary: Boolean
        get() {
            val positive = positive.rawContent.removePrefix("+")
            return positive.startsWith("0b") || positive.startsWith("0B")
        }

    val value: Number
        get() {
            if (isNegative) {
                return -positive.value
            }
            val value = positive.rawContent.removePrefix("+")
            return when {
                isHex -> value.substring(2).toLong(16)
                isBinary -> value.substring(2).toLong(2)
                isDecimal -> value.toDouble()
                isWhole -> value.toLong()
                else -> throw IllegalStateException()
            }
        }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
