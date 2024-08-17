package xyz.wagyourtail.commonskt.parser

import xyz.wagyourtail.commonskt.utils.translateEscapes

class CharReader(
    buf: String,
    pos: Int = 0
) {

    var pos: Int = pos
        private set

    val buffer = buf.replace("\r\n", "\n")

    inline fun <U> use(block: (CharReader) -> U) = block(this)

    fun copy() = CharReader(buffer, pos)

    fun exhausted() = pos >= buffer.length

    fun peek(): Char? {
        if (pos >= buffer.length) return null
        return buffer[pos]
    }

    fun take(): Char? {
        if (pos >= buffer.length) return null
        return buffer[pos++]
    }

    @PublishedApi
    internal fun takeUnchecked(): Char {
        return buffer[pos++]
    }

    fun takeRemaining() = takeUntil { false }

    fun takeLine(): String {
        return takeUntil { it == '\n' }
    }

    inline fun takeUntil(predicate: (Char) -> Boolean): String {
        return buildString {
            while (pos < buffer.length && !predicate(buffer[pos])) {
                append(takeUnchecked())
            }
        }
    }

    inline fun takeWhile(predicate: (Char) -> Boolean): String {
        return buildString {
            while (pos < buffer.length && predicate(buffer[pos])) {
                append(takeUnchecked())
            }
        }
    }

    fun takeWhitespace(): String {
        return takeUntil { !it.isWhitespace() }
    }

    fun takeNext(sep: (Char) -> Boolean = { it.isWhitespace() }): String? {
        takeWhile { sep(it) && it != '\n' }
        val next = peek()
        if (next == null || next == '\n') return null
        if (next == '"') {
            return takeString()
        }
        return takeUntil(sep)
    }

    fun takeNextLiteral(sep: Char = '\t'): String? {
        if (exhausted() || buffer[pos] == '\n') return null
        return buildString {
            while (!exhausted()) {
                val b = buffer[pos]
                if (b == '\n') break
                val c = buffer[pos++]
                if (c == sep) break
                append(c)
            }
        }
    }

    fun takeNextLiteral(sep: (Char) -> Boolean): String? {
        if (exhausted()) return null
        if (peek() == '\n') {
            return null
        }
        return buildString {
            while (!exhausted()) {
                val b = peek()
                if (b == '\n') break
                val c = take()
                if (sep(c!!)) break
                append(c)
            }
        }
    }

    fun takeNonNewlineWhitespace(): String {
        return takeUntil { !it.isWhitespace() || it == '\n' }
    }

    fun takeRemainingOnLine(sep: (Char) -> Boolean = { it.isWhitespace() }): List<String> {
        val list = mutableListOf<String>()
        var next = takeNext(sep)
        while (next != null) {
            list.add(next)
            next = takeNext(sep)
        }
        return list
    }

    fun takeString(lenient: Boolean = true, escapeDoubleQuote: Boolean = false) = buildString {
        expect('"')
        var escapes = 0
        while (pos < buffer.length) {
            val c = take()
            if (c == '"' && escapes == 0) {
                if (escapeDoubleQuote && peek() == '"') {
                    append("\\")
                    append(take())
                } else {
                    break
                }
            }
            if (c == '\\') {
                escapes++
            } else {
                escapes = 0
            }
            append(c)
            if (escapes == 2) {
                escapes = 0
            }
        }
    }.translateEscapes(lenient)

    fun expect(c: Char): Char {
        val next = take()
        if (next != c) {
            throw IllegalArgumentException("Expected $c, found $next")
        }
        return next
    }

    // -- CSV specific functions --

    fun takeRemainingCol(lenient: Boolean = false): List<String> {
        val list = mutableListOf<String>()
        var next = takeCol(lenient)
        while (next != null) {
            list.add(next)
            next = takeCol(lenient)
        }
        return list
    }

    fun takeCol(lenient: Boolean = false): String? {
        val took = if (peek() == ',') {
            take()
            true
        } else {
            false
        }
        val next = peek()
        if ((next == null || next == '\n') && !took) {
            return null
        }
        if (next == '"') {
            val value = takeString(lenient, true)
            val wsp = takeNonNewlineWhitespace()
            if (peek() != ',' && peek() != '\n' && peek() != null) {
                if (!lenient) {
                    throw IllegalArgumentException("Expected , found ${peek()}")
                } else {
                    return value + wsp + takeCol(lenient)
                }
            }
            return value
        }
        return takeUntil { it == ',' || it == '\n' }
    }

}