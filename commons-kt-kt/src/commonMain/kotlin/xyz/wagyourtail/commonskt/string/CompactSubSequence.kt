package xyz.wagyourtail.commonskt.string

/**
 * A implementation of CharSequence. designed to be used in memory-efficient contexts by not duplicating strings.
 */
class CompactSubSequence(
    private val backing: CharSequence,
    private val start: Int = 0,
    private val end: Int = backing.length
) : CharSequence {

    init {
        if (start < 0 || start > backing.length || end < 0 || end > backing.length || start > end) {
            throw IndexOutOfBoundsException("Invalid subsequence range: [$start, $end]")
        }
    }

    override val length: Int
        get() = end - start

    override fun get(index: Int): Char {
        if (index < 0 || index >= length) {
            throw IndexOutOfBoundsException("Index out of range: $index")
        }
        return backing[start + index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return CompactSubSequence(backing, this.start + startIndex, this.start + endIndex)
    }

    override fun toString(): String {
        return backing.subSequence(start, end).toString()
    }
}
