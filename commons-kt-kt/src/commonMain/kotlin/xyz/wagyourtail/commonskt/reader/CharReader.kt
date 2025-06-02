package xyz.wagyourtail.commonskt.reader

import xyz.wagyourtail.commonskt.utils.translateEscapes

abstract class CharReader<T : CharReader<T>> {

    companion object {
        operator fun invoke(value: String) = StringCharReader(value)
    }

    /**
     * @return either a char, or null for eof
     */
    abstract fun peek(): Char?

    /**
     * @return either a char, or null for eof
     */
    abstract fun take(): Char?

    /**
     * @return a string of the next count chars
     */
    open fun take(count: Int) = buildString {
        for (i in 0 until count) {
            take()?.let { append(it) } ?: break
        }
    }

    /**
     * @return a copy of the reader at the current position
     */
    abstract fun copy(): T

    /**
     * @return a copy of the reader at the current position, with a limit.
     * @since 1.0.4
     */
    abstract fun copy(limit: Int): T


    /**
     * set this position as remembered.
     */
    abstract fun mark()

    /**
     * reset to remembered mark position, or beginning if no mark.
     */
    abstract fun reset()

    fun exhausted() = peek() == null

    open fun takeRemaining() = buildString {
        while (peek() != null) append(take())
    }

    fun takeLine() = takeUntil('\n')

    open fun takeUntil(char: Char) = takeUntil { it == char }

    inline fun takeUntil(sep: (Char) -> Boolean) = buildString {
        var next = peek()
        while (next != null && !sep(next)) {
            append(take()!!)
            next = peek()
        }
    }

    inline fun takeWhile(acceptor: (Char) -> Boolean) = takeUntil { !acceptor(it) }

    fun takeWhitespace() = takeWhile { it.isWhitespace() }

    fun takeNonNewlineWhitespace() = takeWhile { it.isWhitespace() && it != '\n' }

    inline fun takeNext(sep: (Char) -> Boolean = { it.isWhitespace() }): String? {
        var next = peek()
        if (next == null || next == '\n') return null
        val value = if (next == '"') takeString()
        else takeUntil { sep(it) || it == '\n' }
        // take trailing sep
        next = peek()
        if (next != null && next != '\n' && sep(next)) {
            take()
        }
        return value
    }

    fun takeNext(sep: Char) = takeNext { it == sep }

    inline fun takeNextLiteral(sep: (Char) -> Boolean = { it.isWhitespace() }): String? {
        var next = peek()
        if (next == null || next == '\n') return null
        if (next == '\n') return null
        val value = takeUntil { sep(it) || it == '\n' }
        // take trailing sep
        next = peek()
        if (next != null && next != '\n' && sep(next)) {
            take()
        }
        return value
    }

    fun takeNextLiteral(sep: Char) = takeNextLiteral { it == sep }

    inline fun takeRemainingOnLine(sep: (Char) -> Boolean = { it.isWhitespace() }) = buildList<String> {
        var next = takeNext(sep)
        while (next != null) {
            add(next)
            next = takeNext(sep)
        }
    }

    fun takeRemainingOnLine(sep: Char) = takeRemainingOnLine { it == sep }

    inline fun takeRemainingLiteralOnLine(sep: (Char) -> Boolean = { it.isWhitespace() }) = buildList<String> {
        var next = takeNextLiteral(sep)
        while (next != null) {
            add(next)
            next = takeNextLiteral(sep)
        }
    }

    fun takeRemainingLiteralOnLine(sep: Char) = takeRemainingLiteralOnLine { it == sep }

    fun takeString(leinient: Boolean = true, escapeDoubleQuote: Boolean = false, quote: Char = '"') = buildString {
        expect(quote)
        var escapes = 0
        var next = take()
        while (next != null) {
            if (next == quote && escapes == 0) {
                if (escapeDoubleQuote && peek() == quote) {
                    append('\\')
                    append(take()!!)
                } else {
                    break
                }
            }
            if (next == '\\') {
                escapes++
            } else {
                escapes = 0
            }
            append(next)
            if (escapes == 2) {
                escapes = 0
            }
            next = take()
        }
    }.translateEscapes(leinient)

    fun expect(char: Char): Char {
        val it = take()
        if (it != char) throw IllegalArgumentException("Expected $char but got ${it ?: "EOS"}")
        return char
    }

    fun expect(value: String): String {
        for (char in value) {
            expect(char)
        }
        return value
    }

    fun takeCol(leinient: Boolean = true, sep: (Char) -> Boolean = { it == ',' }): String? {
        var next = peek()
        if (next == null || next == '\n') {
            return null
        }
        if (next == '"') {
            val value = takeString(leinient, true)
            val whiteSpace = takeNonNewlineWhitespace()
            next = peek()
            if (next != null && next != '\n' && !sep(next)) {
                if (!leinient) {
                    throw IllegalArgumentException("Expected separator char, got $next")
                }
                return value + whiteSpace + takeCol(true, sep)
            }
            next = peek()
            if (next != null && sep(next)) {
                take()
            }
            return value
        }
        val value = takeUntil { sep(it) || it == '\n' }
        next = peek()
        if (next != null && sep(next)) {
            take()
        }
        return value
    }

    fun takeCol(leinient: Boolean = true, sep: Char) = takeCol(leinient) { it == sep }

    fun takeRemainingCol(leinient: Boolean = true, sep: (Char) -> Boolean = { it == ',' }) = buildList<String> {
        var next = takeCol(leinient, sep)
        while (next != null) {
            add(next)
            next = takeCol(leinient, sep)
        }
    }

    fun takeRemainingCol(leinient: Boolean = true, sep: Char) = takeRemainingCol(leinient) { it == sep }

}