# Binary Search

**Package:** `io.algokit.search`  
**Source:** [`BinarySearch.java`](https://github.com/maneeshchaturvedi/algokit-java/blob/main/src/main/java/io/algokit/search/BinarySearch.java)

Binary search is one of the most error-prone patterns in interview coding — off-by-one bugs in loop bounds, increment logic, and return values cause silent wrong answers that are hard to debug under pressure. This library escapes the problem entirely by exposing a **single primitive** (`firstTrue`) built on a monotone predicate, then deriving every classic variant as a one-liner. Once you internalize the predicate framing, you will never write a raw binary search loop again.

---

## The Predicate-Based Approach

The key insight is that every binary search over a sorted space looks for a **boundary**: a point where some Boolean condition flips from `false` to `true`. Instead of searching for a specific value, you specify *what you want to be true* and let `firstTrue` find the earliest position where it is.

```
Index:     0     1     2     3     4     5     6
Values:   [3,    5,    5,    7,    9,    12,   15]
Predicate (a[i] >= 7):
           F     F     F     T     T     T     T
                              ^
                         firstTrue returns 3
```

This framing is strictly more general than "find value X in sorted array" — it handles lower bound, upper bound, existence checks, and even **binary search on the answer** (searching over a range of candidate integers) all with the same template.

### Why `firstTrue` beats raw binary search

A raw binary search requires you to reason simultaneously about loop invariants, which comparison to use (`<` vs `<=`), and what to return (`lo` vs `hi` vs `mid`). The predicate template collapses this to one question: *"For a given index, is the predicate true?"* The loop mechanics are fixed; you only provide the predicate.

---

## `firstTrue`

### Signature
```java
public static int firstTrue(int lo, int hi, IntPredicate pred)
```

### How and why it works
Maintains the invariant that the answer lies in the half-open interval `[lo, hi)`. At each step, `mid = lo + (hi - lo) / 2` (overflow-safe). If `pred.test(mid)` is true, the answer is at `mid` or earlier, so `hi = mid` (not `mid - 1`, because `mid` itself might be the answer). If false, the answer must be strictly to the right, so `lo = mid + 1`. When `lo == hi`, both pointers converge on the answer. The function returns `hi` when no index in `[lo, hi)` satisfies the predicate, making the "not found" case explicit and easy to check.

### Complexity
- **Time:** O(log(hi - lo) · cost of pred)
- **Space:** O(1)

### Usage example
```java
// Smallest integer x in [1, 100] where x * x >= 50
int sqrt50 = BinarySearch.firstTrue(1, 101, x -> (long) x * x >= 50); // 8

// Minimum speed such that you can finish all trips in 'hours' (LC 1870)
int minSpeed = BinarySearch.firstTrue(1, MAX_SPEED + 1, speed -> canFinish(trips, speed, hours));

// First position where value would fit in a sorted list
int[] a = {2, 4, 6, 8};
int pos = BinarySearch.firstTrue(0, a.length, i -> a[i] >= 5); // 2  (a[2] == 6)
```

### Related LeetCode problems
- **704** – Binary Search (simplest possible use)
- **35**  – Search Insert Position
- **1011** – Capacity To Ship Packages Within D Days
- **1870** – Minimum Speed to Arrive on Time
- **410** – Split Array Largest Sum

### Common variants
- **Circular arrays:** transform the predicate to account for the rotation offset.
- **Real-valued search:** use `firstTrueLong` with scaled integer representations, or a floating-point loop with a fixed number of iterations.

### Gotchas
- The range is **half-open** `[lo, hi)`. Pass `hi = a.length`, not `a.length - 1`, or you will never check the last element.
- The predicate **must be monotone** on `[lo, hi)`: all `false` values must precede all `true` values. Passing a non-monotone predicate produces silently incorrect results.
- `mid = lo + (hi - lo) / 2` is required (not `(lo + hi) / 2`) to avoid integer overflow when `lo` and `hi` are both near `Integer.MAX_VALUE`.

---

## `firstTrueLong`

### Signature
```java
public static long firstTrueLong(long lo, long hi, LongPredicate pred)
```

### How and why it works
Identical algorithm to `firstTrue` but operates over a `long` domain. Java's type system cannot unify `IntPredicate` and `LongPredicate`, so a separate method name avoids an ambiguous overload when callers use a lambda — the compiler cannot infer which functional interface to target. Use this variant whenever the search space exceeds `Integer.MAX_VALUE` (e.g., searching over file sizes in bytes, distances in nanometers, or time in milliseconds).

### Complexity
- **Time:** O(log(hi - lo) · cost of pred)
- **Space:** O(1)

### Usage example
```java
// Minimum number of operations (could be up to 10^14)
long minOps = BinarySearch.firstTrueLong(1L, (long) 1e14 + 1, ops -> isPossible(ops));

// Square root (integer) via binary search on longs
long sqrtN = BinarySearch.firstTrueLong(0L, (long) 2e9 + 1, x -> x * x >= n) - 1;
// subtract 1 because we found first x where x^2 >= n; we want last x where x^2 <= n
```

### Related LeetCode problems
- **1283** – Find the Smallest Divisor Given a Threshold
- **1760** – Minimum Limit of Balls in a Bag
- **2064** – Minimized Maximum of Products Distributed to Any Store

### Common variants
- Binary search on a real-valued answer: use a floating-point loop instead — run ~100 iterations of `mid = (lo + hi) / 2.0` for precision without needing epsilon logic.

### Gotchas
- Use `lo + (hi - lo) / 2` not `(lo + hi) / 2` to avoid `long` overflow when both bounds are near `Long.MAX_VALUE / 2`.
- A separate named method (`firstTrueLong`) is intentional. Adding an overload `firstTrue(long, long, LongPredicate)` would cause an ambiguous call when the predicate is written as a lambda.

---

## `lowerBound`

### Signature
```java
public static int lowerBound(int[] a, int target)
```

### How and why it works
Returns the first index `i` where `a[i] >= target`, or `a.length` if no such index exists. This is the direct equivalent of C++'s `std::lower_bound`. The implementation is a literal one-liner: `firstTrue(0, a.length, i -> a[i] >= target)`. Reading the predicate aloud tells you everything — "find the first position where the element is at least target." If all elements are smaller, `firstTrue` returns `a.length` (the "none found" sentinel from the half-open range).

### Complexity
- **Time:** O(log n)
- **Space:** O(1)

### Usage example
```java
int[] sorted = {1, 3, 5, 5, 7, 9};

BinarySearch.lowerBound(sorted, 5);  // 2  (first index where a[i] >= 5)
BinarySearch.lowerBound(sorted, 6);  // 4  (first index where a[i] >= 6, i.e., 7)
BinarySearch.lowerBound(sorted, 10); // 6  (a.length — no element >= 10)
BinarySearch.lowerBound(sorted, 0);  // 0  (every element >= 0)

// Count of elements equal to target
int lo = BinarySearch.lowerBound(sorted, 5);
int hi = BinarySearch.upperBound(sorted, 5);
int count = hi - lo; // 2
```

### Related LeetCode problems
- **34**  – Find First and Last Position of Element in Sorted Array
- **300** – Longest Increasing Subsequence (patience sorting uses lowerBound)
- **354** – Russian Doll Envelopes

### Common variants
- Count elements strictly less than target: `lowerBound(a, target)` gives the count directly.
- Last index where `a[i] <= target`: `upperBound(a, target) - 1`.

### Gotchas
- `lowerBound` returns `a.length` (not `-1`) when target is not found. Always guard the returned index with a bounds check before accessing the array.
- On an unsorted array, the predicate `a[i] >= target` is not monotone, and the result is undefined.

---

## `upperBound`

### Signature
```java
public static int upperBound(int[] a, int target)
```

### How and why it works
Returns the first index `i` where `a[i] > target`, or `a.length` if none exists. Equivalent to C++'s `std::upper_bound`. Implementation: `firstTrue(0, a.length, i -> a[i] > target)`. Together, `lowerBound` and `upperBound` define the half-open range `[lowerBound, upperBound)` that contains all elements equal to `target` — a pattern used constantly in problems that ask for "count of occurrences", "range of a value", or "insert position".

### Complexity
- **Time:** O(log n)
- **Space:** O(1)

### Usage example
```java
int[] sorted = {1, 3, 5, 5, 7, 9};

BinarySearch.upperBound(sorted, 5);  // 4  (first index where a[i] > 5, i.e., 7)
BinarySearch.upperBound(sorted, 4);  // 2  (first index > 4, which is a[2]=5)
BinarySearch.upperBound(sorted, 9);  // 6  (a.length — no element > 9)

// Count of target in sorted array
int lo = BinarySearch.lowerBound(sorted, 5); // 2
int hi = BinarySearch.upperBound(sorted, 5); // 4
int count = hi - lo; // 2

// Insert after all equal elements (stable insertion sort position)
int insertAfter = BinarySearch.upperBound(sorted, 5); // insert at index 4
```

### Related LeetCode problems
- **34**  – Find First and Last Position of Element in Sorted Array
- **981** – Time Based Key-Value Store

### Common variants
- Last occurrence of target: `upperBound(a, target) - 1` (then verify `a[result] == target`).

### Gotchas
- `upperBound(a, target) - lowerBound(a, target)` gives the count of `target` in the array. Both return `a.length` for a missing target, so the difference is correctly `0`.
- Do not confuse with "index of target" — `upperBound` returns the position *after* the last occurrence.

---

## `indexOf`

### Signature
```java
public static int indexOf(int[] a, int target)
```

### How and why it works
Finds `target` in a sorted array, returning its index or `-1` if absent. Built on `lowerBound`: find the first position where `a[i] >= target`, then confirm the element actually equals target. This two-step approach is cleaner than writing a dedicated loop — it reuses the already-correct `lowerBound` logic and adds only one comparison. The `-1` sentinel is deliberately different from `lowerBound`'s `a.length` sentinel to signal "not found" in a way that callers can distinguish from a valid index.

### Complexity
- **Time:** O(log n)
- **Space:** O(1)

### Usage example
```java
int[] sorted = {2, 5, 7, 9, 11};

BinarySearch.indexOf(sorted, 7);   // 2
BinarySearch.indexOf(sorted, 6);   // -1
BinarySearch.indexOf(sorted, 11);  // 4
BinarySearch.indexOf(sorted, 12);  // -1

// Presence check
boolean found = BinarySearch.indexOf(sorted, 9) != -1; // true
```

### Related LeetCode problems
- **704** – Binary Search
- **33**  – Search in Rotated Sorted Array (requires a modified predicate, not this direct call)
- **74**  – Search a 2D Matrix

### Common variants
- For the first occurrence of `target` in an array with duplicates, `indexOf` returns *an* occurrence (specifically the leftmost one, since it delegates to `lowerBound`).
- For searching in a `List<Integer>`, use `frequencies` or `Collections.binarySearch` instead.

### Gotchas
- `indexOf` assumes the array is **sorted in ascending order**. On an unsorted array, it may return `-1` even if the element exists, with no error.
- If duplicates exist, `indexOf` returns the **first** occurrence because it delegates to `lowerBound`. This differs from `java.util.Arrays.binarySearch`, which returns an unspecified duplicate index.

---

## Binary Search on the Answer Pattern

Many problems that appear to require exhaustive search can be solved in O(n log(answer_range)) using this pattern:

1. **Identify that the answer is monotone:** if speed `s` is sufficient to finish in time, then any speed `> s` also works. If you can partition with max-sum `k`, you can also do it with any `k' > k`.
2. **Set `lo` and `hi`** to the minimum and maximum possible answers.
3. **Write a feasibility predicate** that checks "is this candidate answer valid?"
4. **Call `firstTrue(lo, hi+1, pred)`** for "minimum valid answer", or transform to `firstTrue` for maximum.

```java
// LC 1011: Minimum weight capacity to ship all packages in D days
int minCapacity = BinarySearch.firstTrue(
    Arrays.stream(weights).max().getAsInt(),  // lo: must carry heaviest package
    Arrays.stream(weights).sum() + 1,         // hi: carry all in 1 day
    cap -> {
        int days = 1, load = 0;
        for (int w : weights) {
            if (load + w > cap) { days++; load = 0; }
            load += w;
        }
        return days <= D;
    }
);
```

This pattern collapses a problem that might naively require O(answer_range × n) brute force into O(n log(answer_range)).
