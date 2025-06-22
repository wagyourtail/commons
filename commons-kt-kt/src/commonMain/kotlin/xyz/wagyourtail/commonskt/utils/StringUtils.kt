package xyz.wagyourtail.commonskt.utils

import kotlin.text.indexOf

/**
 * unlike java, this will translate unicode escapes as well
 */
fun String.translateEscapes(lenient: Boolean = false): String {
    if (this.isEmpty() || !this.contains("\\")) {
        return this
    }
    return buildString(this.length) {
        var i = 0
        while (i < this@translateEscapes.length) {
            val c = this@translateEscapes[i++]
            if (c == '\\') {
                if (i >= this@translateEscapes.length) throw IllegalArgumentException("Invalid escape, hit end of string")
                when (val n = this@translateEscapes[i++]) {
                    '"' -> append('"')
                    '\'' -> append('\'')
                    '\\' -> append('\\')
                    '0', '1', '2', '3', '4', '5', '6', '7' -> {
                        var octal = n.toString()
                        val max = if (octal.toInt() < 4) 2 else 1
                        for (j in 0 until max) {
                            if (i >= this@translateEscapes.length) break
                            val next = this@translateEscapes[i]
                            if (next !in ('0'..'7')) break
                            octal += next
                            i++
                        }
                        append(octal.toInt(8).toChar())
                    }

                    'u' -> {
                        val hex = this@translateEscapes.substring(i, i + 4)
                        if (hex.length != 4) throw IllegalArgumentException("Invalid unicode escape: $hex, expected 4 characters, found EOS")
                        append(hex.toInt(16).toUnicode())
                        i += 4
                    }

                    'b' -> append('\b')
                    'f' -> append('\u000C')
                    'n' -> append('\n')
                    'r' -> append('\r')
                    's' -> append(' ')
                    't' -> append('\t')
                    else -> {
                        if (lenient) {
                            append("\\")
                            append(n)
                        } else {
                            throw IllegalArgumentException(
                                "Invalid escape: ${n.toString().escape(true)} in \"${this}\" at $i"
                            )
                        }
                    }
                }
            } else {
                append(c)
            }
        }
    }
}

fun String.escape(unicode: Boolean = false, spaces: Boolean = false, doubleQuote: Boolean = false): String {
    if (this.isEmpty()) return this
    return buildString(this.length) {
        var i = 0
        while (i < this@escape.length) {
            when (val c = this@escape[i++]) {
                '"' -> {
                    if (doubleQuote) {
                        append("\"\"")
                    } else {
                        append("\\\"")
                    }
                }

                '\'' -> append("\\'")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                ' ' -> {
                    if (spaces) {
                        append("\\s")
                    } else {
                        append(c)
                    }
                }

                else -> {
                    if (unicode && (c.code < 0x20 || c.code > 0x7f)) {
                        append("\\u${c.code.toString(16).padStart(4, '0')}")
                    } else {
                        append(c)
                    }
                }
            }
        }
    }
}

fun String.maybeEscape(): String {
    if (any { it.isWhitespace() } || startsWith("\"")) {
        return "\"${escape(unicode = true)}\""
    }
    return this
}

fun String.removeSuffix(suffix: String, ignoreCase: Boolean = false): String {
    if (endsWith(suffix, ignoreCase)) {
        return substring(0, length - suffix.length)
    }
    return this
}

fun String.removePrefix(prefix: String, ignoreCase: Boolean = false): String {
    if (startsWith(prefix, ignoreCase)) {
        return substring(prefix.length)
    }
    return this
}

fun String.removeSurrounding(prefix: String, suffix: String, ignoreCase: Boolean = false): String {
    return removePrefix(prefix, ignoreCase).removeSuffix(suffix, ignoreCase)
}

fun String.capitalized() = replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }

fun String.uncapitalized() = replaceFirstChar { if (it.isUpperCase()) it.lowercase() else it.toString() }

fun String.decapitalized() = uncapitalized()

fun String.indexOf(other: Char, startIndex: Int, endIndex: Int, ignoreCase: Boolean = false): Int {
    val i = indexOf(other, startIndex, ignoreCase)
    if (i >= endIndex) return -1
    return i
}

fun String.count(char: Char, startIndex: Int = 0, endIndex: Int = this.length, ignoreCase: Boolean = false): Int {
    var count = 0
    var i = startIndex
    while (i < endIndex) {
        val index = indexOf(char, i, ignoreCase)
        if (index < 0) break
        count++
        i = index + 1
    }
    return count
}

fun String.count(subString: String, startIndex: Int = 0, endIndex: Int = this.length, ignoreCase: Boolean = false): Int {
    var count = 0
    var i = startIndex
    while (i < endIndex) {
        val index = indexOf(subString, i, ignoreCase)
        if (index < 0) break
        count++
        i = index + subString.length
    }
    return count
}