package xyz.wagyourtail.commonskt.utils

import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

operator fun <T: Comparable<T>> Iterable<T>.compareTo(other: Iterable<T>): Int {
    val i1 = iterator()
    val i2 = other.iterator()
    while (i1.hasNext() && i2.hasNext()) {
        val e1 = i1.next()
        val e2 = i2.next()
        val cmp = e1.compareTo(e2)
        if (cmp != 0) return cmp
    }
    if (i1.hasNext()) return 1
    if (i2.hasNext()) return -1
    return 0
}

inline fun <T> Iterable<T>.compareTo(other: Iterable<T>, comparator: (T, T) -> Int): Int {
    val i1 = iterator()
    val i2 = other.iterator()
    while (i1.hasNext() && i2.hasNext()) {
        val e1 = i1.next()
        val e2 = i2.next()
        val cmp = comparator(e1, e2)
        if (cmp != 0) return cmp
    }
    if (i1.hasNext()) return 1
    if (i2.hasNext()) return -1
    return 0
}

inline fun <E, K, V> Iterable<E>.associateNonNull(apply: (E) -> Pair<K, V>?): Map<K, V> {
    val mut = mutableMapOf<K, V>()
    for (e in this) {
        apply(e)?.let {
            mut.put(it.first, it.second)
        }
    }
    return mut
}

inline fun <K, V> Iterable<K>.associateWithNonNull(apply: (K) -> V?): Map<K, V> {
    val mut = mutableMapOf<K, V>()
    for (e in this) {
        apply(e)?.let {
            mut.put(e, it)
        }
    }
    return mut
}

inline fun <E, K, V> Iterable<E>.mutliAssociate(apply: (E) -> Pair<K, V>): Map<K, List<V>> {
    val mut = mutableMapOf<K, MutableList<V>>()
    for (e in this) {
        val (k, v) = apply(e)
        mut.getOrPut(k) { mutableListOf() } += v
    }
    return mut
}


fun <T> Iterable<T>.repeat(n: Int): List<T> {
    return buildList {
        for (i in 0 until n) {
            addAll(this@repeat)
        }
    }
}

fun <T> Iterable<T>.permutations(size: Int): Sequence<List<T>> {
    if (size == 1) {
        return this.map { listOf(it) }.asSequence()
    }
    val queue = this@permutations.toMutableList()
    return sequence {
        while (queue.size >= size) {
            val first = queue.removeAt(0)
            val perms = queue.permutations(size - 1)
            yieldAll(perms.map { listOf(first) + it })
        }
    }
}

fun <T> Iterator<T>.withDelimiter(delimiter: T): Iterator<T> {
    return iterator {
        if (!this@withDelimiter.hasNext()) {
            return@iterator
        }
        yield(this@withDelimiter.next())
        while (this@withDelimiter.hasNext()) {
            yield(delimiter)
            yield(this@withDelimiter.next())
        }
    }
}

fun <T> Iterable<T>.withDelimiter(delimiter: T): Iterable<T> {
    return object : Iterable<T> {
        override fun iterator(): Iterator<T> {
            return this@withDelimiter.iterator().withDelimiter(delimiter)
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <K, V> Iterable<Pair<K, V?>>.filterNotNullValues(): List<Pair<K, V>> =
    filter { it.second != null } as List<Pair<K, V>>

@Suppress("UNCHECKED_CAST")
@JvmName("filterNotNullValuesIndexed")
fun <V> Iterable<IndexedValue<V?>>.filterNotNullValues(): List<IndexedValue<V>> =
    filter { it.value != null } as List<IndexedValue<V>>


@OptIn(ExperimentalTypeInference::class)
fun <E> iterable(@BuilderInference block: suspend SequenceScope<E>.() -> Unit): Iterable<E> {
    return object : Iterable<E> {
        override fun iterator() = iterator(block)
    }
}
