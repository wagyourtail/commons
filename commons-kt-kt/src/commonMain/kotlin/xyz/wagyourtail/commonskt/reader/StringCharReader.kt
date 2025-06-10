package xyz.wagyourtail.commonskt.reader

import xyz.wagyourtail.commonskt.utils.indexOf
import kotlin.math.min

class StringCharReader(val buffer: String, var pos: Int = 0, val endPos: Int = buffer.length) : CharReader<StringCharReader>() {
    private var mark: Int = 0

    override fun peek(): Char? {
        if (pos < endPos) {
            return buffer[pos]
        }
        return null
    }

    override fun take(): Char? {
        if (pos < endPos) {
            return buffer[pos++]
        }
        return null
    }

    override fun take(count: Int): String {
        if (pos < endPos) {
            val end = min(pos + count, endPos)
            val str = buffer.substring(pos, end)
            pos = end
            return str
        }
        return ""
    }

    override fun copy() = copy(endPos - pos)

    override fun copy(limit: Int): StringCharReader {
        return StringCharReader(buffer, pos, pos + limit).also { it.mark() }
    }

    override fun takeRemaining(): String {
        if (pos < endPos) {
            return buffer.substring(pos, endPos)
        }
        return ""
    }

    override fun takeUntil(char: Char): String {
        val next = buffer.indexOf(char, pos, endPos)
        if (next == -1) {
            val str = buffer.substring(pos, endPos)
            pos = endPos
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

    override fun <R> parse(reader: CharReader<*>.() -> R): R {
        mark()
        try {
            val wrapping = copy()
            val value = reader(wrapping)
            pos = wrapping.pos
            return value
        } catch (e: ParseException) {
            reset()
            throw e
        }
    }

    override fun createException(msg: String, cause: Throwable?): ParseException {
        return super.createException(msg + " (at " + getPosition() + ")", cause)
    }

    fun getPosition(): String {
        if (!buffer.contains("\n")) {
            return "$pos"
        }
        val line = buffer.substring(0, pos).count { it == '\n' }
        val column = pos - buffer.substring(0, pos).lastIndexOf('\n')
        return "$line:$column"
    }
}

