package xyz.wagyourtail.commonskt.reader

import xyz.wagyourtail.commonskt.utils.indexOf
import kotlin.math.min

class StringCharReader(val buffer: CharSequence, var pos: Int = 0, val endPos: Int = buffer.length) :
    CharReader<StringCharReader>(), BackedCharReader {
    private var mark: Int = 0

    override val backingBuffer: CharSequence
        get() = buffer

    override val backingPosition: Int
        get() = pos

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

    override fun copy(limit: Int): StringCharReader {
        if (limit < 0) throw IllegalArgumentException("limit < 0")
        val limit = pos + limit
        return StringCharReader(buffer, pos, min(endPos, if (limit < pos) Int.MAX_VALUE else limit)).also { it.mark() }
    }

    override fun takeRemaining(): String {
        if (pos < endPos) {
            val str = buffer.substring(pos, endPos)
            pos = endPos
            return str
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

    override fun mark(limit: Int) {
        mark = pos
    }

    override fun reset() {
        pos = mark
    }

    /**
     * parse is mark-transparent: the caller's mark/reset state is preserved
     * across both success and failure.
     */
    override fun <R> parse(reader: CharReader<*>.() -> R): R {
        val savedPos = pos
        val savedMark = mark
        try {
            val wrapping = copy()
            val value = reader(wrapping)
            pos = wrapping.pos
            mark = savedMark
            return value
        } catch (e: ParseException) {
            pos = savedPos
            mark = savedMark
            throw e
        }
    }

    /** line-start offsets — built once; exceptions are thrown on
     *  speculation paths where an O(pos) scan per throw is quadratic */
    private var lineStarts: IntArray? = null

    private fun lineStarts(): IntArray = lineStarts ?: run {
        val list = ArrayList<Int>()
        var i = buffer.indexOf('\n')
        while (i != -1) {
            list.add(i + 1)
            i = buffer.indexOf('\n', i + 1)
        }
        list.toIntArray().also { lineStarts = it }
    }

    override fun createException(msg: String, cause: Throwable?): ParseException {
        val starts = lineStarts()
        // last line-start <= pos
        var lo = 0; var hi = starts.size - 1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            if (starts[mid] <= pos) lo = mid + 1 else hi = mid - 1
        }
        val lineIdx = hi
        return if (lineIdx < 0) ParseException(msg, 0, pos + 1, cause)
        else ParseException(msg, lineIdx + 1, pos - starts[lineIdx] + 1, cause)
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
