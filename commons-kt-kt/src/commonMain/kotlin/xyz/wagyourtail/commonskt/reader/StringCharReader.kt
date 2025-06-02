package xyz.wagyourtail.commonskt.reader

import xyz.wagyourtail.commonskt.utils.indexOf
import kotlin.math.min

class StringCharReader(val buffer: String, var pos: Int = 0, val limit: Int = buffer.length) : CharReader<StringCharReader>() {
    private var mark: Int = 0

    override fun peek(): Char? {
        if (pos < limit) {
            return buffer[pos]
        }
        return null
    }

    override fun take(): Char? {
        if (pos < limit) {
            return buffer[pos++]
        }
        return null
    }

    override fun take(count: Int): String {
        if (pos < limit) {
            val end = min(pos + count, limit)
            val str = buffer.substring(pos, end)
            pos = end
            return str
        }
        return ""
    }

    override fun copy() = copy(limit)

    override fun copy(limit: Int): StringCharReader {
        return StringCharReader(buffer, pos, limit).also { it.mark() }
    }

    override fun takeRemaining(): String {
        if (pos < limit) {
            return buffer.substring(pos, limit)
        }
        return ""
    }

    override fun takeUntil(char: Char): String {
        val next = buffer.indexOf(char, pos, limit)
        if (next == -1) {
            val str = buffer.substring(pos, limit)
            pos = limit
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

