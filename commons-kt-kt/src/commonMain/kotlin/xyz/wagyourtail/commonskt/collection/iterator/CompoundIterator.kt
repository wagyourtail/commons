package xyz.wagyourtail.commonskt.collection.iterator

import kotlin.jvm.JvmName

operator fun <T> Iterator<T>.plus(other: Iterator<T>): Iterator<T> {
    if (this is CompoundIterator) {
        this.iterators.add(other)
    }
    return CompoundIterator(mutableListOf(this, other))
}

@JvmName("mutablePlus")
operator fun <T> MutableIterator<T>.plus(other: Iterator<T>): MutableIterator<T> {
    if (this is CompoundIterator) {
        this.iterators.add(other)
    }
    return CompoundIterator(mutableListOf(this, other))
}

class CompoundIterator<T>(val iterators: MutableList<Iterator<T>> = mutableListOf()) : MutableIterator<T> {
    var current: Iterator<T> = iterators.removeFirst()
    var previous: Iterator<T>? = null

    override fun next(): T {
        previous = current
        return current.next()
    }

    override fun hasNext(): Boolean {
        while (!current.hasNext() && iterators.isNotEmpty()) {
            current = iterators.removeFirst()
        }
        return current.hasNext()
    }

    override fun remove() {
        if (previous is MutableIterator<T>) {
            (previous as MutableIterator<T>).remove()
        } else {
            throw IllegalStateException()
        }
    }

}