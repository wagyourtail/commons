package xyz.wagyourtail.commonskt.collection.finalizable

class FinalizableList<E>(backing: MutableList<E>) : FinalizableCollection<E, MutableList<E>>(backing),
    MutableList<E>,
    List<E> by backing
{

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

    override fun add(index: Int, element: E) {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized list")
        backing.add(index, element)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized list")
        return backing.addAll(index, elements)
    }

    override fun iterator(): MutableIterator<E> {
        return super.iterator()
    }

    override fun listIterator(): MutableListIterator<E> {
        return FinalizableListIterator(backing.listIterator()).also {
            if (finalized) it.finalize()
        }
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        return FinalizableListIterator(backing.listIterator(index)).also {
            if (finalized) it.finalize()
        }
    }

    override fun removeAt(index: Int): E {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized list")
        return backing.removeAt(index)
    }

    override fun set(index: Int, element: E): E {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized list")
        return backing.set(index, element)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        return FinalizableList(backing.subList(fromIndex, toIndex)).also {
            if (finalized) it.finalize()
        }
    }

}

fun <E> finalizableListOf(vararg elements: E): FinalizableList<E> {
    return FinalizableList(mutableListOf(*elements))
}