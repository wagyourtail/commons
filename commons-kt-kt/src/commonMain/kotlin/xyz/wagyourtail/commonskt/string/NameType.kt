package xyz.wagyourtail.commonskt.string

import xyz.wagyourtail.commonskt.utils.capitalized
import xyz.wagyourtail.commonskt.utils.iterable
import xyz.wagyourtail.commonskt.utils.uncapitalized

fun String.convertNameType(from: NameType, to: NameType): String {
    if (from == to) {
        return this
    }

    // shortcuts

    // - pascal/camel
    if (from == NameType.PASCAL_CASE && to == NameType.CAMEL_CASE) {
        // check if starts with initialism
        return if (
            (this.length >= 3 && this[0].isUpperCase() && this[1].isUpperCase() && this[2].isUpperCase()) ||
            (this.length == 2 && this[0].isUpperCase() && this[1].isUpperCase())
        ) {
            this
        } else {
            this.uncapitalized()
        }
    }
    if (from == NameType.CAMEL_CASE && to == NameType.PASCAL_CASE) {
        return this.capitalized()
    }

    // - kebab/snake
    if (from == NameType.KEBAB_CASE && to == NameType.SNAKE_CASE) {
        return this.replace('-', '_')
    }
    if (from == NameType.SNAKE_CASE && to == NameType.KEBAB_CASE) {
        return this.replace('_', '-')
    }

    // fallback
    val split = from.split(this)
    return to.join(split)
}

/**
 * @param split should return an iterable of the lowercase words making up the name (with initalisms remaining capitalized)
 * @param join should join the iterable of words into a string
 */
data class NameType(val split: (String) -> Iterable<String>, val join: (Iterable<String>) -> String) {

    companion object {
        val CAMEL_CASE = NameType(
            {
                // split by upper case, keep multiple capitals in a row together
                iterable {
                    val sb = StringBuilder()
                    for (i in 0 until it.length) {
                        if (it[i].isUpperCase()) {
                            if (sb.isNotEmpty() && sb[sb.length - 1].isLowerCase()) {
                                yield(sb.toString().lowercase())
                                sb.clear()
                            }
                            sb.append(it[i])
                        } else {
                            if (sb.isNotEmpty()) {
                                val lastChar = sb[sb.length - 1]
                                if (sb.length > 1 && lastChar.isUpperCase()) {
                                    // append multiple capitals and remove them from the sb
                                    val s = sb.substring(0, sb.length - 1)
                                    yield(if (s.length > 1) s else s.lowercase())
                                    sb.deleteRange(0, sb.length - 1)
                                }
                            }
                            sb.append(it[i])
                        }
                    }
                    if (sb.isNotEmpty()) {
                        if (sb.length > 1 && sb[0].isUpperCase() && sb[1].isUpperCase()) {
                            yield(sb.toString())
                        } else {
                            yield(sb.toString().lowercase())
                        }
                    }
                }
            },
            {
                val iter = it.iterator()
                iter.next() + iter.asSequence().joinToString("") { it.capitalized() }
            }
        )

        val PASCAL_CASE = NameType(
            CAMEL_CASE.split,
            { CAMEL_CASE.join(it).capitalized() }
        )

        val SNAKE_CASE = NameType(
            { it.split("_") },
            { it.joinToString("_") }
        )

        val KEBAB_CASE = NameType(
            { it.split("-") },
            { it.joinToString("-") }
        )

    }

}