package io.algokit.stack;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MonotonicStackTest {

    @Test void nextGreaterIndex() {
        assertArrayEquals(new int[]{3, 2, 3, -1, -1},
                MonotonicStack.nextGreaterIndex(new int[]{2, 1, 2, 4, 3}));
    }

    @Test void windowMin() {
        assertArrayEquals(new int[]{2, 2, 3, 3},
                MonotonicStack.windowMin(new int[]{4, 2, 12, 3, 8}, 2));
    }
}
