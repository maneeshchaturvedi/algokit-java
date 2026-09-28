package io.algokit.structures;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * "Value as of time t" store backed by a TreeMap per key.
 * Uses {@code floorEntry} to find the latest write at or before the query time.
 *
 * <p>Useful for: time-based key-value stores, config snapshots, plan versioning.
 */
public final class Versioned<V> {
    private final Map<String, TreeMap<Long, V>> history = new HashMap<>();

    public void put(String key, long time, V value) {
        history.computeIfAbsent(key, k -> new TreeMap<>()).put(time, value);
    }

    /** Returns the value for the latest write at or before {@code time}, or null. */
    public V get(String key, long time) {
        TreeMap<Long, V> versions = history.get(key);
        if (versions == null) return null;
        Map.Entry<Long, V> e = versions.floorEntry(time);
        return e == null ? null : e.getValue();
    }
}
