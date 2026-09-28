package io.algokit.search;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTest {

    @Test void lowerAndUpperBound() {
        int[] a = {1, 2, 2, 2, 5, 7};
        assertEquals(1, BinarySearch.lowerBound(a, 2));
        assertEquals(4, BinarySearch.upperBound(a, 2));
        assertEquals(0, BinarySearch.lowerBound(a, 0));
        assertEquals(6, BinarySearch.lowerBound(a, 8));
    }

    @Test void indexOf() {
        int[] a = {1, 3, 5, 7};
        assertEquals(2, BinarySearch.indexOf(a, 5));
        assertEquals(-1, BinarySearch.indexOf(a, 4));
        assertEquals(-1, BinarySearch.indexOf(new int[0], 1));
    }

    @Test void firstTrueEmptyRange() {
        assertEquals(5, BinarySearch.firstTrue(5, 5, x -> true));
    }

    @Test void firstTrueLong() {
        long result = BinarySearch.firstTrueLong(0L, 3_000_000_000L,
                x -> x * x >= 4_000_000_000_000_000_000L);
        assertEquals(2_000_000_000L, result);
    }
}
