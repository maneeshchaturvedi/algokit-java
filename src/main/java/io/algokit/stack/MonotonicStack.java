package io.algokit.stack;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Monotonic stack and deque templates for next-greater-element and sliding window min/max.
 */
public final class MonotonicStack {
    private MonotonicStack() {}

    /**
     * For each index i, the index of the next element to the right that is strictly greater, or -1.
     *
     * <p>Variants: flip {@code <} to {@code >} for next-smaller; iterate right-to-left for
     * previous-greater/smaller; use {@code <=} for "greater or equal".
     */
    public static int[] nextGreaterIndex(int[] a) {
        int[] next = new int[a.length];
        Arrays.fill(next, -1);
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < a.length; i++) {
            while (!stack.isEmpty() && a[stack.peek()] < a[i]) {
                next[stack.pop()] = i;
            }
            stack.push(i);
        }
        return next;
    }

    /**
     * Minimum of every window of size k (monotonic deque). O(n).
     *
     * <p>Change {@code >=} to {@code <=} for window maximum.
     */
    public static int[] windowMin(int[] a, int k) {
        if (k < 1 || k > a.length) throw new IllegalArgumentException("need 1 <= k <= n");
        int[] out = new int[a.length - k + 1];
        Deque<Integer> dq = new ArrayDeque<>();
        for (int i = 0; i < a.length; i++) {
            if (!dq.isEmpty() && dq.peekFirst() == i - k) dq.pollFirst();
            while (!dq.isEmpty() && a[dq.peekLast()] >= a[i]) dq.pollLast();
            dq.offerLast(i);
            if (i >= k - 1) out[i - k + 1] = a[dq.peekFirst()];
        }
        return out;
    }
}
