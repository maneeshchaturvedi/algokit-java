package io.algokit.window;

import java.util.HashMap;
import java.util.Map;

/**
 * Sliding window templates: fixed-size, variable-size (longest valid / shortest valid),
 * and at-most-k-distinct.
 */
public final class SlidingWindow {
    private SlidingWindow() {}

    /** Maximum sum of k consecutive elements. O(n). Requires 1 &lt;= k &lt;= n. */
    public static long maxSumWindow(int[] nums, int k) {
        if (k <= 0 || k > nums.length) {
            throw new IllegalArgumentException("need 1 <= k <= n");
        }
        long window = 0;
        for (int i = 0; i < k; i++) window += nums[i];
        long best = window;
        for (int right = k; right < nums.length; right++) {
            window += nums[right] - (long) nums[right - k];
            best = Math.max(best, window);
        }
        return best;
    }

    /**
     * Longest subarray with sum at most {@code limit}. Requires non-negative values.
     * Expand right, shrink left while invalid. O(n).
     */
    public static int longestSumAtMost(int[] nums, long limit) {
        long windowSum = 0;
        int best = 0, left = 0;
        for (int right = 0; right < nums.length; right++) {
            windowSum += nums[right];
            while (windowSum > limit) {
                windowSum -= nums[left++];
            }
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    /**
     * Shortest subarray with sum at least {@code target}, or 0 if none.
     * Requires non-negative values. O(n).
     */
    public static int minSubArrayLen(int[] nums, long target) {
        long windowSum = 0;
        int best = Integer.MAX_VALUE, left = 0;
        for (int right = 0; right < nums.length; right++) {
            windowSum += nums[right];
            while (left <= right && windowSum >= target) {
                best = Math.min(best, right - left + 1);
                windowSum -= nums[left++];
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    /** Longest window with at most k distinct values. O(n). */
    public static int longestAtMostKDistinct(int[] a, int k) {
        Map<Integer, Integer> inWindow = new HashMap<>();
        int best = 0;
        for (int left = 0, right = 0; right < a.length; right++) {
            inWindow.merge(a[right], 1, Integer::sum);
            while (inWindow.size() > k) {
                inWindow.merge(a[left], -1, (o, d) -> o + d == 0 ? null : o + d);
                left++;
            }
            best = Math.max(best, right - left + 1);
        }
        return best;
    }
}
