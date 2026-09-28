package io.algokit.backtrack;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BacktrackingTest {

    @Test void subsets() {
        List<List<Integer>> subs = Backtracking.subsets(new int[]{1, 2, 3});
        assertEquals(8, subs.size());
        assertTrue(subs.contains(List.of()));
        assertTrue(subs.contains(List.of(1, 3)));
    }

    @Test void combine() {
        List<List<Integer>> combos = Backtracking.combine(4, 2);
        assertEquals(6, combos.size());
        assertTrue(combos.contains(List.of(1, 4)));
    }

    @Test void combineEdgeCases() {
        assertEquals(1, Backtracking.combine(3, 0).size());  // [[]]
        assertTrue(Backtracking.combine(2, 3).isEmpty());
    }
}
