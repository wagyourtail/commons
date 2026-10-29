package xyz.wagyourtail.commons.core.data;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Shared parse-result cache for structured data implementations.
 *
 * Atomic/leaf structured data may be thin wrappers around a single raw token
 * or otherwise unable to store per-instance state, so parsed sub-parts can't
 * be kept on the instance. This cache keys parsed results by the *type* and
 * the raw key, so every instance sharing a key also shares its parsed parts.
 *
 * Caching behavior is controlled globally via {@link #setMaxSize(int)} and can
 * be changed at runtime:
 * <ul>
 *   <li>{@code -1}: disabled — every access reparses (no cache memory)</li>
 *   <li>{@code 0}: unbounded — one entry per unique (tag, key) pair</li>
 *   <li>{@code n > 0}: bounded LRU — at most {@code n} entries across all tags</li>
 * </ul>
 */
public final class ParseCache {

    private ParseCache() {
    }

    private static volatile int maxSize = 0;

    private static final LinkedHashMap<Entry, Object> delegate = new LinkedHashMap<>();

    /**
     * @return current capacity setting: {@code < 0} disabled, {@code 0}
     * unbounded, {@code n > 0} bounded LRU.
     */
    public static int getMaxSize() {
        return maxSize;
    }

    /**
     * {@code < 0}: caches disabled; {@code 0}: unbounded; {@code n > 0}:
     * bounded LRU capacity.
     */
    public static void setMaxSize(int maxSize) {
        ParseCache.maxSize = maxSize;
    }

    /**
     * Returns the cached value for ({@code tag}, {@code key}), computing and
     * storing it on miss.
     *
     * @param tag namespaces the entry — pass the type's companion (or any
     *            stable sentinel); the same key may parse differently under
     *            different types.
     * @param key must implement value equality (e.g. {@code String}, or a
     *            content-equality wrapper — not a plain {@code byte[]}).
     */
    @SuppressWarnings("unchecked")
    public static <V> V getOrPut(Object tag, Object key, Compute<V> compute) {
        int max = maxSize;
        Entry cacheKey = new Entry(tag, key);
        if (max < 0) return compute.compute();
        Object value = delegate.get(cacheKey);
        if (value == null) {
            value = compute.compute();
            delegate.put(cacheKey, value);
            if (max > 0 && delegate.size() > max) {
                delegate.remove(delegate.keySet().iterator().next());
            }
        } else if (max > 0) {
            // LRU touch: reinsert at the end
            delegate.remove(cacheKey);
            delegate.put(cacheKey, value);
        }
        return (V) value;
    }

    /**
     * Removes all entries.
     */
    public static void clear() {
        delegate.clear();
    }

    /**
     * Current entry count, for debugging/profiling.
     */
    public static int size() {
        return delegate.size();
    }

    /**
     * computation for {@link #getOrPut(Object, Object, Compute)} — a
     * single-method interface so Java 8+ callers may use lambdas.
     */
    public interface Compute<V> {
        V compute();
    }

    private static final class Entry {
        private final Object tag;
        private final Object key;

        private Entry(Object tag, Object key) {
            this.tag = tag;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;
            Entry entry = (Entry) o;
            return Objects.equals(tag, entry.tag) && Objects.equals(key, entry.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag, key);
        }
    }

}
