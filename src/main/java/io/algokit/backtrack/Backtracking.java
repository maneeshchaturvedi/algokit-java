package io.algokit.backtrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Backtracking templates: subsets and combinations.
 *
 * <p>The pattern is always: choose, explore, un-choose. Record a COPY of the path at each goal.
 */
public final class Backtracking {
    private Backtracking() {}

    /** All subsets of distinct values. */
    public static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        subsetsHelper(nums, 0, new ArrayList<>(), out);
        return out;
    }

    private static void subsetsHelper(int[] nums, int start, List<Integer> path, List<List<Integer>> out) {
        out.add(new ArrayList<>(path));
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);
            subsetsHelper(nums, i + 1, path, out);
            path.remove(path.size() - 1);
        }
    }

    /** All k-element combinations of 1..n (LC 77). */
    public static List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> out = new ArrayList<>();
        if (k < 0 || k > n) return out;
        combineHelper(1, n, k, new ArrayList<>(), out);
        return out;
    }

    private static void combineHelper(int start, int n, int k, List<Integer> path, List<List<Integer>> out) {
        if (path.size() == k) {
            out.add(new ArrayList<>(path));
            return;
        }
        int stillNeeded = k - path.size();
        for (int x = start; x <= n - stillNeeded + 1; x++) {
            path.add(x);
            combineHelper(x + 1, n, k, path, out);
            path.remove(path.size() - 1);
        }
    }
}
