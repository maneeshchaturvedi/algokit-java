# Backtracking API Reference

Package: `io.algokit.backtrack`

Backtracking templates for combinatorial enumeration. All methods are static utilities on
`Backtracking`.

---

## The choose / explore / un-choose pattern

Every backtracking algorithm is the same skeleton, applied to a different problem:

```
backtrack(state, path, output):
    if goal(state):
        output.add(copy(path))   // COPY — never store a reference to path
        return                   // (sometimes continue instead of return)
    for each choice in choices(state):
        path.add(choice)         // CHOOSE
        backtrack(next(state, choice), path, output)  // EXPLORE
        path.remove(last)        // UN-CHOOSE
```

**Why you must copy the path.** `path` is a single mutable list shared across all recursive
calls. When you add it to `out`, you are storing a reference to the current working list. If
you later modify `path` (which you will — the algorithm continues), every previously stored
reference reflects those modifications. Always call `new ArrayList<>(path)`.

**The recursion tree mental model.** The call tree has one node per state. At each node, the
for-loop iterates over branches (choices). Un-choosing restores the state so the next branch
starts from the same parent state. This is why the pattern is called *backtracking*: you
literally back up to the parent before exploring the next child.

---

## subsets

```java
public static List<List<Integer>> subsets(int[] nums)
// All 2^n subsets of distinct values; input assumed to have no duplicates.
```

**How it works.** At each call, record the current `path` as a valid subset (including the
empty set). Then try extending the path with each element from `start` onward. Passing `i + 1`
as the new `start` ensures:
1. Each element is used at most once per subset.
2. Subsets are generated in sorted order (no duplicates like {2,1} and {1,2}).

```
subsets([1,2,3]):
  record []
  choose 1 → record [1]
    choose 2 → record [1,2]
      choose 3 → record [1,2,3]
    choose 3 → record [1,3]
  choose 2 → record [2]
    choose 3 → record [2,3]
  choose 3 → record [3]
Result: [], [1], [1,2], [1,2,3], [1,3], [2], [2,3], [3]
```

**Complexity.** O(n · 2^n) time (2^n subsets, each copied in O(n)), O(n) extra space for the
path (not counting output).

**Usage**

```java
List<List<Integer>> all = Backtracking.subsets(new int[]{1, 2, 3});
// 8 subsets
```

**Related LeetCode problems.** 78 (Subsets — exact match), 90 (Subsets II — with duplicates,
sort input and skip `nums[i] == nums[i-1]` at the same depth), 491 (Non-decreasing Subsequences
— values must be non-decreasing, cannot sort input so use a HashSet per level to deduplicate).

**Variant — subsets with duplicates (LC 90).** Sort `nums` first. In the loop, skip `nums[i]`
when `i > start && nums[i] == nums[i-1]`:

```java
Arrays.sort(nums);
for (int i = start; i < nums.length; i++) {
    if (i > start && nums[i] == nums[i-1]) continue;  // skip duplicate branch
    path.add(nums[i]);
    helper(nums, i + 1, path, out);
    path.remove(path.size() - 1);
}
```

---

## combine

```java
public static List<List<Integer>> combine(int n, int k)
// All k-element combinations of 1..n  (LC 77).
```

**How it works.** Same skeleton as `subsets` but with two changes:
1. Recurse only until `path.size() == k` (emit and return instead of emitting at every call).
2. Prune the loop upper bound to `n - stillNeeded + 1`.

**The pruning.** `stillNeeded = k - path.size()` is the number of elements still required.
The loop can stop at `n - stillNeeded + 1` because there are not enough remaining elements
to complete a combination past that point.

Example: n=5, k=3, path has 1 element → stillNeeded=2 → loop stops at `5-2+1=4`. Choosing 5
at this level would leave only one number (nothing ≥ 6), making it impossible to reach k=3.

**Complexity.** O(k · C(n,k)) time (C(n,k) combinations, each copied in O(k)), O(k) extra
space for the path.

**Usage**

```java
List<List<Integer>> combos = Backtracking.combine(4, 2);
// [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]
```

**Related LeetCode problems.** 77 (Combinations — exact match), 39 (Combination Sum — repeated
use of same element; do not increment `start`), 40 (Combination Sum II — used once, duplicates
in input; sort and skip), 216 (Combination Sum III — exactly k numbers summing to n).

---

## Extending the template to other problems

### Permutations (LC 46)

Instead of a `start` index, pass a `used` boolean array. At each level, try every unused element.

```java
void permHelper(int[] nums, boolean[] used, List<Integer> path, List<List<Integer>> out) {
    if (path.size() == nums.length) { out.add(new ArrayList<>(path)); return; }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;
        used[i] = true;
        path.add(nums[i]);
        permHelper(nums, used, path, out);
        path.remove(path.size() - 1);
        used[i] = false;
    }
}
```

**Complexity.** O(n · n!) time.

### Word search (LC 79)

The path is implicitly the visited grid cells. Un-choosing means marking a cell unvisited.

```java
boolean dfs(char[][] board, String word, int idx, int r, int c) {
    if (idx == word.length()) return true;
    if (out of bounds || board[r][c] != word.charAt(idx)) return false;
    char tmp = board[r][c];
    board[r][c] = '#';                          // CHOOSE (mark visited)
    for (int[] d : DIRS)
        if (dfs(board, word, idx+1, r+d[0], c+d[1])) return true;
    board[r][c] = tmp;                          // UN-CHOOSE
    return false;
}
```

### Combination sum (LC 39)

Same as `subsets` but: recurse with the same index (not `i+1`) to allow reuse, and only emit
when the running sum equals the target.

---

## Common pitfalls

| Pitfall                        | Symptom                                  | Fix                                         |
|--------------------------------|------------------------------------------|---------------------------------------------|
| Not copying path on emit       | All results are identical or empty lists | `out.add(new ArrayList<>(path))`            |
| Forgetting to un-choose        | Incorrect paths; algorithm never backtracks | Always pair `add` with `remove(size-1)`  |
| Wrong start index              | Duplicate subsets or missing elements    | Increment to `i+1` for no-repeat; same `i` for repeat |
| Missing pruning on combine     | Correct but slow                         | Loop to `n - stillNeeded + 1`               |
| Not sorting for duplicate input| Duplicate results in subsets/combinations| Sort first, skip `nums[i]==nums[i-1]`       |

---

## Complexity summary

| Function   | Time          | Space (excl. output) |
|------------|---------------|----------------------|
| `subsets`  | O(n · 2^n)    | O(n)                 |
| `combine`  | O(k · C(n,k)) | O(k)                 |
| Permute    | O(n · n!)     | O(n)                 |

Backtracking is exponential by nature. Pruning reduces the constant factor but not the
asymptotic class. For problems where a polynomial-time alternative exists (e.g. counting
subsets via DP, finding a single combination via greedy), prefer the non-backtracking approach.
Use backtracking when the output is all solutions, not just the best one.
