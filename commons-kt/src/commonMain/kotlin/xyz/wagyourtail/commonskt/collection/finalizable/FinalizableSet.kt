package xyz.wagyourtail.commonskt.collection.finalizable

open class FinalizableSet<E>(backing: MutableSet<E>): FinalizableCollection<E, MutableSet<E>>(backing), MutableSet<E>, Set<E> by backing {

    override fun iterator(): MutableIterator<E> {
        return super.iterator()
    }

    override fun contains(element: E): Boolean {
        return backing.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return backing.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return backing.isEmpty()
    }

    override val size: Int
        get() = backing.size

}

fun <E> finalizableSetOf(vararg elements: E): FinalizableSet<E> {
    return FinalizableSet(mutableSetOf(*elements))
}