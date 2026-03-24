package xyz.wagyourtail.commonskt.collection.sorted

import kotlin.collections.mutableListOf

open class MutableSortedSetImpl<E>(
    val elements: MutableList<E>,
    override val comparator: Comparator<E>,
    val from: E? = null,
    val to: E? = null,
) : MutableSortedSet<E> {

    constructor(comparator: Comparator<E>) : this(mutableListOf<E>(), comparator)

    private val fromIndex: Int
        get() {
            val fromIndex = from?.let { elements.binarySearch(from, comparator) } ?: 0
            return if (fromIndex < 0) -fromIndex - 1 else fromIndex
        }

    private val toIndex: Int
        get() {
            val toIndex = to?.let { elements.binarySearch(to, comparator) } ?: elements.size
            return if (toIndex < 0) -toIndex - 1 else toIndex
        }

    override val size: Int
        get() = toIndex - fromIndex

    override fun isEmpty() = size == 0

    override fun contains(element: E): Boolean {
        return elements.binarySearch(element, comparator) in fromIndex until toIndex
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return elements.all { contains(it) }

    }

    override fun iterator(): MutableIterator<E> {
        val iter = elements.subList(fromIndex, toIndex).toList().iterator()
        return object : MutableIterator<E> {
            var current: E? = null
            override fun remove() {
                if (current == null) throw NoSuchElementException()
                elements.remove(current)
            }

            override fun next(): E {
                current = iter.next()
                return current!!
            }

            override fun hasNext(): Boolean {
                return iter.hasNext()
            }
        }
    }

    override fun add(element: E): Boolean {
        val index = elements.binarySearch(element, comparator)
        if (index >= 0) return false
        elements.add(-index - 1, element)
        return true
    }

    override fun remove(element: E): Boolean {
        val index = elements.binarySearch(element, comparator)
        if (index < 0) return false
        elements.removeAt(index)
        return true
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var any = false
        for (element in elements) {
            any = add(element) || any
        }
        return any
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var any = false
        for (element in elements) {
            any = remove(element) || any
        }
        return any
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        // collect all in range that aren't in elements
        val toRemove = elements.filter { e ->
            (from?.let { comparator.compare(e, from) } ?: 0) >= 0 &&
            (to?.let { comparator.compare(e, to) } ?: 0) <= 0
        } - elements.toSet()
        return removeAll(toRemove)
    }

    override fun clear() {
        if (to != null || from != null) {
            elements.removeAll { e ->
                (from?.let { comparator.compare(e, from) } ?: 0) >= 0 &&
                (to?.let { comparator.compare(e, to) } ?: 0) <= 0
            }
        } else {
            elements.clear()
        }
    }

    override fun first(): E {
        return elements[fromIndex]
    }

    override fun last(): E {
        return elements[toIndex - 1]
    }

    override fun headSet(toElement: E): MutableSortedSetImpl<E> {
        if (to == null) {
            return MutableSortedSetImpl(elements, comparator, from = from, to = toElement)
        } else {
            // figure out which to is less
            val newTo = if (comparator.compare(to, toElement) < 0) to else toElement
            return MutableSortedSetImpl(elements, comparator, from = from, to = newTo)
        }
    }

    override fun tailSet(fromElement: E): MutableSortedSetImpl<E> {
        if (from == null) {
            return MutableSortedSetImpl(elements, comparator, from = fromElement, to = to)
        } else {
            // figure out which from is greater
            val newFrom = if (comparator.compare(from, fromElement) > 0) from else fromElement
            return MutableSortedSetImpl(elements, comparator, from = newFrom, to = to)
        }
    }

}

fun <E : Comparable<E>> sortedSetOf(vararg elements: E): SortedSet<E> {
    val set = MutableSortedSetImpl<E>(naturalOrder())
    set.addAll(elements.toList())
    return set
}

fun <E> sortedSetOf(vararg elements: E, comparator: Comparator<E>): SortedSet<E> {
    val set = MutableSortedSetImpl(comparator)
    set.addAll(elements.toList())
    return set
}
