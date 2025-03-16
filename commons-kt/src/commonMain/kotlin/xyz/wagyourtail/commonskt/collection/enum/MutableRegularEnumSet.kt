package xyz.wagyourtail.commonskt.collection.enum

@PublishedApi
internal class MutableRegularEnumSet<T: Enum<T>>(override var bitfield: Long, universe: Array<T>) : RegularEnumSet<T>(universe), MutableEnumSet<T> {

    companion object {

        fun <T: Enum<T>> noneOf(universe: Array<T>): MutableRegularEnumSet<T> {
            return MutableRegularEnumSet(0L, universe)
        }

        fun <T: Enum<T>> of(value: T, universe: Array<T>): MutableRegularEnumSet<T> {
            return MutableRegularEnumSet(1L shl value.ordinal, universe)
        }

        fun <T: Enum<T>> of(vararg values: T, universe: Array<T>): MutableRegularEnumSet<T> {
            var bitfield = 0L
            for (value in values) {
                bitfield = bitfield or (1L shl value.ordinal)
            }
            return MutableRegularEnumSet(bitfield, universe)
        }

        fun <T: Enum<T>> allOf(universe: Array<T>): MutableRegularEnumSet<T> {
            return MutableRegularEnumSet(-1L ushr (64 - universe.size), universe)
        }
    }

    override val size: Int
        get() = bitfield.countOneBits()

    override fun clear() {
        bitfield = 0L
    }

    override fun addAll(elements: Collection<T>): Boolean {
        if (elements is RegularEnumSet<T>) {
            val new = bitfield or elements.bitfield
            if (new == bitfield) return false
            bitfield = new
            return true
        }
        var b = false
        for (element in elements) {
            b = add(element) || b
        }
        return b
    }

    override fun add(element: T): Boolean {
        if (contains(element)) return false
        bitfield = bitfield or (1L shl element.ordinal)
        return true
    }

    override fun isEmpty(): Boolean {
        return bitfield == 0L
    }

    override fun iterator(): MutableIterator<T> {
        val iter = super.iterator()
        return object : MutableIterator<T> {
            var last: T? = null

            override fun hasNext(): Boolean {
                return iter.hasNext()
            }

            override fun next(): T {
                return iter.next().also { last = it }
            }

            override fun remove() {
                val last = last ?: throw IllegalStateException("next must be called before remove")
                bitfield = bitfield and (1L shl last.ordinal).inv()
            }

        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        if (elements is RegularEnumSet<T>) {
            val new = bitfield and elements.bitfield
            if (new == bitfield) return false
            bitfield = new
            return true
        }
        var new = 0L
        for (element in elements) {
            new = new or (1L shl element.ordinal)
        }
        if (new == bitfield) return false
        bitfield = new
        return true
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        if (elements is RegularEnumSet<T>) {
            val new = bitfield and elements.bitfield.inv()
            if (new == bitfield) return false
            bitfield = new
            return true
        }
        var b = false
        for (element in elements) {
            b = remove(element) || b
        }
        return b
    }

    override fun remove(element: T): Boolean {
        if (!contains(element)) return false
        bitfield = bitfield and (1L shl element.ordinal).inv()
        return true
    }

}
