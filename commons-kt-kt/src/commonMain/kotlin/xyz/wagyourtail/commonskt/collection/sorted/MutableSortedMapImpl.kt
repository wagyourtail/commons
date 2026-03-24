package xyz.wagyourtail.commonskt.collection.sorted

import xyz.wagyourtail.commonskt.collection.MutableMapEntry

class MutableSortedMapImpl<K, V> private constructor(override val entries: MutableSortedSetImpl<MutableMap.MutableEntry<K, V>>, override val comparator: Comparator<K>) : MutableSortedMap<K, V> {

    constructor(comparator: Comparator<K>) : this(
        MutableSortedSetImpl(Comparator { a, b -> comparator.compare(a.key, b.key) }),
        comparator
    )

    data class KeyOnly<K, V>(override val key: K) : MutableMap.MutableEntry<K, V> {
        override val value: V
            get() = throw UnsupportedOperationException()

        override fun setValue(newValue: V): V {
            throw UnsupportedOperationException()
        }
    }

    override val keys: MutableSortedSet<K>
        get() = object : MutableSortedSet<K> {
            override val comparator: Comparator<K>
                get() = this@MutableSortedMapImpl.comparator

            override fun first(): K {
                return entries.first().key
            }

            override fun last(): K {
                return entries.last().key
            }

            override fun headSet(toElement: K): MutableSortedSet<K> {
                return entries.headSet(KeyOnly(toElement)).map { it.key }.let { keyList ->
                    MutableSortedSetImpl(keyList.toMutableList(), comparator)
                }
            }

            override fun tailSet(fromElement: K): MutableSortedSet<K> {
                return entries.tailSet(KeyOnly(fromElement)).map { it.key }.let { keyList ->
                    MutableSortedSetImpl(keyList.toMutableList(), comparator)
                }
            }

            override val size: Int
                get() = entries.size

            override fun isEmpty(): Boolean {
                return entries.isEmpty()
            }

            override fun contains(element: K): Boolean {
                return entries.contains(KeyOnly(element))
            }

            override fun containsAll(elements: Collection<K>): Boolean {
                return elements.all { contains(it) }
            }

            override fun iterator(): MutableIterator<K> {
                val entryIterator = entries.iterator()
                return object : MutableIterator<K> {
                    override fun hasNext(): Boolean = entryIterator.hasNext()
                    override fun next(): K = entryIterator.next().key
                    override fun remove() = entryIterator.remove()
                }
            }

            override fun add(element: K): Boolean {
                throw UnsupportedOperationException("Cannot add key directly to map")
            }

            override fun remove(element: K): Boolean {
                return entries.remove(KeyOnly(element))
            }

            override fun addAll(elements: Collection<K>): Boolean {
                throw UnsupportedOperationException("Cannot add keys directly to map")
            }

            override fun removeAll(elements: Collection<K>): Boolean {
                return elements.any { remove(it) }
            }

            override fun retainAll(elements: Collection<K>): Boolean {
                val elementsToKeep = elements.toSet()
                return entries.removeAll { entry -> entry.key !in elementsToKeep }
            }

            override fun clear() {
                entries.clear()
            }
        }

    override fun firstKey(): K {
        return entries.first().key
    }

    override fun lastKey(): K {
        return entries.last().key
    }

    override fun headMap(toKey: K): MutableSortedMapImpl<K, V> {
        return MutableSortedMapImpl(entries.headSet(KeyOnly(toKey)), comparator)
    }

    override fun tailMap(fromKey: K): MutableSortedMapImpl<K, V> {
        return MutableSortedMapImpl(entries.tailSet(KeyOnly(fromKey)), comparator)
    }

    override val size: Int
        get() = entries.size

    override fun isEmpty(): Boolean {
        return entries.isEmpty()
    }

    override fun containsKey(key: K): Boolean {
        return entries.contains(KeyOnly(key))
    }

    override fun containsValue(value: V): Boolean {
        return entries.any { it.value == value }
    }

    override fun get(key: K): V? {
        val index = entries.elements.binarySearch(KeyOnly(key), entries.comparator)
        return if (index >= 0) entries.elements[index].value else null
    }

    override val values: MutableCollection<V>
        get() = object : MutableCollection<V> {
            override val size: Int
                get() = entries.size

            override fun isEmpty(): Boolean = entries.isEmpty()

            override fun contains(element: V): Boolean {
                return entries.any { it.value == element }
            }

            override fun containsAll(elements: Collection<V>): Boolean {
                return elements.all { contains(it) }
            }

            override fun iterator(): MutableIterator<V> {
                val entryIterator = entries.iterator()
                return object : MutableIterator<V> {
                    override fun hasNext(): Boolean = entryIterator.hasNext()
                    override fun next(): V = entryIterator.next().value
                    override fun remove() = entryIterator.remove()
                }
            }

            override fun add(element: V): Boolean {
                throw UnsupportedOperationException("Cannot add value directly to map values")
            }

            override fun addAll(elements: Collection<V>): Boolean {
                throw UnsupportedOperationException("Cannot add values directly to map values")
            }

            override fun remove(element: V): Boolean {
                val iterator = entries.iterator()
                var removed = false
                while (iterator.hasNext()) {
                    if (iterator.next().value == element) {
                        iterator.remove()
                        removed = true
                    }
                }
                return removed
            }

            override fun removeAll(elements: Collection<V>): Boolean {
                return elements.any { remove(it) }
            }

            override fun retainAll(elements: Collection<V>): Boolean {
                val elementsToKeep = elements.toSet()
                return entries.removeAll { entry -> entry.value !in elementsToKeep }
            }

            override fun clear() {
                entries.clear()
            }
        }

    override fun put(key: K, value: V): V? {
        val entry = MutableMapEntry(key, value)
        val index = entries.elements.binarySearch(entry, entries.comparator)
        return if (index >= 0) {
            val oldValue = entries.elements[index].value
            entries.elements[index] = entry
            oldValue
        } else {
            entries.elements.add(-index - 1, entry)
            null
        }
    }

    override fun remove(key: K): V? {
        val index = entries.elements.binarySearch(KeyOnly(key), entries.comparator)
        return if (index >= 0) {
            val oldValue = entries.elements[index].value
            entries.elements.removeAt(index)
            oldValue
        } else {
            null
        }
    }

    override fun putAll(from: Map<out K, V>) {
        from.forEach { (key, value) -> put(key, value) }
    }

    override fun clear() {
        entries.clear()
    }
}

fun <K: Comparable<K>, V> mutableSortedMapOf(vararg pairs: Pair<K, V>): MutableSortedMap<K, V> {
    val map = MutableSortedMapImpl<K, V>(naturalOrder())
    map.putAll(pairs)
    return map
}

fun <K, V> mutableSortedMapOf(vararg pairs: Pair<K, V>, comparator: Comparator<K>) : MutableSortedMap<K, V> {
    val map = MutableSortedMapImpl<K, V>(comparator)
    map.putAll(pairs.reversed())
    return map
}


