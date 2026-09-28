# Sliding Window

**Package:** `io.algokit.window`  
**Source:** [`SlidingWindow.java`](https://github.com/maneeshchaturvedi/algokit-java/blob/main/src/main/java/io/algokit/window/SlidingWindow.java)

The sliding window pattern solves subarray/substring problems that would naively require O(n²) nested loops. The idea: maintain a window `[left, right]` that you expand by advancing `right` one step at a time, and shrink by advancing `left` when the window violates some constraint. The amortized cost is O(n) because each index enters and leaves the window exactly once.

This library provides four templates covering the main shapes: fixed-size windows, longest-valid windows, shortest-valid windows, and windows bounded by distinct-value counts.

---

## The Monotonicity Requirement

Sliding window works because the window's validity has a **monotone** relationship with its size. Specifically:

- **Longest valid:** if a window is invalid, making it larger cannot make it valid (with non-negative values, a sum can only grow as you add more). So when the window becomes invalid, shrink from the left.
- **Shortest valid:** if a window satisfies the target, making it smaller might still satisfy it. So when valid, try to shrink; when invalid, expand.

**Critical:** this monotone property holds for sums of **non-negative** values only. With negative values, shrinking the window can actually increase the sum. For those cases, use the prefix-sum + HashMap approach from `countSubarraysWithSum`.

---

## `maxSumWindow`

### Signature
```java
public static long maxSumWindow(int[] nums, int k)
```

### How and why it works
Computes the maximum sum of any contiguous subarray of exactly `k` elements. The first window (indices 0 to k-1) is built with a simple loop. Every subsequent window is obtained in O(1) by adding the incoming right element and subtracting the outgoing left element (`nums[right - k]`). This "slide" operation maintains the invariant that `window` always contains the sum of exactly `k` consecutive elements. Using `long` for `window` avoids overflow when values are large. The function validates its input upfront to fail loudly rather than return a nonsensical result.

### Complexity
- **Time:** O(n)
- **Space:** O(1)

### Usage example
```java
int[] nums = {2, 3, 4, 1, 5};
long max = SlidingWindow.maxSumWindow(nums, 3); // 10  (subarray [3,4,1]? No: [4,1,5]=10)
// Windows: [2,3,4]=9, [3,4,1]=8, [4,1,5]=10  => best=10

// Average of best k-element window
double avgMax = (double) SlidingWindow.maxSumWindow(nums, 3) / 3; // 3.333...
```

### Related LeetCode problems
- **643** – Maximum Average Subarray I
- **1343** – Number of Sub-arrays of Size K and Average Greater than or Equal to Threshold
- **2090** – K Radius Subarray Averages

### Common variants
- **Minimum sum window:** replace `Math.max` with `Math.min` and initialize `best` to `Long.MAX_VALUE`.
- **Fixed-size with a different aggregate:** replace sum with product, XOR, or any associative operation that supports an O(1) "remove leftmost" operation.
- **Count windows meeting a threshold:** wrap `maxSumWindow` logic in a counter.

### Gotchas
- Throws `IllegalArgumentException` when `k <= 0` or `k > nums.length`. Callers that might pass edge-case values should guard upfront.
- The subtracted element uses `(long) nums[right - k]` to widen before subtraction — important if `window` is `long` and `nums[right - k]` is `int`.

---

## `longestSumAtMost`

### Signature
```java
public static int longestSumAtMost(int[] nums, long limit)
```

### How and why it works
Expands the window by advancing `right`, then shrinks from `left` while the sum exceeds `limit`. Because all values are non-negative, `windowSum` can only increase when `right` advances and can only decrease when `left` advances — the "grow until invalid, shrink until valid" loop correctly finds the longest window that stays within budget. After shrinking (or skipping if already valid), the current window size `right - left + 1` is a candidate for the best length. The `left` pointer never moves backward, giving amortized O(n) total work.

### Complexity
- **Time:** O(n)
- **Space:** O(1)

### Usage example
```java
int[] nums = {3, 1, 2, 5, 1};

// Longest subarray with sum at most 8
SlidingWindow.longestSumAtMost(nums, 8); // 4  ([3,1,2] len 3? [1,2,5]=8 len 3? [3,1,2,... ] No)
// Windows: [3]=3, [3,1]=4, [3,1,2]=6, [3,1,2,5]=11>8 shrink
// after shrink: [1,2,5]=8 len 3, [1,2,5,1]=9>8, shrink: [2,5,1]=8 len 3
// Actually best=3... let's verify: [1,2,5,1]=9, [1,2,5]=8✓ len=3, [3,1,2]=6 len=3  => 3
SlidingWindow.longestSumAtMost(new int[]{1, 1, 1, 1}, 3); // 3  ([1,1,1])

// Number of fruits (at most 2 types = at most 2 distinct -> use longestAtMostKDistinct)
```

### Related LeetCode problems
- **209** – Minimum Size Subarray Sum (inverse: shortest where sum >= target)
- **713** – Subarray Product Less Than K (same pattern with product)
- **1040** – Moving Stones Until Consecutive II

### Common variants
- **Product version:** replace sum with product; be careful with zeros (product becomes 0, requiring special handling).
- **With negatives:** cannot use this template — use prefix sums + binary search or deque-based approaches.
- Combine with a frequency map (see `longestAtMostKDistinct`) for string/character constraints.

### Gotchas
- **Non-negative values only.** With negatives, shrinking the left end can increase the sum, breaking the invariant that "shrinking makes the window more valid."
- Returns 0 for an empty array or when every single element exceeds the limit (e.g., `nums = {10}, limit = 5` → the window shrinks past `left = right`, making `right - left + 1 = 0`, which never beats `best = 0`).

---

## `minSubArrayLen`

### Signature
```java
public static int minSubArrayLen(int[] nums, long target)
```

### How and why it works
The mirror image of `longestSumAtMost`. Expand until the window sum reaches `target`, then record the window length and keep shrinking from the left to find a shorter window that still meets the target. The inner `while` condition `windowSum >= target && left <= right` ensures we record every valid window during the shrink phase — not just the first one after expansion. Returns 0 (not `Integer.MAX_VALUE`) when no subarray reaches the target, which matches the LeetCode convention.

### Complexity
- **Time:** O(n)
- **Space:** O(1)

### Usage example
```java
// LC 209 example
SlidingWindow.minSubArrayLen(new int[]{2, 3, 1, 2, 4, 3}, 7); // 2  ([4,3])
SlidingWindow.minSubArrayLen(new int[]{1, 4, 4}, 4);          // 1  ([4])
SlidingWindow.minSubArrayLen(new int[]{1, 1, 1, 1, 1}, 11);   // 0  (impossible)

// Minimum consecutive days of charging to reach battery goal
int minDays = SlidingWindow.minSubArrayLen(dailyCharge, batteryGoal);
```

### Related LeetCode problems
- **209** – Minimum Size Subarray Sum (this exact problem)
- **862** – Shortest Subarray with Sum at Least K (note: allows negatives, requires deque)
- **1658** – Minimum Operations to Reduce X to Zero (transform to max window with sum = total - x)

### Common variants
- **With negatives (LC 862):** The two-pointer approach breaks because the sum is no longer monotone. Use a monotonic deque on prefix sums instead.
- **Exact sum:** for non-negative arrays, `minSubArrayLen` where you break as soon as `windowSum == target`.

### Gotchas
- The `left <= right` guard in the `while` prevents `left` from overshooting `right` in the edge case where a single element exactly equals `target`.
- Returns `0` (not `-1` or `Integer.MAX_VALUE`) for "not found." This is deliberate convention — check `== 0` at the call site.
- **Non-negative values only** for the same reason as `longestSumAtMost`.

---

## `longestAtMostKDistinct`

### Signature
```java
public static int longestAtMostKDistinct(int[] a, int k)
```

### How and why it works
Tracks which values are currently in the window using a `HashMap<Integer, Integer>` that maps each value to its count within the window. When the number of distinct values (`inWindow.size()`) exceeds `k`, the window is invalid: shrink by decrementing the count of `a[left]` and advancing `left`. The critical idiom is the **merge-to-null** trick on removal:

```java
inWindow.merge(a[left], -1, (old, delta) -> old + delta == 0 ? null : old + delta);
```

When the count reaches zero, the merge callback returns `null`, which causes `Map.merge` to **remove the key entirely** — keeping `inWindow.size()` accurate without a separate `remove` call. This is more concise and efficient than `getOrDefault` + conditional `remove`.

### Complexity
- **Time:** O(n) amortized — each element enters and exits the map once
- **Space:** O(k) — the map holds at most k+1 keys at any moment (briefly, before shrinking)

### Usage example
```java
// LC 340: Longest Substring with At Most K Distinct Characters
int[] chars = "eceba".chars().toArray();
SlidingWindow.longestAtMostKDistinct(chars, 2); // 3  ("ece")

// LC 904: Fruit Into Baskets (at most 2 distinct fruit types)
SlidingWindow.longestAtMostKDistinct(fruits, 2);

// Exactly k distinct: atMost(k) - atMost(k-1)
int longestExactlyK = SlidingWindow.longestAtMostKDistinct(a, k)
                    - SlidingWindow.longestAtMostKDistinct(a, k - 1);
```

### Related LeetCode problems
- **340** – Longest Substring with At Most K Distinct Characters
- **904** – Fruit Into Baskets (k = 2)
- **159** – Longest Substring with At Most Two Distinct Characters (k = 2)
- **992** – Subarrays with K Different Integers (use exactly-k = atMost(k) - atMost(k-1))

### Common variants
- **Exactly k distinct:** `longestAtMostKDistinct(a, k) - longestAtMostKDistinct(a, k-1)` for count of subarrays; adapt for longest subarray with exactly k distinct.
- **String input:** convert with `s.chars().toArray()` or map characters to ints.
- **Count subarrays (not just longest):** change `best = Math.max(best, right - left + 1)` to `count += right - left + 1` — this counts all valid subarrays ending at `right`.

### Gotchas
- The merge-to-null idiom `(o, d) -> o + d == 0 ? null : o + d` is a Java-specific `Map.merge` feature: returning `null` from the remapping function causes the key to be deleted. This is correct and intentional — do not replace it with a simpler `put(key, count - 1)` without also adding a `if (count == 1) remove(key)` branch.
- `inWindow.size()` tracks distinct count, not total elements. Ensure you do not confuse the two.
- When `k == 0`, the window is always invalid (size immediately becomes 1 > 0 on the first element). The result will be 0, which is correct — no subarray can have 0 distinct values unless empty.
- For character-based problems, pass `s.chars().toArray()` to avoid per-character `Character` boxing overhead.
