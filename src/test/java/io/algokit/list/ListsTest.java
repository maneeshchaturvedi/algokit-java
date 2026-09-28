package io.algokit.list;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ListsTest {

    @Test void ofAndToArray() {
        assertArrayEquals(new int[]{1, 2, 3}, Lists.toArray(Lists.of(1, 2, 3)));
        assertNull(Lists.of());
    }

    @Test void removeAll() {
        assertArrayEquals(new int[]{1, 2}, Lists.toArray(Lists.removeAll(Lists.of(6, 1, 6, 6, 2, 6), 6)));
        assertNull(Lists.removeAll(Lists.of(7, 7), 7));
    }

    @Test void middleNode() {
        assertEquals(3, Lists.middleNode(Lists.of(1, 2, 3, 4, 5)).val);
        assertEquals(4, Lists.middleNode(Lists.of(1, 2, 3, 4, 5, 6)).val);  // second middle
    }
}
