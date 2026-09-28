package io.algokit.arrays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Array and string utility functions: counting, grouping, prefix sums, and complement lookups.
 */
public final class Arrays2 {
    private Arrays2() {}

    /** Counts lowercase a-z characters. O(n) time, O(1) space. */
    public static int[] letterCounts(String s) {
        int[] counts = new int[26];
        for (int i = 0; i < s.length(); i++) {
            counts[s.charAt(i) - 'a']++;
        }
        return counts;
    }

    /** Counts 7-bit ASCII characters (0..127). */
    public static int[] asciiCounts(String s) {
        int[] counts = new int[128];
        for (int i = 0; i < s.length(); i++) {
            counts[s.charAt(i)]++;
        }
        return counts;
    }

    /** Frequency map for arbitrary keys. Uses {@code Map.merge} for one-line counting. */
    public static <T> Map<T, Integer> frequencies(Iterable<T> items) {
        Map<T, Integer> freq = new HashMap<>();
        for (T item : items) {
            freq.merge(item, 1, Integer::sum);
        }
        return freq;
    }

    /** Groups items by a derived key; each group keeps input order. */
    public static <K, V> Map<K, List<V>> groupBy(List<V> items, Function<V, K> keyOf) {
        Map<K, List<V>> groups = new HashMap<>();
        for (V item : items) {
            groups.computeIfAbsent(keyOf.apply(item), k -> new ArrayList<>()).add(item);
        }
        return groups;
    }

    /**
     * Builds a prefix sum array. {@code prefix[i] = a[0] + ... + a[i-1]}.
     * Sum of a[l..r] (inclusive) = {@code prefix[r + 1] - prefix[l]}.
     * Uses long to avoid overflow.
     */
    public static long[] prefixSums(int[] a) {
        long[] prefix = new long[a.length + 1];
        for (int i = 0; i < a.length; i++) {
            prefix[i + 1] = prefix[i] + a[i];
        }
        return prefix;
    }

    /** O(1) range sum query on a prefix sum array. Returns sum of original a[l..r] inclusive. */
    public static long rangeSum(long[] prefix, int l, int r) {
        return prefix[r + 1] - prefix[l];
    }

    /** Checks whether any pair (i, j) with i != j has |a[i] - a[j]| == diff. One-pass, O(n). */
    public static boolean hasPairWithDifference(int[] a, int diff) {
        Set<Long> seen = new HashSet<>();
        for (int x : a) {
            if (seen.contains((long) x - diff) || seen.contains((long) x + diff)) {
                return true;
            }
            seen.add((long) x);
        }
        return false;
    }

    /** Number of contiguous subarrays summing to k. Works with negatives. O(n). */
    public static int countSubarraysWithSum(int[] a, int k) {
        Map<Long, Integer> seen = new HashMap<>();
        seen.put(0L, 1);
        long prefix = 0;
        int count = 0;
        for (int x : a) {
            prefix += x;
            count += seen.getOrDefault(prefix - k, 0);
            seen.merge(prefix, 1, Integer::sum);
        }
        return count;
    }

    /**
     * Packs two ints into a single long key for use as a HashMap key.
     * Avoids the overhead of creating a record/list for composite keys.
     */
    public static long packKey(int a, int b) {
        return ((long) a << 32) | (b & 0xFFFFFFFFL);
    }
}
