package xyz.wagyourtail.commonskt.collection.sorted

open class MutableSortedCollectionImpl<E>(
    override val comparator: Comparator<E>,
    val elements: MutableList<E> = mutableListOf(),
    val from: E? = null,
    val to: E? = null,
    val allowMultiple: Boolean = true,
) : MutableSortedCollection<E> {

    protected val fromIndex: Int
        get() {
            val fromIndex = from?.let { elements.binarySearch(it, comparator) } ?: 0
            return if (fromIndex < 0) -fromIndex - 1 else fromIndex
        }

    protected val toIndex: Int
        get() {
            val toIndex = to?.let { elements.binarySearch(it, comparator) } ?: elements.size
            return if (toIndex < 0) -toIndex - 1 else toIndex
        }

    init {
        if (fromIndex > toIndex) throw IllegalArgumentException("$from > $to")
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
        return elements.subList(fromIndex, toIndex).iterator()
    }

    override fun add(element: E): Boolean {
        // update ends
        if (from.let {
                it != null && comparator.compare(
                    element, it
                ) < 0
            }) throw IndexOutOfBoundsException("$element is < $from")
        if (to.let {
                it != null && comparator.compare(
                    element, it
                ) > 0
            }) throw IndexOutOfBoundsException("$element is >= $to")
        var index = elements.binarySearch(element, comparator)
        if (index >= 0 && !allowMultiple) return false
        if (index < 0) index = -index - 1
        elements.add(index, element)
        return true
    }

    override fun remove(element: E): Boolean {
        val index = elements.binarySearch(element, comparator)
        if (index < 0 || index !in fromIndex until toIndex) return false
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
            (from?.let { comparator.compare(e, it) } ?: 0) >= 0 && (to?.let { comparator.compare(e, it) } ?: 0) <= 0
        } - elements.toSet()
        return removeAll(toRemove)
    }

    override fun clear() {
        if (to != null || from != null) {
            elements.removeAll { e ->
                (from?.let { comparator.compare(e, it) } ?: 0) >= 0 && (to?.let { comparator.compare(e, it) } ?: 0) <= 0
            }
        } else {
            elements.clear()
        }
    }

    override fun first(): E {
        if (fromIndex == toIndex) throw IndexOutOfBoundsException()
        return elements[fromIndex]
    }

    override fun last(): E {
        if (fromIndex == toIndex) throw IndexOutOfBoundsException()
        return elements[toIndex - 1]
    }

    override fun removeFirst(): E {
        if (fromIndex == toIndex) throw IndexOutOfBoundsException()
        return elements.removeAt(fromIndex)
    }

    override fun removeLast(): E {
        if (fromIndex == toIndex) throw IndexOutOfBoundsException()
        return elements.removeAt(toIndex - 1)
    }

    override fun asReversed(): MutableSortedCollectionImpl<E> {
        // a, b, c, d, e
        // b, c -> c, b
        // [b, d) -> [c, a)
        val fromExclusive = elements.getOrNull(fromIndex - 1)
        val toInclusive = elements.getOrNull(toIndex - 1)
        return MutableSortedCollectionImpl(
            comparator.reversed(), elements.asReversed(), toInclusive, fromExclusive, allowMultiple
        )
    }
}

