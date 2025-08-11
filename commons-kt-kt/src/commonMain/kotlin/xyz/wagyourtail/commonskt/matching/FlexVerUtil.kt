package xyz.wagyourtail.commonskt.matching

import org.jetbrains.annotations.VisibleForTesting
import kotlin.math.max
import kotlin.math.min

/**
 * Parse the given strings as freeform version strings, and compare them according to FlexVer.
 * @param b the second version string
 * @return `0` if the two versions are equal, a negative number if `a < b`, or a positive number if `a > b`
 */
fun String.compareFlexVer(b: String): Int {
    return FlexVerComparator.compare(this, b)
}

/**
 * Implements FlexVer, a SemVer-compatible intuitive comparator for free-form versioning strings as
 * seen in the wild. It's designed to sort versions like people do, rather than attempting to force
 * conformance to a rigid and limited standard. As such, it imposes no restrictions. Comparing two
 * versions with differing formats will likely produce nonsensical results (garbage in, garbage out),
 * but best effort is made to correct for basic structural changes, and versions of differing length
 * will be parsed in a logical fashion.
 *
 * @author Unascribed
 */
object FlexVerComparator {
    /**
     * Parse the given strings as freeform version strings, and compare them according to FlexVer.
     *
     * @param a the first version string
     * @param b the second version string
     * @return `0` if the two versions are equal, a negative number if `a < b`, or a positive number if `a > b`
     */
    fun compare(a: String, b: String): Int {
        val ad = decompose(a)
        val bd = decompose(b)
        for (i in 0 until max(ad.size, bd.size)) {
            val c = get(ad, i).compareTo(get(bd, i))
            if (c != 0) return c
        }
        return 0
    }


    private val NULL: VersionComponent = object : VersionComponent(CharArray(0)) {
        override fun compareTo(that: VersionComponent): Int {
            return if (that === this) 0 else -that.compareTo(this)
        }
    }

    /**
     * Break apart a string into intuitive version components,
     * by splitting it where a run of characters changes from numeric to non-numeric.
     *
     * @param str the version String
     */
    @VisibleForTesting
    fun decompose(str: String): List<VersionComponent> {
        if (str.isEmpty()) return emptyList()
        var lastWasNumber = isAsciiDigit(str[0])
        val chars = str.toCharArray()
        val out: MutableList<VersionComponent> = ArrayList()
        var j = 0
        var i = 0
        while (i < chars.size) {
            val c = chars[i]
            if (c == '+') break // remove appendices

            val number = isAsciiDigit(c)
            if (number != lastWasNumber || (c == '-' && j > 0 && chars[0] != '-')) {
                out.add(createComponent(lastWasNumber, chars, j))
                j = 0
                lastWasNumber = number
            }
            chars[j] = c
            j++
            i++
        }
        out.add(createComponent(lastWasNumber, chars, j))
        return out
    }

    private fun isAsciiDigit(c: Char): Boolean {
        return c >= '0' && c <= '9'
    }

    private fun createComponent(number: Boolean, s: CharArray, j: Int): VersionComponent {
        val arr = s.slice(0..<j).toCharArray()
        return if (number) {
            NumericVersionComponent(arr)
        } else if (arr.size > 1 && arr[0] == '-') {
            SemVerPrereleaseVersionComponent(arr)
        } else {
            VersionComponent(arr)
        }
    }

    private fun get(li: List<VersionComponent>, i: Int): VersionComponent {
        return if (i >= li.size) NULL else li[i]
    }

    @VisibleForTesting
    open class VersionComponent(private val chars: CharArray) {
        fun chars(): CharArray {
            return chars
        }

        open fun compareTo(that: VersionComponent): Int {
            if (that === NULL) return 1
            val a = this.chars()
            val b = that.chars()

            for (i in 0 until min(a.size, b.size)) {
                val c1 = a[i]
                val c2 = b[i]
                if (c1 != c2) return c1 - c2
            }

            return a.size - b.size
        }

        override fun toString(): String {
            return chars.concatToString()
        }
    }

    @VisibleForTesting
    class SemVerPrereleaseVersionComponent(chars: CharArray) : VersionComponent(chars) {
        override fun compareTo(that: VersionComponent): Int {
            if (that === NULL) return -1 // opposite order

            return super.compareTo(that)
        }
    }

    @VisibleForTesting
    class NumericVersionComponent(chars: CharArray) : VersionComponent(chars) {
        override fun compareTo(that: VersionComponent): Int {
            if (that === NULL) return 1
            if (that is NumericVersionComponent) {
                val a = removeLeadingZeroes(this.chars())
                val b = removeLeadingZeroes(that.chars())
                if (a.size != b.size) return a.size - b.size
                for (i in a.indices) {
                    val ad = a[i]
                    val bd = b[i]
                    if (ad != bd) return ad - bd
                }
                return 0
            }
            return super.compareTo(that)
        }

        private fun removeLeadingZeroes(a: CharArray): CharArray {
            if (a.size == 1) return a
            var i = 0
            val stopIdx = a.size - 1
            while (i < stopIdx && a[i] == '0') {
                i++
            }
            return a.slice(i..<a.size).toCharArray()
        }
    }
}