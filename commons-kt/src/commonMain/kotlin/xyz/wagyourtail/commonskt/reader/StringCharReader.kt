package xyz.wagyourtail.commonskt.reader

import kotlin.math.min

class StringCharReader(val buffer: String, var pos: Int = 0) : CharReader<StringCharReader>() {
    private var mark: Int = 0

    override fun peek(): Char? {
        if (pos < buffer.length) {
            return buffer[pos]
        }
        return null
    }

    override fun take(): Char? {
        if (pos < buffer.length) {
            return buffer[pos++]
        }
        return null
    }

    override fun take(count: Int): String {
        val end = min(pos + count, buffer.length)
        val str = buffer.substring(pos, end)
        pos = end
        return str
    }

    override fun copy(): StringCharReader {
        return StringCharReader(buffer, pos)
    }

    override fun takeRemaining(): String {
        if (pos < buffer.length) {
            return buffer.substring(pos)
        }
        return ""
    }

    override fun takeUntil(char: Char): String {
        val next = buffer.indexOf(char, pos)
        if (next == -1) {
            val str = buffer.substring(pos)
            pos = buffer.length
            return str
        }
        val str = buffer.substring(pos, next)
        pos = next
        return str
    }

    override fun mark() {
        mark = pos
    }

    override fun reset() {
        pos = mark
    }
}