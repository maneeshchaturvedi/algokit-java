package io.algokit.dp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DynamicProgrammingTest {

    @Test void coinChange() {
        assertEquals(2, DynamicProgramming.coinChange(new int[]{1, 3, 4}, 6));  // 3+3, not greedy 4+1+1
        assertEquals(-1, DynamicProgramming.coinChange(new int[]{2}, 3));
        assertEquals(0, DynamicProgramming.coinChange(new int[]{5}, 0));
    }

    @Test void countGridPaths() {
        boolean[][] open = new boolean[3][3];
        assertEquals(6, DynamicProgramming.countGridPaths(open));  // C(4,2)

        boolean[][] blocked = {{false, false}, {true, false}};
        assertEquals(1, DynamicProgramming.countGridPaths(blocked));  // only right-then-down
    }
}
