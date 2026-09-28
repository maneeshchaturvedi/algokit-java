package io.algokit.text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Text processing utilities: tokenization, term frequency, and TF-IDF scoring.
 */
public final class Text {
    private Text() {}

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to",
            "for", "of", "with", "by", "is", "it", "this", "that", "are", "was"
    );

    /** Lowercase, split on non-alphanumeric, drop blanks and common stop words. */
    public static List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(w -> !w.isEmpty() && !STOP_WORDS.contains(w))
                .toList();
    }

    /**
     * Weighted term frequency: title terms get weight 2, body terms get weight 1.
     */
    public static Map<String, Integer> weightedTermFrequency(String title, String body) {
        Map<String, Integer> tf = new HashMap<>();
        for (String w : tokenize(title)) tf.merge(w, 2, Integer::sum);
        for (String w : tokenize(body)) tf.merge(w, 1, Integer::sum);
        return tf;
    }

    /** Inverse document frequency: {@code log(1 + N / df)}. */
    public static double idf(int totalDocuments, int docFrequency) {
        return Math.log(1.0 + (double) totalDocuments / docFrequency);
    }
}
