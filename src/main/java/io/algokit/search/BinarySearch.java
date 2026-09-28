package io.algokit.search;

import java.util.function.IntPredicate;
import java.util.function.LongPredicate;

/**
 * The canonical binary search template built on a monotone predicate.
 *
 * <p>Instead of searching for a specific value, you define a predicate that is
 * {@code false} for all indices below some boundary, then {@code true} from that
 * boundary onward. {@code firstTrue} finds that boundary.
 *
 * <p>Every classic binary search variant (lower bound, upper bound, search-on-answer)
 * reduces to a one-liner using this template.
 */
public final class BinarySearch {
    private BinarySearch() {}

    /**
     * Smallest x in [lo, hi) where {@code pred.test(x)} is true, or hi if none.
     * Precondition: pred is monotone on [lo, hi) — false...false true...true.
     */
    public static int firstTrue(int lo, int hi, IntPredicate pred) {
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (pred.test(mid)) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    /** Same template over a long domain. Distinct name avoids ambiguity with implicit lambdas. */
    public static long firstTrueLong(long lo, long hi, LongPredicate pred) {
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (pred.test(mid)) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }

    /** First index with {@code a[i] >= target}. Returns a.length if none. Equivalent to C++ lower_bound. */
    public static int lowerBound(int[] a, int target) {
        return firstTrue(0, a.length, i -> a[i] >= target);
    }

    /** First index with {@code a[i] > target}. Returns a.length if none. Equivalent to C++ upper_bound. */
    public static int upperBound(int[] a, int target) {
        return firstTrue(0, a.length, i -> a[i] > target);
    }

    /** Index of target in a sorted array, or -1. */
    public static int indexOf(int[] a, int target) {
        int i = lowerBound(a, target);
        return (i < a.length && a[i] == target) ? i : -1;
    }
}
