package io.algokit.structures;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Least-recently-used cache built on {@code LinkedHashMap} with access-order eviction.
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LruCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
