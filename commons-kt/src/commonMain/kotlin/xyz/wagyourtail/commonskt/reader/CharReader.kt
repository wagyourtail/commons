package xyz.wagyourtail.commonskt.reader

import xyz.wagyourtail.commonskt.utils.translateEscapes

abstract class CharReader<T: CharReader<T>> {

    /**
     * @return either a char, or null for eof
     */
    abstract fun peek(): Char?

    /**
     * @return either a char, or null for eof
     */
    abstract fun take(): Char?

    /**
     * @return a copy of the reader at the current position
     */
    abstract fun copy(): T

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
            append(next.toChar())
            next = peek()
        }
    }

    inline fun takeWhile(acceptor: (Char) -> Boolean) = buildString {
        var next = peek()
        while (next != null && acceptor(next)) {
            append(next.toChar())
            next = peek()
        }
    }

    fun takeWhitespace() = takeWhile { it.isWhitespace() }

    fun takeNonNewlineWhitespace() = takeWhile { it.isWhitespace() && it != '\n' }

    inline fun takeNext(sep: (Char) -> Boolean = { it.isWhitespace() }): String? {
        takeNonNewlineWhitespace()
        val next = peek()
        if (next == null || next == '\n') return null
        if (next == '"') return takeString()
        return takeWhile(sep)
    }

    inline fun takeNextLiteral(sep: (Char) -> Boolean = { it.isWhitespace() }): String? {
        takeNonNewlineWhitespace()
        val next = peek()
        if (next == null || next == '\n') return null
        if (next == '\n') return null
        return takeWhile(sep)
    }

    inline fun takeRemainingOnLine(sep: (Char) -> Boolean = { it.isWhitespace() }): List<String> = buildList {
        var next = takeNext(sep)
        while (next != null) {
            add(next)
            next = takeNext(sep)
        }
    }

    fun takeString(leinient: Boolean = true, escapeDoubleQuote: Boolean = false) = buildString {
        expect('"')
        var escapes = 0
        var next = take()
        while (next != null) {
            if (next == '"' && escapes == 0) {
                if (escapeDoubleQuote && peek() == '"') {
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

    fun expect(char: Char) {
        val it = take()
        if (it != char) throw IllegalArgumentException("Expected $char but got $it")
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

}