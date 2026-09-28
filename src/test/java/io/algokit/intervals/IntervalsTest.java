package io.algokit.intervals;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class IntervalsTest {

    @Test void merge() {
        List<int[]> merged = Intervals.merge(new int[][]{{8,10},{1,3},{2,6},{15,18},{6,7}});
        assertEquals(3, merged.size());
        assertArrayEquals(new int[]{1, 7}, merged.get(0));
        assertArrayEquals(new int[]{8, 10}, merged.get(1));
        assertArrayEquals(new int[]{15, 18}, merged.get(2));
    }

    @Test void mergeEmpty() {
        assertTrue(Intervals.merge(new int[0][]).isEmpty());
    }

    @Test void mergeSingle() {
        List<int[]> merged = Intervals.merge(new int[][]{{1, 5}});
        assertEquals(1, merged.size());
    }
}
