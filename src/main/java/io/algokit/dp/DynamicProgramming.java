package io.algokit.dp;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Dynamic programming templates: coin change, grid paths, and a deep-recursion helper.
 */
public final class DynamicProgramming {
    private DynamicProgramming() {}

    /** Min coins to make amount, or -1. O(amount * coins.length). */
    public static int coinChange(int[] coins, int amount) {
        int[] best = new int[amount + 1];
        Arrays.fill(best, Integer.MAX_VALUE);
        best[0] = 0;
        for (int a = 1; a <= amount; a++) {
            for (int c : coins) {
                if (c <= a && best[a - c] != Integer.MAX_VALUE) {
                    best[a] = Math.min(best[a], best[a - c] + 1);
                }
            }
        }
        return best[amount] == Integer.MAX_VALUE ? -1 : best[amount];
    }

    /**
     * Number of monotone (right/down) paths from (0,0) to bottom-right, avoiding blocked cells.
     * Space-optimised 1D tabulation.
     */
    public static long countGridPaths(boolean[][] blocked) {
        int rows = blocked.length, cols = blocked[0].length;
        long[] dp = new long[cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (blocked[r][c]) dp[c] = 0;
                else if (r == 0 && c == 0) dp[c] = 1;
                else if (c > 0) dp[c] += dp[c - 1];
            }
        }
        return dp[cols - 1];
    }

    /**
     * Runs a deep recursion on a thread with a larger stack.
     * Useful when recursive depth exceeds the default thread stack (typically ~10K frames).
     *
     * @param stackBytes stack size hint for the JVM (e.g. 512L * 1024 * 1024 for 512MB)
     */
    public static <T> T runWithStack(long stackBytes, Supplier<T> task) {
        Object[] result = new Object[1];
        Throwable[] failure = new Throwable[1];
        Thread t = new Thread(null, () -> {
            try { result[0] = task.get(); }
            catch (Throwable e) { failure[0] = e; }
        }, "deep-recursion", stackBytes);
        t.start();
        try { t.join(); }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted", e);
        }
        if (failure[0] instanceof RuntimeException re) throw re;
        if (failure[0] instanceof Error err) throw err;
        if (failure[0] != null) throw new IllegalStateException("task failed", failure[0]);
        @SuppressWarnings("unchecked") T value = (T) result[0];
        return value;
    }
}
