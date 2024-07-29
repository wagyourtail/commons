package xyz.wagyourtail.commonskt.utils

fun Int.toUnicode(): String {
    // isBmpCodePoint
    return if (this in (Char.MIN_VALUE.code..Char.MAX_VALUE.code)) {
        this.toChar().toString()
    } else if (this.isValidUnicodeCodePoint()) {
        charArrayOf(this.highUnicodeSurrogate(), this.lowUnicodeSurrogate()).concatToString()
    } else {
        throw IllegalArgumentException("Invalid unicode code point: $this")
    }
}

fun Int.isValidUnicodeCodePoint(): Boolean {
    val plane = this.ushr(16)
    return plane < ((0X10FFFF + 1).ushr(16))
}

fun Int.lowUnicodeSurrogate() = (this.and(0x3ff) + '\uDC00'.code).toChar()
fun Int.highUnicodeSurrogate() = (this.ushr(10) + ('\uD800'.code - 0x010000)).toChar()

fun Int.checkedToChar(): Char? =
    if (this in (Char.MIN_VALUE.code..Char.MAX_VALUE.code)) {
        this.toChar()
    } else {
        null
    }

fun StringBuilder.appendCodePoint(value: Int) {
    if (value in (Char.MIN_VALUE.code..Char.MAX_VALUE.code)) {
        append(value.toChar())
    } else if (value.isValidUnicodeCodePoint()) {
        append(value.highUnicodeSurrogate())
        append(value.lowUnicodeSurrogate())
    } else {
        throw IllegalArgumentException("Invalid unicode code point: $value")
    }
}