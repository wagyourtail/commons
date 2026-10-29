package xyz.wagyourtail.commonskt.reader

import kotlin.math.min

/**
 * [ByteReader] over an in-memory `ByteArray`, with random-access [seek] and
 * zero-copy [copy] sub-range readers — the byte analog of [StringCharReader].
 */
class ByteArrayByteReader(val buffer: ByteArray, pos: Int = 0, val endPos: Int = buffer.size) :
    ByteReader<ByteArrayByteReader>(), BackedByteReader {

    override var position: Int = pos
        internal set
    private var mark: Int = pos

    override val backingBuffer: ByteArray
        get() = buffer

    override val backingPosition: Int
        get() = position

    override fun peek(): Int? {
        if (position < endPos) {
            return buffer[position].toInt() and 0xFF
        }
        return null
    }

    override fun take(): Int? {
        if (position < endPos) {
            return buffer[position++].toInt() and 0xFF
        }
        return null
    }

    /**
     * jump to an absolute position within this reader's range.
     */
    fun seek(pos: Int) {
        require(pos in 0..endPos) { "seek position $pos out of range 0..$endPos" }
        this.position = pos
    }

    override fun skip(count: Int): Int {
        val end = min(position + count, endPos)
        val skipped = end - position
        position = end
        return skipped
    }

    override fun take(count: Int): ByteArray {
        if (position < endPos) {
            val end = min(position + count, endPos)
            val out = buffer.copyOfRange(position, end)
            position = end
            return out
        }
        return ByteArray(0)
    }

    override fun copy(limit: Int): ByteArrayByteReader {
        if (limit < 0) throw IllegalArgumentException("limit < 0")
        val limit = position + limit
        return ByteArrayByteReader(buffer, position, min(endPos, if (limit < position) Int.MAX_VALUE else limit))
            .also { it.mark() }
    }

    override fun takeRemaining(): ByteArray {
        if (position < endPos) {
            val out = buffer.copyOfRange(position, endPos)
            position = endPos
            return out
        }
        return ByteArray(0)
    }

    override fun takeUntil(byte: Int): ByteArray {
        val needle = (byte and 0xFF).toByte()
        var next = position
        while (next < endPos && buffer[next] != needle) next++
        val out = buffer.copyOfRange(position, next)
        position = next
        return out
    }

    override fun mark(limit: Int) {
        mark = position
    }

    override fun reset() {
        position = mark
    }

    /**
     * parse is mark-transparent: the caller's mark/reset state is preserved
     * across both success and failure.
     */
    override fun <R> parse(reader: ByteReader<*>.() -> R): R {
        val savedPosition = position
        val savedMark = mark
        try {
            val wrapping = copy()
            val value = reader(wrapping)
            position = wrapping.position
            mark = savedMark
            return value
        } catch (e: ParseException) {
            position = savedPosition
            mark = savedMark
            throw e
        }
    }

    override fun createException(msg: String, cause: Throwable?): ParseException {
        return ParseException.offset(msg, position, cause)
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
