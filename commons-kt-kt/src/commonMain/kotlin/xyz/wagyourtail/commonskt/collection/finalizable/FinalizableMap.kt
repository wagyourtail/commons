package xyz.wagyourtail.commonskt.collection.finalizable

class FinalizableMap<K, V>(val backing: MutableMap<K, V> = mutableMapOf()) :
    MutableMap<K, V>,
    Map<K, V> by backing,
    Finalizable
{
    private var finalized = false

    override val keys: MutableSet<K>
        get() = FinalizableSet<K>(backing.keys).also {
            if (finalized) it.finalize()
        }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = object : FinalizableSet<MutableMap.MutableEntry<K, V>>(backing.entries) {
            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
                return object : FinalizableIterator<MutableMap.MutableEntry<K, V>, MutableIterator<MutableMap.MutableEntry<K, V>>>(
                    backing.iterator()
                ) {

                    override fun next(): MutableMap.MutableEntry<K, V> {
                        return FinalizableEntry(backing.next())
                    }

                }.also {
                    if (finalized) it.finalize()
                }
            }

            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
                if (finalized) throw UnsupportedOperationException("Cannot modify finalized set")
                return backing.add(if (element is FinalizableEntry) element.backing else element)
            }

            override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                if (finalized) throw UnsupportedOperationException("Cannot modify finalized set")
                return backing.addAll(elements.map { if (it is FinalizableEntry) it.backing else it }.toSet())
            }

            override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
                if (finalized) throw UnsupportedOperationException("Cannot modify finalized set")
                return backing.remove(if (element is FinalizableEntry) element.backing else element)
            }

            override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                if (finalized) throw UnsupportedOperationException("Cannot modify finalized set")
                return backing.removeAll(elements.map { if (it is FinalizableEntry) it.backing else it }.toSet())
            }

            override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                if (finalized) throw UnsupportedOperationException("Cannot modify finalized set")
                return backing.retainAll(elements.map { if (it is FinalizableEntry) it.backing else it }.toSet())
            }

        }.also {
            if (finalized) it.finalize()
        }

    override val values: MutableCollection<V>
        get() = FinalizableCollection(backing.values).also {
            if (finalized) it.finalize()
        }

    override fun finalize() {
        finalized = true
        values.forEach { if (it is Finalizable) it.finalize() }
    }

    override fun clear() {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized map")
        backing.clear()
    }

    override fun remove(key: K): V? {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized map")
        return backing.remove(key)
    }

    override fun putAll(from: Map<out K, V>) {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized map")
        backing.putAll(from)
    }

    override fun put(key: K, value: V): V? {
        if (finalized) throw UnsupportedOperationException("Cannot modify finalized map")
        return backing.put(key, value)
    }

    inner class FinalizableEntry(val backing: MutableMap.MutableEntry<K, V>) : MutableMap.MutableEntry<K, V>,
        Map.Entry<K, V> by backing {
        override fun setValue(newValue: V): V {
            if (finalized) throw UnsupportedOperationException("Cannot modify finalized entry")
            return backing.setValue(newValue)
        }

    }

}

fun <K, V> finalizableMapOf(vararg pairs: Pair<K, V>): FinalizableMap<K, V> {
    return FinalizableMap(mutableMapOf(*pairs))
}
