package xyz.wagyourtail.commonskt.collection.finalizable

open class FinalizableIterable<E, T : MutableIterable<E>>(val backing: T) :
    MutableIterable<E>,
    Iterable<E> by backing,
    Finalizable
{
    protected var finalized = false

    override fun finalize() {
        finalized = true
        iterator().forEach { if (it is Finalizable) it.finalize() }
    }

    override fun iterator(): MutableIterator<E> {
        return FinalizableIterator(backing.iterator()).also {
            if (finalized) it.finalize()
        }
    }

}