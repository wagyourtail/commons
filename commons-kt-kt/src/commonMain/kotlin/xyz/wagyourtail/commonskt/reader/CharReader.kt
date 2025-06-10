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
     * @return the number of chars skipped
     */
    fun skip(count: Int): Int {
        var skipped = 0
        while (skipped < count) {
            val ch = take()
            if (ch == null) break
            skipped++
        }
        return skipped
    }

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
    open fun copy(): T {
        return copy(Int.MAX_VALUE)
    }

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

    fun expect(char: Char, ignoreCase: Boolean = false): Char {
        val it = take()
        if (it?.equals(char, ignoreCase) != true) createException("Expected $char but got ${it ?: "EOS"}")
        return char
    }

    fun expect(value: String, ignoreCase: Boolean = false): String {
        for (char in value) {
            expect(char, ignoreCase)
        }
        return value
    }

    fun expectEOF() {
        val it = take()
        if (it != null) createException("Expected EOF but got $it")
    }

    fun expectEOS() {
        val it = take()
        if (it != null) createException("Expected EOS but got $it")
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
                    throw createException("Expected separator char, got $next")
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

    open fun <R> parse(reader: CharReader<*>.() -> R): R {
        this.mark()
        try {
            return reader(WrappingReader(this, Int.MAX_VALUE))
        } catch (e: ParseException) {
            this.reset()
            throw e
        }
    }

    fun <R> parse(vararg readers: CharReader<*>.() -> R): R {
        val exceptions = mutableListOf<ParseException>()
        for (reader in readers) {
            try {
                return parse(reader)
            } catch (e: ParseException) {
                exceptions.add(e)
            }
        }
        throw createCompositeException("Failed to parse", *exceptions.toTypedArray())
    }

    open fun createException(msg: String, cause: Throwable? = null) = ParseException(msg, cause)

    fun createCompositeException(msg: String, vararg exceptions: ParseException) = createException(msg, null).also {
        for (e in exceptions) {
            it.addSuppressed(e)
        }
    }

    class WrappingReader(private val reader: CharReader<*>, private val limit: Int) : CharReader<WrappingReader>() {
        private val sb = StringBuilder()
        private var position = 0
        private var mark = 0

        override fun peek(): Char? {
            if (position == limit) {
                return null
            }
            if (position >= sb.length) {
                val next = reader.take()
                if (next == null) return null
                sb.append(next)
                return next
            }
            return sb[position]
        }

        override fun take(): Char? {
            if (position == limit) {
                return null
            }
            if (position >= sb.length) {
                val next = reader.take()
                position++
                if (next == null) return null
                sb.append(next)
                return next
            }
            return sb[position++]
        }

        override fun copy(): WrappingReader {
            return WrappingReader(reader, limit - position)
        }

        override fun copy(limit: Int): WrappingReader {
            return WrappingReader(reader, limit)
        }

        override fun mark() {
            mark = position
        }

        override fun reset() {
            position = mark
        }

    }

}