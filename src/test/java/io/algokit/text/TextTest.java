package io.algokit.text;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class TextTest {

    @Test void tokenize() {
        List<String> tokens = Text.tokenize("The Quick Brown Fox!");
        assertEquals(List.of("quick", "brown", "fox"), tokens);  // "the" is a stop word
    }

    @Test void weightedTermFrequency() {
        Map<String, Integer> tf = Text.weightedTermFrequency("billing help", "billing issue please help");
        assertEquals(3, tf.get("billing"));  // 2 (title) + 1 (body)
        assertEquals(3, tf.get("help"));     // 2 (title) + 1 (body)
    }

    @Test void idf() {
        assertEquals(Math.log(11.0), Text.idf(100, 10), 0.0001);
    }
}
