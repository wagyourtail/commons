package xyz.wagyourtail.commonskt.collection.sorted

interface SortedMap<K, V> : Map<K, V> {

    val comparator: Comparator<K>

    override val keys: SortedSet<K>

    override val entries: Set<Map.Entry<K, V>>

    fun firstKey(): K

    fun lastKey(): K

    fun headMap(toKey: K): SortedMap<K, V>

    fun tailMap(fromKey: K): SortedMap<K, V>

    fun subMap(fromKey: K, toKey: K): SortedMap<K, V> = tailMap(fromKey).headMap(toKey)

}

interface MutableSortedMap<K, V> : SortedMap<K, V>, MutableMap<K, V> {

    override val keys: MutableSortedSet<K>

    override val entries: MutableSortedSet<MutableMap.MutableEntry<K, V>>

    override fun headMap(toKey: K): MutableSortedMap<K, V>

    override fun tailMap(fromKey: K): MutableSortedMap<K, V>

    override fun subMap(fromKey: K, toKey: K): MutableSortedMap<K, V> = tailMap(fromKey).headMap(toKey)

}
