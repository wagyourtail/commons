package xyz.wagyourtail.commonskt.parsers.impl.constant

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.constant.number.*
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.unaryMinus


class NumberConstant : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<NumberConstant>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek() ?: return false
            return c == '-' || c == '.' || c == 'N' || c == 'I' || c.isDigit()
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            val first: Char? = reader.peek()
            if (first == '-') {
                append(reader.take()!!)
            }
            var next: Char? = reader.peek()
            if (next != null && next > '0' && next <= '9') {
                append(WholePart.read(reader))
                next = reader.peek()
                if (next == 'l' || next == 'L') {
                    append(reader.take()!!)
                    return
                }
                if (next == '.') {
                    append(reader.take()!!)
                    next = reader.peek()
                    if (next!! >= '0' && next <= '9') {
                        append(DecimalPart.read(reader))
                    }
                    next = reader.peek()
                }
                if (next == 'e' || next == 'E') {
                    append(reader.take()!!)
                    append(ExponentPart.read(reader))
                    next = reader.peek()
                }
            } else if (next == '0') {
                append(reader.take()!!)
                next = reader.peek()
                when (next) {
                    '.' -> {
                        append(reader.take()!!)
                        next = reader.peek()
                        if (next!! >= '0' && next <= '9') {
                            append(DecimalPart.read(reader))
                        }
                        next = reader.peek()
                        if (next == 'e' || next == 'E') {
                            append(reader.take()!!)
                            append(ExponentPart.read(reader))
                            next = reader.peek()
                        }
                    }

                    'x', 'X' -> {
                        append(reader.take()!!)
                        append(HexPart.read(reader))
                        next = reader.peek()
                        if (next == 'l' || next == 'L') {
                            append(reader.take()!!)
                        }
                        return
                    }

                    'b', 'B' -> {
                        append(reader.take()!!)
                        append(BinaryPart.read(reader))
                        next = reader.peek()
                        if (next == 'l' || next == 'L') {
                            append(reader.take()!!)
                        }
                        return
                    }

                    null -> {}
                    else -> if (next >= '0' && next < '8') {
                        append(OctalPart.read(reader))
                        next = reader.peek()
                        if (next == 'l' || next == 'L') {
                            append(reader.take()!!)
                        }
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
                    next = reader.peek()
                }
            } else if (next == 'I') {
                append(reader.expect("Infinity"))
                next = reader.peek()
            } else if (next == 'N') {
                append(reader.expect("NaN"))
                next = reader.peek()
            } else {
                throw reader.createException("Not a number character: " + next as Char)
            }

            if (next == null) return
            if (next == 'f' || next == 'F'
                || next == 'd' || next == 'D'
            ) {
                append(reader.take()!!)
            }
        }

        override fun unchecked(value: String): NumberConstant = NumberConstant(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): NumberConstant =
            NumberConstant(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): NumberConstant = read(rawContent)

    }

    val isNegative: Boolean
        get() = rawContent[0] == '-'

    val positive: NumberConstant
        get() {
            val raw = rawContent
            if (raw[0] == '-') {
                return NumberConstant(raw.substring(1))
            }
            return this
        }

    val negative: NumberConstant
        get() {
            val raw = rawContent
            if (raw[0] != '-') {
                return NumberConstant("-$raw")
            }
            return this
        }

    val isWhole: Boolean
        get() {
            val positive = positive.rawContent

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
            val positive = positive.rawContent
            val last = positive[positive.length - 1]

            if (positive[0] == '0') {
                if (positive.length == 1) {
                    return false
                }

                val next = positive[1]
                return next == '.' || next == 'e' || next == 'E'
            }

            if (last == 'd' || last == 'D' || last == 'f' || last == 'F') {
                return true
            }

            if (last == 'l' || last == 'L') {
                return false
            }

            return positive.contains(".") || positive.contains("e") || positive.contains("E")
        }

    val isHex: Boolean
        get() {
            val positive = positive.rawContent
            return positive.startsWith("0x") || positive.startsWith("0X")
        }

    val isBinary: Boolean
        get() {
            val positive = positive.rawContent
            return positive.startsWith("0b") || positive.startsWith("0B")
        }

    val isOctal: Boolean
        get() {
            val positive = positive.rawContent
            if (positive.length == 1) {
                return false
            }
            val second = positive[1]
            return positive[0] == '0' && (second >= '0' && second <= '7')
        }

    val isFloat: Boolean
        get() {
            val raw = rawContent
            val last = raw[raw.length - 1]
            return (last == 'f' || last == 'F') && !isHex
        }

    val isLong: Boolean
        get() {
            val raw = rawContent
            val last = raw[raw.length - 1]
            return last == 'l' || last == 'L'
        }

    val isDouble: Boolean
        get() {
            val positive = positive.rawContent

            if (positive.startsWith("I") || positive.startsWith("N")) {
                val last = positive[positive.length - 1]
                return last != 'f' && last != 'F'
            }

            if (isDecimal) {
                val last = positive[positive.length - 1]
                return last != 'f' && last != 'F'
            }
            return false
        }

    val isInteger: Boolean
        get() {
            return isWhole && !isLong
        }

    val value: Number
        get() {
            if (isNegative) {
                return -positive.value
            }
            val raw = rawContent
            if (isHex) {
                return if (isLong) {
                    raw.substring(2, raw.length - 1).toLong(16)
                } else {
                    raw.substring(2).toInt(16)
                }
            }
            if (isBinary) {
                return if (isLong) {
                    raw.substring(2, raw.length - 1).toLong(2)
                } else {
                    raw.substring(2).toInt(2)
                }
            }
            if (isOctal) {
                return if (isLong) {
                    raw.substring(1, raw.length - 1).toLong(8)
                } else {
                    raw.substring(1).toInt(8)
                }
            }
            if (isFloat) {
                return raw.substring(0, raw.length - 1).toFloat()
            }
            if (isLong) {
                return raw.substring(0, raw.length - 1).toLong()
            }
            if (isDouble) {
                val last = raw[raw.length - 1]
                return if (last == 'd' || last == 'D') {
                    raw.substring(0, raw.length - 1).toDouble()
                } else {
                    raw.toDouble()
                }
            }
            if (isInteger) {
                return raw.toInt()
            }
            throw IllegalStateException()
        }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
