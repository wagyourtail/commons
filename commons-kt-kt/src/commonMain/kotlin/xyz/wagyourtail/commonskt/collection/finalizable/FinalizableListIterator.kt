package xyz.wagyourtail.commonskt.collection.finalizable

class FinalizableListIterator<E>(backing: MutableListIterator<E>) :
    FinalizableIterator<E, MutableListIterator<E>>(backing),
    MutableListIterator<E>,
    ListIterator<E> by backing {
    override fun add(element: E) {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized list iterator")
        backing.add(element)
    }

    override fun set(element: E) {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized list iterator")
        backing.set(element)
    }

    override fun hasNext(): Boolean {
        return backing.hasNext()
    }

    override fun hasPrevious(): Boolean {
        return backing.hasPrevious()
    }

    override fun nextIndex(): Int {
        return backing.nextIndex()
    }

    override fun previous(): E {
        return backing.previous()
    }

    override fun previousIndex(): Int {
        return backing.previousIndex()
    }

    override fun next(): E {
        return backing.next()
    }

}