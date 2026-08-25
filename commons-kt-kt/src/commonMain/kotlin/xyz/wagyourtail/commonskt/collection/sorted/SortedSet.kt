package xyz.wagyourtail.commonskt.collection.sorted

interface SortedSet<E> : SortedCollection<E>, Set<E> {

    fun headSet(toElement: E): SortedSet<E>

    fun tailSet(fromElement: E): SortedSet<E>

    fun subSet(fromElement: E, toElement: E): SortedSet<E> = tailSet(fromElement).headSet(toElement)

    override fun asReversed(): SortedSet<E>
}

interface MutableSortedSet<E> : SortedSet<E>, MutableSortedCollection<E>, MutableSet<E> {

    override fun headSet(toElement: E): MutableSortedSet<E>

    override fun tailSet(fromElement: E): MutableSortedSet<E>

    override fun subSet(fromElement: E, toElement: E): MutableSortedSet<E> = tailSet(fromElement).headSet(toElement)

    override fun asReversed(): MutableSortedSet<E>
}