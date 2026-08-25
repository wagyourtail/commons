package xyz.wagyourtail.commonskt.collection.sorted

interface MutableSortedCollection<E> : SortedCollection<E>, MutableCollection<E> {

    fun removeFirst(): E

    fun removeLast(): E

    override fun asReversed(): MutableSortedCollection<E>
}

interface SortedCollection<E> : Collection<E> {
    val comparator: Comparator<E>

    fun first(): E

    fun last(): E

    fun asReversed(): SortedCollection<E>
}
