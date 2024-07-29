package xyz.wagyourtail.commonskt.finalizable

open class FinalizableCollection<E, T: MutableCollection<E>>(backing: T): FinalizableIterable<E, T>(backing), MutableCollection<E>, Collection<E> by backing {

    override fun iterator(): MutableIterator<E> {
        return super.iterator()
    }

    override fun add(element: E): Boolean {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized collection")
        return backing.add(element)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized collection")
        return backing.addAll(elements)
    }

    override fun clear() {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized collection")
        backing.clear()
    }

    override fun remove(element: E): Boolean {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized collection")
        return backing.remove(element)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized collection")
        return backing.removeAll(elements)
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized collection")
        return backing.retainAll(elements)
    }

}