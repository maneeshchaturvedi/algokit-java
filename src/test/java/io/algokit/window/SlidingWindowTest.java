package io.algokit.window;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowTest {

    @Test void maxSumWindow() {
        assertEquals(39, SlidingWindow.maxSumWindow(new int[]{1, 4, 2, 10, 23, 3, 1, 0, 20}, 4));
    }

    @Test void longestSumAtMost() {
        assertEquals(3, SlidingWindow.longestSumAtMost(new int[]{3, 1, 2, 7, 1, 1}, 6));
        assertEquals(0, SlidingWindow.longestSumAtMost(new int[]{9, 9}, 5));
    }

    @Test void minSubArrayLen() {
        assertEquals(2, SlidingWindow.minSubArrayLen(new int[]{2, 3, 1, 2, 4, 3}, 7));
        assertEquals(0, SlidingWindow.minSubArrayLen(new int[]{1, 1, 1}, 11));
    }

    @Test void longestAtMostKDistinct() {
        assertEquals(4, SlidingWindow.longestAtMostKDistinct(new int[]{1, 2, 1, 2, 3}, 2));
        assertEquals(0, SlidingWindow.longestAtMostKDistinct(new int[]{1, 2, 3}, 0));
    }
}
