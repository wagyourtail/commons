package xyz.wagyourtail.commonskt.collection.small

class SmallArrayMap<K, V>(val initialSize: Int = 0, val resizeAmount: Int = 1) : AbstractMutableMap<K, V>() {
    private var ks = arrayOfNulls<Any?>(initialSize)
    private var vs = arrayOfNulls<Any?>(initialSize)

    init {
        if (resizeAmount < 1) {
            throw IllegalArgumentException("resize must be positive")
        }
    }

    override var size: Int = 0
        private set

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = object : AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {

            override val size: Int
                get() = this@SmallArrayMap.size

            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
                return object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                    var i = 0

                    override fun hasNext(): Boolean = i < size

                    override fun next(): MutableMap.MutableEntry<K, V> {
                        if (!hasNext()) throw NoSuchElementException()
                        return object : MutableMap.MutableEntry<K, V> {
                            var j = i++

                            override val key: K
                                get() = ks[j] as K
                            override val value: V
                                get() = vs[j] as V

                            override fun setValue(newValue: V): V {
                                val old = vs[j] as V
                                vs[j] = newValue
                                return old
                            }

                        }
                    }

                    override fun remove() {
                        if (i > 0 && i <= ks.size) {
                            val index = i - 1
                            removeAt(index)
                            i = index
                        } else {
                            throw IllegalStateException()
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
                if (size == ks.size) {
                    val newKs = arrayOfNulls<Any>(size + resizeAmount)
                    val newVs = arrayOfNulls<Any>(size + resizeAmount)
                    ks.copyInto(newKs)
                    vs.copyInto(newVs)
                    ks = newKs
                    vs = newVs
                }
                ks[size] = element.key
                vs[size] = element.value
                ++this@SmallArrayMap.size
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
            if (size == ks.size) {
                val newKs = arrayOfNulls<Any>(size + resizeAmount)
                val newVs = arrayOfNulls<Any>(size + resizeAmount)
                ks.copyInto(newKs)
                vs.copyInto(newVs)
                ks = newKs
                vs = newVs
            }
            ks[size] = key
            vs[size] = value
            ++size
        }
        return null
    }

    override fun get(key: K): V? {
        val i = keyIndex(key)
        return if (i > -1) vs[i] as V? else null
    }

    override fun remove(key: K): V? {
        val i = keyIndex(key)
        return if (i > -1) {
            val value = vs[i] as V?
            removeAt(i)
            value
        } else {
            null
        }
    }

    private fun removeAt(index: Int) {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("Index: $index, Size: $size")
        if (index == --size) {
            if (size < ks.size - resizeAmount) {
                val newKs = arrayOfNulls<Any>(ks.size - resizeAmount)
                val newVs = arrayOfNulls<Any>(vs.size - resizeAmount)
                ks.copyInto(newKs, 0, 0, index)
                vs.copyInto(newVs, 0, 0, index)
                ks = newKs
                vs = newVs
            }
        } else {
            if (size < ks.size - resizeAmount) {
                val newKs = arrayOfNulls<Any>(ks.size - resizeAmount)
                val newVs = arrayOfNulls<Any>(vs.size - resizeAmount)
                ks.copyInto(newKs, 0, 0, index)
                vs.copyInto(newVs, 0, 0, index)
                ks.copyInto(newKs, index, index + 1, size)
                vs.copyInto(newVs, index, index + 1, size)
                ks = newKs
                vs = newVs
            } else {
                ks.copyInto(ks, index, index + 1)
                vs.copyInto(vs, index, index + 1)
            }
        }
    }

    override fun clear() {
        size = 0
        ks = arrayOfNulls(initialSize)
        vs = arrayOfNulls(initialSize)
    }

}

fun <K, V> smallArrayMapOf(vararg pairs: Pair<K, V>, initialSize: Int = pairs.size, resizeAmount: Int = 1) =
    SmallArrayMap<K, V>(initialSize, resizeAmount).apply { putAll(pairs) }
