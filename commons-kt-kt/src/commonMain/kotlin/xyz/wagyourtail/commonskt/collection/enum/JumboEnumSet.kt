package xyz.wagyourtail.commonskt.collection.enum

import xyz.wagyourtail.commonskt.utils.ceilToMultipleOf

@PublishedApi
internal abstract class JumboEnumSet<T : Enum<T>>(
    internal val bitfield: LongArray,
    val universe: Array<T>
) : EnumSet<T> {

    companion object {
        fun <T : Enum<T>> noneOf(universe: Array<T>): JumboEnumSet<T> {
            val field = LongArray(universe.size.ceilToMultipleOf(64) / 64)
            return ImmutableJumboEnumSet(field, universe)
        }

        fun <T : Enum<T>> of(element: T, universe: Array<T>): JumboEnumSet<T> {
            val field = LongArray(universe.size.ceilToMultipleOf(64) / 64)
            val i = element.ordinal / 64
            val j = element.ordinal % 64
            field[i] = field[i] or (1L shl j)
            return ImmutableJumboEnumSet(field, universe)
        }

        fun <T : Enum<T>> of(vararg elements: T, universe: Array<T>): JumboEnumSet<T> {
            val field = LongArray(universe.size.ceilToMultipleOf(64) / 64)
            for (element in elements) {
                val i = element.ordinal / 64
                val j = element.ordinal % 64
                field[i] = field[i] or (1L shl j)
            }
            return ImmutableJumboEnumSet(field, universe)
        }

        fun <T : Enum<T>> allOf(universe: Array<T>): JumboEnumSet<T> {
            val field = LongArray(universe.size.ceilToMultipleOf(64) / 64)
            for (i in field.indices) {
                if (i == field.size - 1) {
                    field[i] = -1L ushr (64 - universe.size % 64)
                } else {
                    field[i] = -1L
                }
            }
            return ImmutableJumboEnumSet(field, universe)
        }
    }

    override val size: Int
        get() = bitfield.sumOf { it.countOneBits() }

    override fun isEmpty(): Boolean {
        return bitfield.all { it == 0L }
    }

    override fun iterator(): Iterator<T> {
        return iterator {
            for (value in universe) {
                val i = value.ordinal / 64
                val j = value.ordinal % 64
                if (bitfield[i] and (1L shl j) != 0L) {
                    yield(value)
                }
            }
        }
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        if (elements is JumboEnumSet<T>) {
            return bitfield.zip(elements.bitfield).all { it.first and it.second == it.second }
        }
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun contains(element: T): Boolean {
        val i = element.ordinal / 64
        val j = element.ordinal % 64
        return bitfield[i] and (1L shl j) != 0L
    }

    override fun toString(): String {
        return buildString {
            append("{")
            val iterator = this@JumboEnumSet.iterator()
            while (iterator.hasNext()) {
                append(iterator.next())
                if (iterator.hasNext()) {
                    append(", ")
                }
            }
            append("}")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is JumboEnumSet<*>) {
            if (!other.universe.contentEquals(universe)) return false
            return bitfield.contentEquals(other.bitfield)
        } else if (other is Set<*>) {
            if (size != other.size) return false
            return containsAll(other)
        }
        return false
    }

    override fun hashCode(): Int {
        var result = 0
        for (value in this) {
            result += value.hashCode()
        }
        return result
    }

}

@PublishedApi
internal class ImmutableJumboEnumSet<T : Enum<T>>(bitfield: LongArray, universe: Array<T>) : JumboEnumSet<T>(
    bitfield,
    universe
)