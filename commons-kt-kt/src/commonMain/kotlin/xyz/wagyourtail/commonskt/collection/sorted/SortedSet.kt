package xyz.wagyourtail.commonskt.collection.sorted

interface SortedSet<E> : Set<E> {

    val comparator: Comparator<E>

    fun first(): E

    fun last(): E

    fun headSet(toElement: E): SortedSet<E>

    fun tailSet(fromElement: E): SortedSet<E>

    fun subSet(fromElement: E, toElement: E): SortedSet<E> = tailSet(fromElement).headSet(toElement)

}

interface MutableSortedSet<E> : SortedSet<E>, MutableSet<E> {

    override fun headSet(toElement: E): MutableSortedSet<E>

    override fun tailSet(fromElement: E): MutableSortedSet<E>

    override fun subSet(fromElement: E, toElement: E): MutableSortedSet<E> = tailSet(fromElement).headSet(toElement)

}