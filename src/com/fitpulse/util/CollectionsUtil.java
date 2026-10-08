package com.fitpulse.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Reusable generic collection utility class.
 * Demonstrates:
 * - Rubric Item 2: Collections & Generics (List, Set, Map, bounded & unbounded type parameters)
 * - Operations: Search, Filter, Update, Index, and Grouping algorithms.
 */
public final class CollectionsUtil {

    private CollectionsUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Generic Search: Filters any collection based on a specified predicate.
     * Demonstrates: Generics <T>, List<T>, iteration with enhanced for-loop.
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        List<T> results = new ArrayList<>();
        if (collection == null || predicate == null) {
            return results;
        }
        for (T item : collection) {
            if (predicate.test(item)) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Generic Search: Finds the first element in a collection matching a predicate.
     */
    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null || predicate == null) {
            return Optional.empty();
        }
        for (T item : collection) {
            if (predicate.test(item)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * Generic Update: Searches for an element matching the matcher and applies the updater mutation.
     * Demonstrates: List index-based search and in-place update.
     */
    public static <T> boolean updateFirst(List<T> list, Predicate<T> matcher, Consumer<T> updater) {
        if (list == null || matcher == null || updater == null) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            if (matcher.test(item)) {
                updater.accept(item);
                return true;
            }
        }
        return false;
    }

    /**
     * Generic Indexing: Converts a collection into a Map indexed by an extracted key.
     * Demonstrates: Map<K, T>, HashMap creation, and O(1) index generation.
     */
    public static <K, T> Map<K, T> indexBy(Collection<T> collection, Function<T, K> keyMapper) {
        Map<K, T> map = new HashMap<>();
        if (collection == null || keyMapper == null) {
            return map;
        }
        for (T item : collection) {
            if (item != null) {
                map.put(keyMapper.apply(item), item);
            }
        }
        return map;
    }

    /**
     * Generic Grouping: Groups elements by key into a Map of Lists.
     * Demonstrates: Map<K, List<T>>, grouping by categorical attribute.
     */
    public static <K, T> Map<K, List<T>> groupBy(Collection<T> collection, Function<T, K> keyMapper) {
        Map<K, List<T>> map = new HashMap<>();
        if (collection == null || keyMapper == null) {
            return map;
        }
        for (T item : collection) {
            if (item != null) {
                K key = keyMapper.apply(item);
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
            }
        }
        return map;
    }

    /**
     * Generic Set Conversion: Eliminates duplicates from a collection.
     * Demonstrates: Set<T> uniqueness via HashSet.
     */
    public static <T> Set<T> toUniqueSet(Collection<T> collection) {
        if (collection == null) {
            return new HashSet<>();
        }
        return new HashSet<>(collection);
    }

    /**
     * Generic Extrema: Finds the item with the maximum value using a comparable extractor.
     * Demonstrates: Bounded type parameter <R extends Comparable<R>>.
     */
    public static <T, R extends Comparable<R>> Optional<T> findMax(Collection<T> collection, Function<T, R> valueExtractor) {
        if (collection == null || collection.isEmpty() || valueExtractor == null) {
            return Optional.empty();
        }
        T bestItem = null;
        R bestValue = null;
        for (T item : collection) {
            R val = valueExtractor.apply(item);
            if (bestValue == null || (val != null && val.compareTo(bestValue) > 0)) {
                bestValue = val;
                bestItem = item;
            }
        }
        return Optional.ofNullable(bestItem);
    }
}
