package xyz.wagyourtail.commonskt.collection.small

class SmallArrayList<E>(initialSize: Int = 0, val resizeAmount: Int = 1) : AbstractMutableList<E>() {
    private var backing = arrayOfNulls<Any>(initialSize)

    override var size: Int = 0
        private set

    override fun set(index: Int, element: E): E {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("Index: $index, Size: $size")
        backing[index] = element
        return element
    }

    override fun add(index: Int, element: E) {
        if (index < 0 || index > size) throw IndexOutOfBoundsException("Index: $index, Size: $size")
        if (size == backing.size) {
            val newBacking = arrayOfNulls<Any>(backing.size + resizeAmount)
            backing.copyInto(newBacking, 0, 0, index)
            if (index < size) backing.copyInto(newBacking, index + 1, index, size)
            backing = newBacking
        } else if (index < size) {
            backing.copyInto(backing, index + 1, index, size)
        }
        size++
        backing[index] = element
    }

    override fun removeAt(index: Int): E {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("Index: $index, Size: $size")
        val element = backing[index] as E
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
        return element
    }

    override fun get(index: Int): E {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("Index: $index, Size: $size")
        return backing[index] as E
    }

    override fun clear() {
        size = 0
        backing = arrayOfNulls(resizeAmount)
    }

}

fun <E> smallArrayListOf(vararg elements: E, resizeAmount: Int = 1, initialSize: Int = elements.size): SmallArrayList<E> {
    val list = SmallArrayList<E>(initialSize, resizeAmount)
    list.addAll(elements)
    return list
}