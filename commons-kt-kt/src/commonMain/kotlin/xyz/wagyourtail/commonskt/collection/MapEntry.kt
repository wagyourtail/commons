package xyz.wagyourtail.commonskt.collection

data class MapEntry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>

data class MutableMapEntry<K, V>(override val key: K, override var value: V) : MutableMap.MutableEntry<K, V> {

    override fun setValue(newValue: V): V {
        val prev = value
        value = newValue
        return prev
    }

}
