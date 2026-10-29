package xyz.wagyourtail.commonskt.data

/**
 * Shared parse-result cache for structured data implementations.
 *
 * Atomic/leaf structured data may be `value class`es around a single raw
 * token or otherwise unable to store per-instance state, so parsed sub-parts
 * can't be kept on the instance. This cache keys parsed results by the *type*
 * and the raw key, so every instance sharing a key also shares its parsed
 * parts.
 *
 * Caching behavior is controlled globally via [maxSize] and can be changed at
 * runtime:
 * - `-1`: disabled — every access reparses (no cache memory)
 * - `0`: unbounded — one entry per unique (tag, key) pair
 * - `n > 0`: bounded LRU — at most `n` entries across all tags
 */
object ParseCache {

    /**
     * < 0: caches disabled; 0: unbounded; n > 0: bounded LRU capacity.
     */
    var maxSize: Int = 0

    private val delegate = LinkedHashMap<Pair<Any, Any>, Any>()

    /**
     * Returns the cached value for ([tag], [key]), computing and storing it on
     * miss.
     *
     * [tag] namespaces the entry — pass the type's companion (or any stable
     * sentinel); the same key may parse differently under different types.
     * [key] must implement value equality (e.g. `String`, `okio.ByteString`,
     * or a content-equality wrapper — not a plain `ByteArray`).
     */
    @Suppress("UNCHECKED_CAST")
    fun <V : Any> getOrPut(tag: Any, key: Any, compute: () -> V): V {
        val max = maxSize
        val cacheKey = tag to key
        if (max < 0) return compute()
        var value = delegate[cacheKey] as V?
        if (value == null) {
            value = compute()
            delegate[cacheKey] = value
            if (max > 0 && delegate.size > max) {
                delegate.remove(delegate.keys.first())
            }
        } else if (max > 0) {
            // LRU touch: reinsert at the end
            delegate.remove(cacheKey)
            delegate[cacheKey] = value
        }
        return value
    }

    /**
     * Removes all entries.
     */
    fun clear() = delegate.clear()

    /**
     * Current entry count, for debugging/profiling.
     */
    val size: Int get() = delegate.size

}
