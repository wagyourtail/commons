package xyz.wagyourtail.commonskt.collection.small

class SmallArraySet<E>(val initialSize: Int = 0, val resizeAmount: Int = 1) : AbstractMutableSet<E>() {
    private var backing = arrayOfNulls<Any>(initialSize)

    override var size: Int = 0
        private set

    override fun iterator(): MutableIterator<E> {
        return object : MutableIterator<E> {
            var i = 0

            override fun hasNext(): Boolean = i < size

            override fun next(): E {
                if (!hasNext()) throw NoSuchElementException()
                return backing[i++] as E
            }

            override fun remove() {
                if (i > 0 && i <= backing.size) {
                    val index = i - 1
                    removeAt(index)
                    i = index
                } else {
                    throw IllegalStateException()
                }
            }
        }
    }

    private fun removeAt(index: Int) {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("Index: $index, Size: $size")
        if (index == --size) {
            if (size < backing.size - resizeAmount) {
                val newBacking = arrayOfNulls<Any>(backing.size - resizeAmount)
                backing.copyInto(newBacking, 0, 0, index)
                backing = newBacking
            }
        } else {
            if (size < backing.size - resizeAmount) {
                val newBacking = arrayOfNulls<Any>(backing.size - resizeAmount)
                backing.copyInto(newBacking, 0, 0, index)
                backing.copyInto(newBacking, index, index + 1, size)
                backing = newBacking
            } else {
                backing.copyInto(backing, index, index + 1)
            }
        }
    }

    override fun add(element: E): Boolean {
        val i = backing.indexOf(element)
        if (i > -1) return false
        if (size == backing.size) {
            val newBacking = arrayOfNulls<Any>(backing.size + resizeAmount)
            backing.copyInto(newBacking, 0, 0, size)
            backing = newBacking
        }
        backing[size++] = element
        return true
    }

    override fun remove(element: E): Boolean {
        val i = backing.indexOf(element)
        if (i > -1) {
            removeAt(i)
            return true
        }
        return false
    }

    override fun clear() {
        size = 0
        backing = arrayOfNulls(initialSize)
    }
}

fun <E> smallArraySetOf(vararg elements: E, resizeAmount: Int = 1, initialSize: Int = elements.size): SmallArraySet<E> {
    val set = SmallArraySet<E>(initialSize, resizeAmount)
    set.addAll(elements)
    return set
}
