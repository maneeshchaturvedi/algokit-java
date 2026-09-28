package io.algokit.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StructuresTest {

    @Test void versioned() {
        Versioned<String> plan = new Versioned<>();
        plan.put("cust1", 10, "starter");
        plan.put("cust1", 20, "pro");
        assertNull(plan.get("cust1", 5));
        assertEquals("starter", plan.get("cust1", 19));
        assertEquals("pro", plan.get("cust1", 20));
        assertNull(plan.get("unknown", 1));
    }

    @Test void lruCache() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");        // access a -> a is now most recent
        cache.put("c", 3);     // evicts b (least recently used)
        assertNull(cache.get("b"));
        assertEquals(1, cache.get("a"));
        assertEquals(3, cache.get("c"));
    }
}
