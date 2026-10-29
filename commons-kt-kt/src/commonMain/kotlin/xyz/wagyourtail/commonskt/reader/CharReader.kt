package xyz.wagyourtail.commonskt.reader

import xyz.wagyourtail.commonskt.utils.translateEscapes
import kotlin.math.min

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
     * @return a copy of the reader at the current position, with its own
     * position/mark state; multiple copies may be active and interleaved
     * freely — reading a copy never moves the parent or sibling copies.
     * Sequential (stream) readers cannot copy; wrap them in
     * [BufferedCharReader] first.
     * @since 1.0.4
     */
    abstract fun copy(limit: Int = Int.MAX_VALUE): CharReader<*>


    /**
     * set this position as remembered.
     * @param limit maximum chars that may be consumed before the mark becomes
     * invalid — advisory for buffer/string readers (always replayable), but
     * on sequential readers it bounds the recorded replay buffer like
     * java.io's read-ahead limit.
     */
    abstract fun mark(limit: Int = Int.MAX_VALUE)

    /**
     * reset to remembered mark position, or beginning if no mark.
     * On sequential readers this throws [IllegalStateException] when there is
     * no active mark (unset, or invalidated by exceeding the mark limit or by
     * [skip]).
     */
    abstract fun reset()

    fun exhausted() = peek() == null

    open fun takeRemaining() = buildString {
        while (peek() != null) append(take())
    }

    fun takeLine() = takeUntil('\n')

    open fun takeUntil(char: Char) = takeUntil { it == char }

    /**
     * take until the given substring is about to be read — the match is not
     * consumed (the reader is left positioned at it). Uses [copy] for
     * speculation so it does not disturb the caller's mark; falls back to a
     * mark/reset scan on readers that don't support copying (raw streams).
     */
    fun takeUntil(characters: String): String {
        val probe = try {
            copy()
        } catch (e: UnsupportedOperationException) {
            null
        }
        if (probe != null) {
            val sb = StringBuilder()
            while (peek() != null) {
                val copy = copy()
                try {
                    copy.expect(characters)
                    return sb.toString()
                } catch (_: ParseException) {
                    sb.append(take() as Char)
                }
            }
            return sb.toString()
        }
        // fallback for readers without copy() — mark/reset scan
        val sb = StringBuilder()
        var next = peek()
        var taken = ""
        while (next != null) {
            do {
                if (characters.startsWith(taken + next)) {
                    if (taken.isEmpty()) {
                        mark()
                    }
                    taken += next
                    break
                } else if (!taken.isEmpty()) {
                    val newStart = taken.indexOf(characters[0], 1)
                    if (newStart == -1) {
                        taken = ""
                        if (characters.startsWith(taken + next)) {
                            mark()
                            taken += next
                        }
                        break
                    }
                    reset()
                    skip(newStart)
                    mark()
                    taken = taken.substring(newStart)
                    skip(taken.length)
                }
            } while (!taken.isEmpty())
            sb.append(take() as Char)
            if (characters == taken) {
                val out = sb.removeSuffix(characters)
                reset()
                return out.toString()
            }
            next = peek()
        }
        return sb.toString()
    }

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

    fun takeWholeNumber(leadingZero: Boolean = false): Long {
        // don't allow leading zero, but allow 0 by itself
        val text = takeWhile { it.isDigit() }
        if (!leadingZero && text.startsWith("0") && text.length > 1) {
            throw createException("Leading zero not allowed: $text")
        }
        if (text.isEmpty()) {
            throw createException("Expected number")
        }
        return text.toLong()
    }

    fun takeString(leinient: Boolean = true, escapeDoubleQuote: Boolean = false, quote: Char = '"'): String {
        return takeString(leinient, escapeDoubleQuote, quote = quote.toString())
    }

    fun takeString(
        leinient: Boolean = true,
        escapeDoubleQuote: Boolean = false,
        escapeNewline: Boolean = false,
        multiline: Boolean = false,
        noStartQuote: Boolean = false,
        noTranslateEscapes: Boolean = false,
        quote: String = "\""
    ): String {
        if (!noStartQuote) {
            expect(quote)
        }
        val sb = StringBuilder()
        while (!exhausted()) {
            val lines = takeUntil(quote).split(Regex("\r?\n"))
            val last = lines.last()
            if (multiline || escapeNewline) {
                for (next in lines.dropLast(1)) {
                    if (escapeNewline) {
                        var count = 0
                        for (j in next.indices.reversed()) {
                            if (next[j] == '\\') {
                                count++
                            } else {
                                break
                            }
                        }
                        if (count % 2 != 0) {
                            // escaped
                            sb.append(
                                if (noTranslateEscapes) {
                                    "$next\n"
                                } else {
                                    next.substring(0, next.length - 1)
                                }
                            )
                        } else if (multiline) {
                            sb.append(next).append('\n')
                        } else {
                            throw createException("Unexpected EOL in string literal")
                        }
                    } else {
                        sb.append(next).append('\n')
                    }
                }
            } else if (lines.size > 1) {
                throw createException("Unexpected EOL in string literal")
            }

            var count = 0
            for (j in last.indices.reversed()) {
                if (last[j] == '\\') {
                    count++
                } else {
                    break
                }
            }
            if (count % 2 != 0) {
                // escaped
                sb.append(
                    if (!noTranslateEscapes) {
                        last.substring(0, last.length - 1)
                    } else {
                        last
                    }
                )
                sb.append(expect(quote))
            } else {
                sb.append(last)
                if (escapeDoubleQuote) {
                    // doubled "quote quote" → append one literal quote and
                    // continue; speculation via parseOrNull so the caller's
                    // mark is not disturbed
                    if (parseOrNull({ expect(quote); expect(quote); true }) == true) {
                        sb.append(quote)
                        if (noTranslateEscapes) {
                            sb.append(quote)
                        }
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
        }
        expect(quote)
        if (noTranslateEscapes) {
            return "$quote$sb$quote"
        }
        return sb.toString().translateEscapes(leinient)
    }

    fun expect(char: Char, ignoreCase: Boolean = false): Char {
        val it = take()
        if (it?.equals(char, ignoreCase) != true) {
            throw createException("Expected $char but got ${it ?: "EOS"}")
        }
        return char
    }

    fun expect(valueType: String, acceptor: (Char) -> Boolean): Char {
        val next = take() ?: throw createException("Expected $valueType but got EOS")
        if (!acceptor(next)) throw createException("Expected $valueType but got $next")
        return next
    }

    fun expect(value: String, ignoreCase: Boolean = false): String {
        for (char in value) {
            expect(char, ignoreCase)
        }
        return value
    }

    fun expectEOF() {
        val it = take()
        if (it != null) throw createException("Expected EOF but got $it")
    }

    fun expectEOS() {
        val it = take()
        if (it != null) throw createException("Expected EOS but got $it")
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
            val br = BufferedCharReader(this, Int.MAX_VALUE)
            val ret = reader(br)
            this.reset()
            this.skip(br.position)
            return ret
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

    fun <R> parse(type: String, vararg readers: CharReader<*>.() -> R): R {
        val exceptions = mutableListOf<ParseException>()
        for (reader in readers) {
            try {
                return parse(reader)
            } catch (e: ParseException) {
                exceptions.add(e)
            }
        }
        throw createCompositeException("Failed to parse as any of $type", *exceptions.toTypedArray())
    }

    fun <R> parseOrNull(vararg readers: CharReader<*>.() -> R): R? {
        for (reader in readers) {
            try {
                return parse(reader)
            } catch (_: ParseException) {
            }
        }
        return null
    }

    open fun createException(msg: String, cause: Throwable? = null) = ParseException(msg, cause = cause)

    open fun createCompositeException(msg: String, vararg exceptions: ParseException) =
        createException(msg, null).also {
            for (e in exceptions) {
                it.addSuppressed(e)
            }
        }

    /**
     * This wrapping class uses the reader class as a delegate,
     * so don't do multithreaded stuff or interleave using the outer reader
     * with the wrapped reader.
     */
    class BufferedCharReader(private val reader: CharReader<*>, private var limit: Int) :
        CharReader<BufferedCharReader>(), BackedCharReader {
        private val buffer = StringBuilder()
        internal var position = 0
        private var mark = 0

        override val backingBuffer: CharSequence
            get() = buffer

        override val backingPosition: Int
            get() = position

        override fun peek(): Char? {
            return peekAt(position)
        }

        fun peekAt(position: Int): Char? {
            if (position >= limit) {
                return null
            }
            if (position > buffer.length) {
                return null
            }
            if (position == buffer.length) {
                return reader.peek()
            }
            return buffer[position]
        }

        override fun take(): Char? {
            if (position >= limit) {
                return null
            }
            val next = takeAt(position) ?: return null
            position++
            return next
        }

        fun takeAt(position: Int): Char? {
            if (position >= limit) {
                return null
            }
            if (position > buffer.length) {
                throw IllegalStateException("position $position is beyond the buffered frontier ${buffer.length}")
            }
            if (position == buffer.length) {
                val next = reader.take() ?: return null
                buffer.append(next)
                return next
            }
            return buffer[position]
        }

        override fun copy(limit: Int): CopyReader {
            if (limit < 0) throw IllegalArgumentException("limit < 0")
            val limit = position + limit
            return CopyReader(position, min(if (limit < position) Int.MAX_VALUE else limit, this.limit))
        }

        /**
         * parse is mark-transparent: the caller's mark/reset state is
         * preserved across both success and failure.
         */
        override fun <R> parse(reader: CharReader<*>.() -> R): R {
            val savedPosition = position
            val savedMark = mark
            try {
                val br = copy()
                val ret = reader(br)
                this.position = br.position
                this.mark = savedMark
                return ret
            } catch (e: ParseException) {
                this.position = savedPosition
                this.mark = savedMark
                throw e
            }
        }

        override fun mark(limit: Int) {
            mark = position
        }

        override fun reset() {
            position = mark
        }

        inner class CopyReader(internal var position: Int, private val limit: Int) : CharReader<CopyReader>(), BackedCharReader {
            private var mark: Int = position

            override val backingBuffer: CharSequence
                get() = buffer

            override val backingPosition: Int
                get() = position

            override fun peek(): Char? {
                if (position >= limit) {
                    return null
                }
                return peekAt(position)
            }

            override fun take(): Char? {
                if (position >= limit) {
                    return null
                }
                val next = takeAt(position) ?: return null
                position++
                return next
            }

            override fun copy(limit: Int): CopyReader {
                if (limit < 0) throw IllegalArgumentException("limit < 0")
                val limit = position + limit
                return CopyReader(position, min(if (limit < position) Int.MAX_VALUE else limit, this.limit))
            }

            /**
             * parse is mark-transparent: the caller's mark/reset state is
             * preserved across both success and failure.
             */
            override fun <R> parse(reader: CharReader<*>.() -> R): R {
                val savedPosition = position
                val savedMark = mark
                try {
                    val br = copy()
                    val ret = reader(br)
                    this.position = br.position
                    this.mark = savedMark
                    return ret
                } catch (e: ParseException) {
                    this.position = savedPosition
                    this.mark = savedMark
                    throw e
                }
            }

            override fun mark(limit: Int) {
                mark = position
            }

            override fun reset() {
                position = mark
            }

        }

    }

}
