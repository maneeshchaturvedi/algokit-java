# Dynamic Programming API Reference

Package: `io.algokit.dp`

Dynamic programming templates covering classic problems and a utility for running deep recursion
safely. All methods are static utilities on `DynamicProgramming`.

---

## The four-rung DP ladder

Every DP solution evolves through up to four stages. Understanding all four lets you start with
what the interviewer asked for and optimise on request.

### Rung 1 — Plain recursion

Express the answer as a function `f(subproblem)` and write the recurrence directly. This is
always the clearest statement of the problem.

```java
int minCoins(int[] coins, int amount) {
    if (amount == 0) return 0;
    int best = Integer.MAX_VALUE;
    for (int c : coins)
        if (c <= amount) {
            int sub = minCoins(coins, amount - c);
            if (sub != Integer.MAX_VALUE) best = Math.min(best, 1 + sub);
        }
    return best;
}
```

**Complexity.** Exponential — each call branches into `|coins|` subproblems.

### Rung 2 — Memoisation (top-down DP)

Add a cache (`memo` array or HashMap) so each unique subproblem is solved exactly once.

```java
int minCoins(int[] coins, int amount, int[] memo) {
    if (amount == 0) return 0;
    if (memo[amount] != 0) return memo[amount];
    int best = Integer.MAX_VALUE;
    for (int c : coins)
        if (c <= amount) { ... }
    return memo[amount] = best;
}
```

**Complexity.** O(amount × |coins|) — same as tabulation.

### Rung 3 — Tabulation (bottom-up DP)

Fill a table from the smallest subproblem upward, eliminating recursion entirely. This is what
`coinChange` and `countGridPaths` implement.

### Rung 4 — Space optimisation

Observe which previous table entries the current computation actually needs and discard the rest.
`countGridPaths` already applies this: a 2D grid table reduces to a 1D rolling row.

---

## DP vs Greedy

The greedy approach to coin change — always pick the largest coin that fits — works for standard
denominations (US coins: 25, 10, 5, 1) but fails in general.

**Counterexample: coins = {1, 3, 4}, amount = 6.**

- Greedy: 4 + 1 + 1 = 3 coins.
- Optimal: 3 + 3 = 2 coins.

Greedy works when the "greedy choice property" holds: a locally optimal choice is part of a
globally optimal solution. Coin change violates this when coins are not multiples of each other.
DP works unconditionally because it explores all subproblems.

**Rule of thumb.** Try greedy first (O(n log n) or better). If you can construct a counterexample
where the greedy choice leads to a suboptimal suffix, switch to DP.

---

## coinChange

```java
public static int coinChange(int[] coins, int amount)
// Returns minimum coins to make amount, or -1 if impossible.
```

**Recurrence.**

```
best[0] = 0
best[a] = 1 + min{ best[a - c] : c in coins, c <= a, best[a-c] != MAX }
```

**How the table fills.** `best[a]` is computed only after all `best[a - c]` values are known
(since `a - c < a`, they were computed in earlier iterations of the outer loop). This is the
bottom-up order.

**Sentinel.** `Integer.MAX_VALUE` represents "unreachable." The guard `best[a-c] != MAX_VALUE`
prevents integer overflow when adding 1.

**Complexity.** O(amount × |coins|) time, O(amount) space.

**Usage**

```java
Graphs.coinChange(new int[]{1, 5, 10, 25}, 41); // 4  (25+10+5+1)
Graphs.coinChange(new int[]{2}, 3);              // -1 (impossible)
DynamicProgramming.coinChange(new int[]{1,3,4}, 6); // 2  (3+3)
```

**Related LeetCode problems.** 322 (Coin Change), 518 (Coin Change II — count ways, not min
coins; change `min` to `+=`), 279 (Perfect Squares — coins are perfect squares), 1049 (Last
Stone Weight II — subset-sum variant).

**Variant — count ways (LC 518).** Change `Math.min(best[a], best[a-c] + 1)` to
`best[a] += best[a-c]`. Start with `best[0] = 1` (one way to make zero: use no coins). Be
careful about loop order: the outer loop over amounts and inner over coins counts with
repetition (combinations with replacement), which is what LC 518 asks for.

**Gotcha.** Loop order matters for "count distinct combinations" vs "count distinct sequences":
- Outer = amount, inner = coins → combinations (order doesn't matter, LC 518).
- Outer = coins, inner = amount → permutations (order matters, LC 377).

---

## countGridPaths

```java
public static long countGridPaths(boolean[][] blocked)
// Number of monotone paths from (0,0) to bottom-right, avoiding blocked cells.
```

**Recurrence.**

```
dp[0][0] = 1
dp[r][c] = 0                     if blocked[r][c]
dp[r][c] = dp[r-1][c] + dp[r][c-1]   otherwise
```

Monotone paths move only right or down, so `dp[r][c]` depends only on the cell above and the
cell to the left — both already computed.

**Space optimisation.** We never look back more than one row. The 2D table `dp[r][c]` reduces
to a 1D array where `dp[c]` is reused in-place:

- Before processing row `r`, `dp[c]` holds the value from row `r-1`.
- After processing column `c`, `dp[c]` holds the value for row `r`.
- `dp[c-1]` within the same row has already been updated to row `r` — exactly the left
  neighbour we need.

**Complexity.** O(rows × cols) time, O(cols) space.

**Usage**

```java
boolean[][] grid = {
    {false, false, false},
    {false, true,  false},   // true = blocked
    {false, false, false}
};
DynamicProgramming.countGridPaths(grid); // 2 paths avoid the blocked cell
```

**Return type is `long`.** The number of paths in an m×n grid without obstacles is C(m+n-2, m-1),
which overflows `int` quickly (e.g. 20×20 grid → ~137 billion).

**Related LeetCode problems.** 62 (Unique Paths), 63 (Unique Paths II — blocked cells, identical
recurrence), 64 (Minimum Path Sum — change `+` to `min`), 931 (Minimum Falling Path Sum — same
DP shape, different neighbours).

---

## runWithStack

```java
public static <T> T runWithStack(long stackBytes, Supplier<T> task)
```

**Problem.** The default JVM thread stack is typically 512 KB–1 MB, allowing roughly 5 000–10 000
recursive frames. A recursive DP solution on a large input (e.g. a string of length 50 000)
will throw `StackOverflowError`.

**Solution.** `runWithStack` spawns a new `Thread` with a custom stack size, executes the
`Supplier` on it, and returns the result. The JVM honours the requested stack size as a hint
(results vary by OS and JVM implementation).

**How it handles exceptions.** The task runs in a different thread, so exceptions cannot
propagate normally. The implementation captures `Throwable` in a shared array and re-throws
`RuntimeException` and `Error` directly; checked exceptions are wrapped in
`IllegalStateException`.

**Usage**

```java
int result = DynamicProgramming.runWithStack(
    256L * 1024 * 1024,   // 256 MB stack
    () -> myDeepRecursion(input)
);
```

**Complexity overhead.** Thread creation is O(1) in terms of algorithmic complexity but adds
real latency (microseconds to milliseconds). Do not use in tight loops. This is a one-shot
escape hatch.

**Gotcha.** The stack size hint is a *minimum* request to the JVM; the OS may round up or
impose its own limits. On macOS the limit is typically 8 MB per thread regardless of the hint
for process threads; spawning a new thread with a custom size is one way to exceed the main
thread's limit.

**Alternative.** Convert the recursive algorithm to iterative using an explicit `Deque` (manual
stack). This is always more reliable than a large-stack thread but requires more code.

---

## DP problem taxonomy

| Problem shape                        | Pattern                         | Example                      |
|--------------------------------------|---------------------------------|------------------------------|
| "Minimum/maximum to reach target"    | 1D bottom-up, forward scan      | Coin change, jump game        |
| "Count ways to reach target"         | 1D bottom-up, sum not min       | Coin change II, climb stairs  |
| "Path through a grid"                | 2D table, right/down moves      | Unique paths, min path sum    |
| "Optimal substructure on sequences" | 2D table, string DP             | LCS, edit distance            |
| "Interval / range"                   | 2D table, bottom-up by length   | Matrix chain, burst balloons  |
| "Subset / knapsack"                  | 1D or 2D, capacity dimension    | 0/1 Knapsack, partition equal |

## When to reach for DP

Use DP when all three conditions hold:

1. **Optimal substructure** — an optimal solution to the problem contains optimal solutions to
   subproblems.
2. **Overlapping subproblems** — naïve recursion solves the same subproblem many times.
3. **No greedy proof** — you cannot show a locally optimal choice is globally optimal.

If condition 3 fails (greedy works), DP is still correct but wasteful. If condition 2 fails
(subproblems don't overlap), plain recursion or divide-and-conquer is sufficient.
