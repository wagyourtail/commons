package xyz.wagyourtail.commonskt.collection

class ListMap<K, V> : AbstractMutableMap<K, V>() {
    private var ks = mutableListOf<K>()
    private var vs = mutableListOf<V>()

    override val size: Int
        get() = ks.size

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = object : AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {
            override val size: Int
                    = ks.size

            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
                return object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                    var i = 0
                    override fun hasNext(): Boolean = i < ks.size
                    override fun next(): MutableMap.MutableEntry<K, V> =
                        object : MutableMap.MutableEntry<K, V> {
                            var j = i++

                            override val key: K
                                get() = ks[j]
                            override val value: V
                                get() = vs[j]

                            override fun setValue(newValue: V): V {
                                val old = vs[j]
                                vs[j] = newValue
                                return old!!
                            }
                        }

                    override fun remove() {
                        if (i > 0 && i <= ks.size) {
                            val index = i - 1
                            ks.removeAt(index)
                            vs.removeAt(index)
                            i = index
                        }
                    }
                }
            }

            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
                val i = ks.indexOf(element.key)
                if (i > -1) {
                    vs[i] = element.value
                    return false
                }
                ks.add(element.key)
                vs.add(element.value)
                return true
            }

        }


    private fun keyIndex(key: K?): Int {
        for (i in ks.indices) {
            if (ks[i] == key) return i
        }
        return -1
    }

    override fun containsKey(key: K): Boolean {
        return keyIndex(key) > -1
    }

    override fun put(key: K, value: V): V? {
        val i = keyIndex(key)
        if (i > -1) {
            val old = vs[i]
            vs[i] = value
            return old as V?
        } else {
            ks.add(key)
            vs.add(value)
            return null
        }
    }

    override fun get(key: K): V? {
        val i = keyIndex(key)
        return if (i > -1) vs[i] as V? else null
    }

    override fun remove(key: K): V? {
        val i = keyIndex(key)
        if (i > -1) {
            val old = vs[i]
            ks.removeAt(i)
            vs.removeAt(i)
            return old
        } else {
            return null
        }
    }
}

fun <K, V> listMapOf(vararg pairs: Pair<K, V>) = ListMap<K, V>().apply { putAll(pairs) }
