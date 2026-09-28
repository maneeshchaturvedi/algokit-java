package io.algokit.intervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Interval merging and manipulation.
 */
public final class Intervals {
    private Intervals() {}

    /** Merges overlapping intervals. Returns a new list; input is not modified. */
    public static List<int[]> merge(int[][] intervals) {
        int[][] sorted = intervals.clone();
        Arrays.sort(sorted, Comparator.comparingInt(iv -> iv[0]));
        List<int[]> out = new ArrayList<>();
        for (int[] iv : sorted) {
            if (!out.isEmpty() && iv[0] <= out.get(out.size() - 1)[1]) {
                out.get(out.size() - 1)[1] = Math.max(out.get(out.size() - 1)[1], iv[1]);
            } else {
                out.add(new int[]{iv[0], iv[1]});
            }
        }
        return out;
    }
}
