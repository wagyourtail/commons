package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.parsers.impl.constant.number.BinaryPart
import xyz.wagyourtail.commonskt.parsers.impl.constant.number.DecimalPart
import xyz.wagyourtail.commonskt.parsers.impl.constant.number.ExponentPart
import xyz.wagyourtail.commonskt.parsers.impl.constant.number.HexPart
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.unaryMinus


class Json5Number(rawContent: String) : StringData.OnlyRaw<Data.ListContent>(rawContent) {

    constructor(content: Data.ListContent) : this(content.toString())

    companion object : StringDataBuilder<Json5Number, ListContent> {

        override fun invoke(rawContent: CharReader<*>): Json5Number {
            return Json5Number(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): ListContent {
            val content = mutableListOf<Any>()

            val first = reader.peek()
            if (first == '-' || first == '+') {
                content.add(reader.take()!!)
            }

            var next = reader.peek()
            if (next != null && next > '0' && next <= '9') {
                content.add(DecimalPart(reader))
                next = reader.peek()
                if (next == '.') {
                    content.add(reader.take()!!)
                    next = reader.peek()
                    if (next != null && next >= '0' && next <= '9') {
                        content.add(DecimalPart(reader))
                    }
                    next = reader.peek()
                }
                if (next == 'e' || next == 'E') {
                    content.add(reader.take()!!)
                    content.add(ExponentPart(reader))
                }
            } else if (next == '0') {
                content.add(reader.take()!!)
                next = reader.peek()
                when (next) {
                    '.' -> {
                        content.add(reader.take()!!)
                        next = reader.peek()
                        if (next != null && next >= '0' && next <= '9') {
                            content.add(DecimalPart(reader))
                        }
                        next = reader.peek()
                        if (next == 'e' || next == 'E') {
                            content.add(reader.take()!!)
                            content.add(ExponentPart(reader))
                        }
                    }

                    'x', 'X' -> {
                        content.add(reader.take()!!)
                        content.add(HexPart(reader))
                        return ListContent(content)
                    }

                    'b', 'B' -> {
                        content.add(reader.take()!!)
                        content.add(BinaryPart(reader))
                        return ListContent(content)
                    }
                }
            } else if (next == '.') {
                content.add(reader.take()!!)
                content.add(DecimalPart(reader))
                next = reader.peek()
                if (next == 'e' || next == 'E') {
                    content.add(reader.take()!!)
                    content.add(ExponentPart(reader))
                }
            } else if (next == 'I') {
                content.add(reader.expect("Infinity"))
            } else if (next == 'N') {
                content.add(reader.expect("NaN"))
            } else {
                throw reader.createException("Not a number character: $next")
            }

            return ListContent(content)
        }

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

    override fun checkedBuildContent(reader: CharReader<*>): ListContent {
        return Companion.checkedBuildContent(reader)
    }

}