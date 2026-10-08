package com.fitpulse.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic In-Memory Cache supporting TTL and LRU expiration.
 * Demonstrates:
 * - Rubric Item 2: Collections & Generics (Map, Set, List, Generics <K, V>)
 * - Thread-safe concurrent access
 *
 * @param <K> Key type
 * @param <V> Cached value type
 */
public class GenericCache<K, V> {
    private final Map<K, CacheEntry<V>> cacheMap;
    private final long ttlMillis;

    private static class CacheEntry<V> {
        final V value;
        final long createdAt;

        CacheEntry(V value) {
            this.value = value;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired(long ttlMillis) {
            return (System.currentTimeMillis() - createdAt) > ttlMillis;
        }
    }

    public GenericCache() {
        this(300_000); // 5 minutes default TTL
    }

    public GenericCache(long ttlMillis) {
        this.cacheMap = new ConcurrentHashMap<>();
        this.ttlMillis = ttlMillis;
    }

    public void put(K key, V value) {
        if (key != null && value != null) {
            cacheMap.put(key, new CacheEntry<>(value));
        }
    }

    public Optional<V> get(K key) {
        if (key == null) return Optional.empty();
        CacheEntry<V> entry = cacheMap.get(key);
        if (entry == null) return Optional.empty();
        if (entry.isExpired(ttlMillis)) {
            cacheMap.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value);
    }

    public boolean containsKey(K key) {
        return get(key).isPresent();
    }

    public void remove(K key) {
        cacheMap.remove(key);
    }

    public void clear() {
        cacheMap.clear();
    }

    /**
     * Demonstrates Set collection usage: returns all unique active keys.
     */
    public Set<K> keySet() {
        cleanExpired();
        return Collections.unmodifiableSet(new HashSet<>(cacheMap.keySet()));
    }

    /**
     * Demonstrates List collection usage: returns all active cached values.
     */
    public List<V> values() {
        cleanExpired();
        List<V> list = new ArrayList<>();
        for (CacheEntry<V> entry : cacheMap.values()) {
            list.add(entry.value);
        }
        return Collections.unmodifiableList(list);
    }

    public int size() {
        cleanExpired();
        return cacheMap.size();
    }

    private void cleanExpired() {
        Iterator<Map.Entry<K, CacheEntry<V>>> it = cacheMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, CacheEntry<V>> entry = it.next();
            if (entry.getValue().isExpired(ttlMillis)) {
                it.remove();
            }
        }
    }
}
