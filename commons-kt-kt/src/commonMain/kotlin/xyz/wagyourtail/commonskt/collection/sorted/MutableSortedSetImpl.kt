package xyz.wagyourtail.commonskt.collection.sorted

class MutableSortedSetImpl<E>(
    comparator: Comparator<E>,
    elements: MutableList<E> = mutableListOf(),
    from: E? = null,
    to: E? = null,
) : MutableSortedCollectionImpl<E>(
    comparator,
    elements,
    from,
    to,
    false,
), MutableSortedSet<E> {

    override fun headSet(toElement: E): MutableSortedSetImpl<E> {
        if (to == null) {
            return MutableSortedSetImpl(comparator, elements, from = from, to = toElement)
        } else {
            // figure out which to is less
            val newTo = if (comparator.compare(to, toElement) < 0) to else toElement
            return MutableSortedSetImpl(comparator, elements, from = from, to = newTo)
        }
    }

    override fun tailSet(fromElement: E): MutableSortedSetImpl<E> {
        if (from == null) {
            return MutableSortedSetImpl(comparator, elements, from = fromElement, to = to)
        } else {
            // figure out which from is greater
            val newFrom = if (comparator.compare(from, fromElement) > 0) from else fromElement
            return MutableSortedSetImpl(comparator, elements, from = newFrom, to = to)
        }
    }

    override fun removeFirst(): E {
        if (fromIndex == toIndex) throw IndexOutOfBoundsException()
        return elements.removeAt(fromIndex)
    }

    override fun removeLast(): E {
        if (fromIndex == toIndex) throw IndexOutOfBoundsException()
        return elements.removeAt(toIndex - 1)
    }

    override fun asReversed(): MutableSortedSetImpl<E> {
        // a, b, c, d, e
        // b, c -> c, b
        // [b, d) -> [c, a)
        val fromExclusive = elements.getOrNull(fromIndex - 1)
        val toInclusive = elements.getOrNull(toIndex - 1)
        return MutableSortedSetImpl(
            comparator.reversed(), elements.asReversed(), from = toInclusive, to = fromExclusive
        )
    }

}

fun <E : Comparable<E>> mutableSortedSetOf(vararg elements: E): MutableSortedSet<E> {
    val set = MutableSortedSetImpl<E>(naturalOrder())
    set.addAll(elements.toList())
    return set
}

fun <E> mutableSortedSetOf(vararg elements: E, comparator: Comparator<E>): MutableSortedSet<E> {
    val set = MutableSortedSetImpl(comparator)
    set.addAll(elements.toList())
    return set
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
