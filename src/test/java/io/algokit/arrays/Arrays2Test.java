package io.algokit.arrays;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Arrays2Test {

    @Test void letterCounts() {
        int[] c = Arrays2.letterCounts("banana");
        assertEquals(3, c['a' - 'a']);
        assertEquals(1, c['b' - 'a']);
        assertEquals(2, c['n' - 'a']);
    }

    @Test void frequencies() {
        Map<String, Integer> f = Arrays2.frequencies(List.of("apple", "kiwi", "apple"));
        assertEquals(2, f.get("apple"));
        assertEquals(1, f.get("kiwi"));
    }

    @Test void groupBy() {
        Map<Integer, List<String>> g = Arrays2.groupBy(List.of("a", "bb", "cc", "d"), String::length);
        assertEquals(List.of("a", "d"), g.get(1));
        assertEquals(List.of("bb", "cc"), g.get(2));
    }

    @Test void prefixSumsAndRangeSum() {
        int[] v = {3, -1, 4, 1, 5};
        long[] p = Arrays2.prefixSums(v);
        assertEquals(4, Arrays2.rangeSum(p, 1, 3));  // -1 + 4 + 1
        assertEquals(3, Arrays2.rangeSum(p, 0, 0));
    }

    @Test void hasPairWithDifference() {
        assertTrue(Arrays2.hasPairWithDifference(new int[]{1, 5, 9}, 4));
        assertFalse(Arrays2.hasPairWithDifference(new int[]{1, 5, 9}, 3));
    }

    @Test void countSubarraysWithSum() {
        assertEquals(2, Arrays2.countSubarraysWithSum(new int[]{1, 1, 1}, 2));
        assertEquals(6, Arrays2.countSubarraysWithSum(new int[]{1, -1, 1, -1, 1}, 0));
    }

    @Test void packKey() {
        assertNotEquals(Arrays2.packKey(1, -1), Arrays2.packKey(0, -1));
        assertEquals((3L << 32) | 7L, Arrays2.packKey(3, 7));
    }
}
