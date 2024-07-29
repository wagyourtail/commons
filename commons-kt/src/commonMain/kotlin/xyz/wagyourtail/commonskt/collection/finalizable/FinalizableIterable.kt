package xyz.wagyourtail.commonskt.finalizable

open class FinalizableIterable<E, T: MutableIterable<E>>(val backing: T): MutableIterable<E>, Iterable<E> by backing {
    protected var finalized = false

    fun finalize() {
        finalized = true
    }

    override fun iterator(): MutableIterator<E> {
        return FinalizableIterator(backing.iterator()).also {
            if (finalized) it.finalize()
        }
    }

}