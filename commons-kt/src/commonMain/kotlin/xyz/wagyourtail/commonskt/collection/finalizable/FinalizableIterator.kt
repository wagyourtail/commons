package xyz.wagyourtail.commonskt.finalizable

open class FinalizableIterator<E, T: MutableIterator<E>>(val backing: T) : MutableIterator<E>, Iterator<E> by backing {
    protected var finalized = false

    fun finalize() {
        finalized = true
    }

    override fun remove() {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized iterator")
        backing.remove()
    }
}