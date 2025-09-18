@file:OptIn(ExperimentalTypeInference::class)

package xyz.wagyourtail.commonskt.utils

import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

fun <K, V> Map<K, V>.firstAsMap(): Map<K, V> {
    val entry = entries.first()
    return mapOf(entry.key to entry.value)
}

fun <K, V> Map<K, V>.firstAsMutableMap(): MutableMap<K, V> {
    val entry = entries.first()
    return mutableMapOf(entry.key to entry.value)
}

fun <K, V> MutableMap<K, MutableList<V>>.putMulti(key: K, value: V) {
    getOrPut(key) { mutableListOf() } += value
}

@JvmName("putMultiSet")
fun <K, V> MutableMap<K, MutableSet<V>>.putMulti(key: K, value: V) {
    getOrPut(key) { mutableSetOf() } += value
}

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

fun <E> Collection<E>.firstAndLast(): Pair<E, E> {
    return if (this.size < 2) {
        throw IndexOutOfBoundsException()
    } else {
        this.first() to this.last()
    }
}

fun <E> Collection<E>.firstAndMaybeLast(): List<E> {
    return if (this.size < 2) {
        listOf(this.first())
    } else {
        listOf(this.first(), this.last())
    }
}

fun <E> buildMutableList(@BuilderInference block: MutableList<E>.() -> Unit): MutableList<E> {
    val list = mutableListOf<E>()
    list.block()
    return list
}

fun <E> buildMutableSet(@BuilderInference block: MutableSet<E>.() -> Unit): MutableSet<E> {
    val set = mutableSetOf<E>()
    set.block()
    return set
}

fun <K, V> buildMutableMap(@BuilderInference block: MutableMap<K, V>.() -> Unit): MutableMap<K, V> {
    val map = mutableMapOf<K, V>()
    map.block()
    return map
}
