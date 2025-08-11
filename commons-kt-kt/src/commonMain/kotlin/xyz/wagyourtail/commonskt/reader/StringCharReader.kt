package xyz.wagyourtail.commonskt.reader

import xyz.wagyourtail.commonskt.utils.indexOf
import kotlin.math.min

class StringCharReader(val buffer: String, var pos: Int = 0, val endPos: Int = buffer.length) :
    CharReader<StringCharReader>() {
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
        var count = 0
        var lineStart = 0
        if (!buffer.contains("\n")) {
            count = -1
        } else {
            do {
                val next = buffer.indexOf('\n', lineStart) + 1
                if (next == 0 || next > pos) break
                lineStart = next
                count++
            } while (true)
        }
        return ParseException(msg, count + 1, pos - lineStart + 1, cause)
    }

    override fun createCompositeException(msg: String, vararg exceptions: ParseException): ParseException {
        val lastExceptions = mutableListOf<ParseException>()
        var lastException: ParseException? = null
        for (e in exceptions) {
            if (lastException == null) {
                lastException = e
                lastExceptions.add(e)
                continue
            }
            val compare = lastException.compareTo(e)
            if (compare < 0) {
                lastExceptions.clear()
                lastExceptions.add(e)
                lastException = e
            } else if (compare == 0) {
                lastExceptions.add(e)
            }
        }
        if (lastExceptions.size == 1) {
            return createException(msg, lastException)
        } else {
            val exception = createException(msg)
            for (e in lastExceptions) {
                exception.addSuppressed(e)
            }
            return exception
        }
    }
}
