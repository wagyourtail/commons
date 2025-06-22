package xyz.wagyourtail.commonskt.utils

import kotlin.jvm.JvmName

@Deprecated("use List.compareTo")
class ListCompare<T : Comparable<T>>(val list: List<T>) : Comparable<ListCompare<T>> {

    override operator fun compareTo(other: ListCompare<T>): Int {
        val size = list.size
        if (size != other.list.size) return size - other.list.size
        for (i in 0 until size) {
            val cmp = list[i].compareTo(other.list[i])
            if (cmp != 0) return cmp
        }
        return 0
    }

}

@Deprecated("use List.compareTo")
fun <T : Comparable<T>> compareable(vararg elements: T) = ListCompare(elements.toList())

@Deprecated("use List.compareTo")
fun <T : Comparable<T>> List<T>.comparable() = ListCompare(this)

operator fun <T: Comparable<T>> List<T>.compareTo(other: List<T>): Int {
    val size = size
    if (size != other.size) return size - other.size
    for (i in 0 until size) {
        val cmp = this[i].compareTo(other[i])
        if (cmp != 0) return cmp
    }
    return 0
}

fun <T> List<T>.compareTo(other: List<T>, comparator: (T, T) -> Int): Int {
    val size = size
    if (size != other.size) return size - other.size
    for (i in 0 until size) {
        val cmp = comparator(this[i], other[i])
        if (cmp != 0) return cmp
    }
    return 0
}

fun <K, V> Map<K, V>.firstAsMap(): Map<K, V> {
    val entry = entries.first()
    return mapOf(entry.key to entry.value)
}

fun <K, V> Map<K, V>.firstAsMutableMap(): MutableMap<K, V> {
    val entry = entries.first()
    return mutableMapOf(entry.key to entry.value)
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

fun <K, V> MutableMap<K, MutableList<V>>.putMulti(key: K, value: V) {
    getOrPut(key) { mutableListOf() } += value
}

@JvmName("putMultiSet")
fun <K, V> MutableMap<K, MutableSet<V>>.putMulti(key: K, value: V) {
    getOrPut(key) { mutableSetOf() } += value
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

@Suppress("UNCHECKED_CAST")
fun <K, V> Iterable<Pair<K, V?>>.filterNotNullValues(): List<Pair<K, V>> =
    filter { it.second != null } as List<Pair<K, V>>

@Suppress("UNCHECKED_CAST")
@JvmName("filterNotNullValuesIndexed")
fun <V> Iterable<IndexedValue<V?>>.filterNotNullValues(): List<IndexedValue<V>> =
    filter { it.value != null } as List<IndexedValue<V>>

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V?>.filterNotNullValues(): Map<K, V> = filterValues { it != null } as Map<K, V>

inline fun <K, V, U> Map<K, V>.mapNotNullValues(mapper: (Map.Entry<K, V>) -> U?): Map<K, U> =
    mapValues(mapper).filterNotNullValues()

fun <E> MutableList<E>.insertBefore(element: E, toAdd: E) {
    val i = indexOf(element)
    if (i < 0) throw NoSuchElementException()
    add(i, toAdd)
}

fun <E> MutableList<E>.insertBefore(element: E, vararg toAdd: E) {
    val i = indexOf(element)
    if (i < 0) throw NoSuchElementException()
    addAll(i, toAdd.toList())
}

fun <E> MutableList<E>.insertAfter(element: E, toAdd: E) {
    val i = indexOf(element)
    if (i < 0) throw NoSuchElementException()
    add(i + 1, toAdd)
}

fun <E> MutableList<E>.insertAfter(element: E, vararg toAdd: E) {
    val i = indexOf(element)
    if (i < 0) throw NoSuchElementException()
    addAll(i + 1, toAdd.toList())
}

fun <E> MutableList<E>.replace(element: E, toAdd: E) {
    val i = indexOf(element)
    if (i < 0) throw NoSuchElementException()
    set(i, toAdd)
}

fun Map<String, String>.resolveArgs(args: List<String>, hasDollar: Boolean = true): List<String> {
    @Suppress("RegExpRedundantEscape") // kotlin required it: Invalid regular expression: /\$\{([^}]+)}/gu: Lone quantifier brackets
    val pattern = Regex((if (hasDollar) "\\$" else "") + "\\{([^}]+)\\}")
    return args.map { arg ->
        pattern.replace(arg) {
            val key = it.groups[1]!!.value
            if (!this.containsKey(key)) throw IllegalArgumentException("Property $key not found")
            this.getValue(key)
        }
    }
}
