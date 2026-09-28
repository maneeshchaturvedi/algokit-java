# Monotonic Stack

**Package:** `io.algokit.stack`  
**Source:** [`MonotonicStack.java`](https://github.com/maneeshchaturvedi/algokit-java/blob/main/src/main/java/io/algokit/stack/MonotonicStack.java)

A monotonic stack is a stack that maintains a strictly increasing (or decreasing) sequence of values from bottom to top. It answers "nearest neighbor" questions — for each element, find the next (or previous) element that is larger (or smaller) — in O(n) total time rather than the O(n²) of brute-force nested loops. The same core data structure, when applied to a deque with an expiry rule, becomes a sliding window min/max in O(n).

---

## The Monotonic Invariant

The stack stores **indices** (not values). It maintains the invariant that the values at those indices are in monotone order. When processing index `i`:

1. Pop all indices `j` where `a[j]` violates the monotone order with `a[i]`.
2. For each popped `j`, the current `i` is the answer to "what is the next element greater than `a[j]`?"
3. Push `i`.

Because each index is pushed once and popped at most once, the total work across all iterations is O(n). This amortized cost makes monotonic stacks fundamentally more efficient than the naive O(n²) approach.

```
Array:  [2, 1, 5, 3, 6, 4, 8, 2]
Stack   (indices, showing values):

i=0:  push 0.        Stack: [0(2)]
i=1:  2>1? no push   Stack: [0(2), 1(1)]
i=2:  a[1]=1 < 5, pop 1 -> next[1]=2; a[0]=2 < 5, pop 0 -> next[0]=2; push 2
      Stack: [2(5)]
i=3:  5>3, push 3.   Stack: [2(5), 3(3)]
...
```

---

## `nextGreaterIndex`

### Signature
```java
public static int[] nextGreaterIndex(int[] a)
```

### How and why it works
For each index `i`, finds the smallest index `j > i` such that `a[j] > a[i]`, storing `j` in `next[i]`. If no such `j` exists, `next[i] = -1` (pre-filled by `Arrays.fill`). The stack stores indices in a monotone decreasing order of their values — the top of the stack always holds the index of the most recently seen element that hasn't found its "next greater" yet. When element `a[i]` arrives and is greater than `a[stack.peek()]`, that peek index has found its answer: `i`. This process continues (popping multiple elements) until either the stack is empty or the top's value is already ≥ `a[i]`.

### Complexity
- **Time:** O(n) — each index is pushed once and popped once
- **Space:** O(n) for the result array and O(n) worst-case stack depth (a strictly descending input never pops)

### Source code
```java
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
```

### Usage example
```java
int[] a = {2, 1, 5, 3, 6, 4, 8, 2};
int[] next = MonotonicStack.nextGreaterIndex(a);
// next = [2, 2, 4, 4, 6, 6, -1, -1]
// a[0]=2, next greater is a[2]=5 at index 2
// a[6]=8, no element greater exists -> -1

// Span of a stock price (LC 901)
// span[i] = number of consecutive days <= a[i] ending at i
int[] span = new int[a.length];
// ... use prevGreater variant

// Temperature daily wait (LC 739)
int[] days = MonotonicStack.nextGreaterIndex(temperatures);
// days[i] = answer if none found (-1 vs expected 0: post-process)
for (int i = 0; i < days.length; i++) {
    days[i] = days[i] == -1 ? 0 : days[i] - i;
}
```

### Related LeetCode problems
- **739** – Daily Temperatures (next greater element with distance, not index)
- **496** – Next Greater Element I
- **503** – Next Greater Element II (circular array)
- **901** – Online Stock Span (previous greater element)
- **84**  – Largest Rectangle in Histogram (uses previous and next smaller)
- **85**  – Maximal Rectangle (extends LC 84 to 2D)
- **42**  – Trapping Rain Water (uses next/previous greater)

### All four variants

The four combinations — next/previous × greater/smaller — each require a one-line change:

| Variant | Change | Direction |
|---|---|---|
| Next greater (default) | `a[stack.peek()] < a[i]` | left → right |
| Next smaller | `a[stack.peek()] > a[i]` | left → right |
| Previous greater | `a[stack.peek()] < a[i]` | right → left |
| Previous smaller | `a[stack.peek()] > a[i]` | right → left |

For "previous" variants, iterate `i` from `a.length - 1` down to `0`.

For "greater or equal" (non-strict), change `<` to `<=`.

```java
// Previous smaller element index
int[] prevSmaller(int[] a) {
    int[] prev = new int[a.length];
    Arrays.fill(prev, -1);
    Deque<Integer> stack = new ArrayDeque<>();
    for (int i = a.length - 1; i >= 0; i--) {      // iterate right to left
        while (!stack.isEmpty() && a[stack.peek()] > a[i]) {  // flip comparison
            prev[stack.pop()] = i;
        }
        stack.push(i);
    }
    return prev;
}
```

### Gotchas
- The stack stores **indices**, not values. Storing values loses the index information needed to compute distances (`days[i] - i` in LC 739) and to fill the result array.
- `Arrays.fill(next, -1)` runs before the loop so that indices remaining on the stack after the loop have their answer set correctly without a second pass.
- For circular array variants (LC 503), iterate `0..2n-1` using `i % n` to index, but only record answers for `i < n`.
- `a[stack.peek()] < a[i]` uses **strict** less-than. Equal elements do not trigger a pop — so equal elements do not count as "greater." Use `<=` if the problem defines "greater or equal."

---

## `windowMin`

### Signature
```java
public static int[] windowMin(int[] a, int k)
```

### How and why it works
Computes the minimum of every contiguous window of size `k` using a **monotonic deque** — a double-ended queue that maintains indices in increasing order of their values from front to back (so the front always holds the index of the current window's minimum). On each step:

1. **Expire front:** if `dq.peekFirst() == i - k`, that index has slid out of the window — remove it.
2. **Maintain monotone invariant at back:** while the back of the deque holds an index whose value `>= a[i]`, pop it — it can never be the minimum for any future window (it's both older and not smaller).
3. **Add `i`** to the back.
4. **Record result:** once `i >= k - 1`, the front of the deque is the index of the minimum for this window.

Each index enters and exits the deque at most once, giving O(n) overall.

### Complexity
- **Time:** O(n) — each index enters and exits the deque at most once
- **Space:** O(k) for the deque, O(n - k + 1) for the output array

### Source code
```java
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
```

### Usage example
```java
int[] a = {1, 3, -1, -3, 5, 3, 6, 7};
int[] mins = MonotonicStack.windowMin(a, 3);
// mins = [-1, -3, -3, -3, 3, 3]
// Window [1,3,-1]→-1, [3,-1,-3]→-3, [-1,-3,5]→-3, [-3,5,3]→-3, [5,3,6]→3, [3,6,7]→3

// Sliding window maximum: change >= to <=
// out[i - k + 1] = a[dq.peekFirst()] gives max instead
```

### Related LeetCode problems
- **239** – Sliding Window Maximum (change `>=` to `<=` for window maximum)
- **862** – Shortest Subarray with Sum at Least K (uses monotone deque on prefix sums)
- **1438** – Longest Continuous Subarray With Absolute Diff Less Than or Equal to Limit (maintain both min and max deques)
- **2398** – Maximum Number of Robots Within Budget (window max + window sum)

### Window maximum variant

Change the one condition `a[dq.peekLast()] >= a[i]` to `a[dq.peekLast()] <= a[i]`:

```java
// Window maximum
while (!dq.isEmpty() && a[dq.peekLast()] <= a[i]) dq.pollLast();
```

Everything else is identical. The deque then maintains indices in decreasing order of values, with the maximum at the front.

### Common variants
- **Variable-size window min/max:** remove the fixed expiry condition and instead add an outer pointer-based loop (used in LC 1438).
- **Min and max simultaneously:** maintain two deques — one for min, one for max. Used when a window constraint involves `max - min <= limit`.
- **Deque on prefix sums (LC 862):** store prefix-sum indices in a monotone deque to find the shortest subarray with a given sum even when values can be negative.

### Gotchas
- The expiry check `dq.peekFirst() == i - k` uses `==` not `<=`. Since we process indices in order and always maintain the deque, the front can only be exactly `i - k` when it needs expiring — it will never be older, because we would have expired it earlier.
- `a[dq.peekLast()] >= a[i]` uses `>=` (not `>`). Keeping equal values would leave stale indices in the deque that contribute nothing useful; removing them keeps the deque size bounded.
- For `windowMax`, the inequality flips to `<=`. Mixing up min vs max is the most common mistake.
- Output length is `n - k + 1`, not `n`. Allocating `n` elements and leaving the extras as zero is a silent bug.
