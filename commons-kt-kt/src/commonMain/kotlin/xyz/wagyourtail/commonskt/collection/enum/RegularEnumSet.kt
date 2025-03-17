package xyz.wagyourtail.commonskt.collection.enum

@PublishedApi
internal sealed class RegularEnumSet<T : Enum<T>>(val universe: Array<T>) : EnumSet<T> {

    companion object {

        fun <T : Enum<T>> noneOf(universe: Array<T>): EnumSet<T> {
            return ImmutableRegularEnumSet(0L, universe)
        }

        fun <T : Enum<T>> of(value: T, universe: Array<T>): EnumSet<T> {
            return ImmutableRegularEnumSet(1L shl value.ordinal, universe)
        }

        fun <T : Enum<T>> of(vararg values: T, universe: Array<T>): EnumSet<T> {
            var bitfield = 0L
            for (value in values) {
                bitfield = bitfield or (1L shl value.ordinal)
            }
            return ImmutableRegularEnumSet(bitfield, universe)
        }

        fun <T : Enum<T>> allOf(universe: Array<T>): EnumSet<T> {
            return ImmutableRegularEnumSet(-1L ushr (64 - universe.size), universe)
        }

    }

    abstract val bitfield: Long

    override val size: Int
        get() = bitfield.countOneBits()

    override fun isEmpty(): Boolean {
        return bitfield == 0L
    }

    override fun iterator(): Iterator<T> {
        return iterator {
            for (value in universe) {
                if (bitfield and (1L shl value.ordinal) != 0L) {
                    yield(value)
                }
            }
        }
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        if (elements is RegularEnumSet<T>) {
            return bitfield and elements.bitfield == elements.bitfield
        }
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun contains(element: T): Boolean {
        return bitfield and (1L shl element.ordinal) != 0L
    }

    override fun toString(): String {
        return buildString {
            append("{")
            val iterator = this@RegularEnumSet.iterator()
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
        if (other is RegularEnumSet<*>) {
            return other.universe.contentEquals(universe) && bitfield == other.bitfield
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
internal class ImmutableRegularEnumSet<T : Enum<T>>(
    override val bitfield: Long,
    universe: Array<T>
) : RegularEnumSet<T>(universe)
