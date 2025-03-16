package xyz.wagyourtail.commonskt.collection.enum

import xyz.wagyourtail.commonskt.utils.ceilToMultipleOf

@PublishedApi
internal class MutableJumboEnumSet<T: Enum<T>>(bitfield: LongArray, universe: Array<T>): JumboEnumSet<T>(bitfield, universe), MutableEnumSet<T> {

    companion object {

        fun <T: Enum<T>> noneOf(universe: Array<T>): MutableJumboEnumSet<T> {
            val field = LongArray(universe.size.ceilToMultipleOf(64) / 64)
            return MutableJumboEnumSet(field, universe)
        }

        fun <T: Enum<T>> of(element: T, universe: Array<T>): MutableJumboEnumSet<T> {
            val field = LongArray(universe.size.ceilToMultipleOf(64) / 64)
            val i = element.ordinal / 64
            val j = element.ordinal % 64
            field[i] = field[i] or (1L shl j)
            return MutableJumboEnumSet(field, universe)
        }

        fun <T: Enum<T>> of(vararg elements: T, universe: Array<T>): MutableJumboEnumSet<T> {
            val field = LongArray(universe.size.ceilToMultipleOf(64) / 64)
            for (element in elements) {
                val i = element.ordinal / 64
                val j = element.ordinal % 64
                field[i] = field[i] or (1L shl j)
            }
            return MutableJumboEnumSet(field, universe)
        }

        fun <T: Enum<T>> allOf(universe: Array<T>): MutableJumboEnumSet<T> {
            val field = LongArray(universe.size.ceilToMultipleOf(64) / 64)
            for (i in field.indices) {
                if (i == field.size - 1) {
                    field[i] = -1L ushr (64 - universe.size % 64)
                } else {
                    field[i] = -1L
                }
            }
            return MutableJumboEnumSet(field, universe)
        }

    }

    override fun add(element: T): Boolean {
        val i = element.ordinal / 64
        val j = element.ordinal % 64
        if (bitfield[i] and (1L shl j) != 0L) return false
        bitfield[i] = bitfield[i] or (1L shl j)
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        if (elements is JumboEnumSet<T>) {
            var b = false
            for (i in elements.bitfield.indices) {
                val new = bitfield[i] or elements.bitfield[i]
                if (new == bitfield[i]) continue
                bitfield[i] = new
                b = true
            }
            return b
        }
        var b = false
        for (element in elements) {
            b = add(element) || b
        }
        return b
    }

    override fun clear() {
        for (i in bitfield.indices) {
            bitfield[i] = 0L
        }
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
                val i = last.ordinal / 64
                val j = last.ordinal % 64
                bitfield[i] = bitfield[i] and (1L shl j).inv()
            }

        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        if (elements is JumboEnumSet<T>) {
            var b = false
            for (i in elements.bitfield.indices) {
                val new = bitfield[i] and elements.bitfield[i]
                if (new == bitfield[i]) continue
                bitfield[i] = new
                b = true
            }
            return b
        }
        val new = LongArray(bitfield.size)
        for (element in elements) {
            val i = element.ordinal / 64
            val j = element.ordinal % 64
            new[i] = new[i] or (1L shl j)
        }
        var b = false
        for (i in bitfield.indices) {
            val new = bitfield[i] and new[i]
            if (new == bitfield[i]) continue
            bitfield[i] = new
            b = true
        }
        return b
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        if (elements is JumboEnumSet<T>) {
            var b = false
            for (i in elements.bitfield.indices) {
                val new = bitfield[i] and elements.bitfield[i].inv()
                if (new == bitfield[i]) continue
                bitfield[i] = new
                b = true
            }
            return b
        }
        var b = false
        for (element in elements) {
            b = remove(element) || b
        }
        return b
    }

    override fun remove(element: T): Boolean {
        val i = element.ordinal / 64
        val j = element.ordinal % 64
        if (bitfield[i] and (1L shl j) == 0L) return false
        bitfield[i] = bitfield[i] and (1L shl j).inv()
        return true
    }

}