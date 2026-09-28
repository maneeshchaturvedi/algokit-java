# Pattern Recognition Guide

The hardest part of a coding interview isn't writing the code — it's recognising which technique to use. Problems rarely name their technique. Instead, they give it away through **signals**: a phrase in the statement, a constraint on the input size, or an operation you'll need to repeat.

This guide teaches you to read those signals. It is organised into five sections:

1. **Signals to Technique** — the master lookup table
2. **Operations to Data Structure** — for design problems
3. **When Nothing Comes to Mind** — a structured recovery routine
4. **Input Size to Complexity** — turning constraints into strategy
5. **Deep Dives** — detailed explanations of each pattern family

---

## 1. Signals to Technique

When you read a problem, look for these phrases and structural cues. Each one points toward a specific technique.

### Hashing and Counting

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "count", "how many times", "frequency" | Hash map counting (`merge(k, 1, Integer::sum)`) | Counting requires visiting each element once and accumulating into buckets. A hash map gives O(1) per bucket. | O(n) |
| "group by", "anagrams", "categorise" | Hash map grouping (`computeIfAbsent` + `add`) | Grouping is counting where the "bucket" is a list. The key is whatever property defines the group — for anagrams, the sorted characters. | O(n * k) where k is key cost |
| "duplicates", "seen before", "first repeat" | Hash set | You need to answer "have I encountered this value already?" A set answers that in O(1). | O(n) |
| "a pair that sums to", "complement" | Hash map from value to index | For each element x, check if `target - x` is already in the map. This turns an O(n^2) nested loop into a single pass. | O(n) |

**When NOT to use hashing:** When you need elements in sorted order, when the key space is small and dense (use an array instead), or when the problem requires range queries (use a TreeMap or prefix sums).

**The one-pass complement pattern** is one of the most versatile:

```java
Map<Integer, Integer> seen = new HashMap<>();
for (int i = 0; i < nums.length; i++) {
    if (seen.containsKey(target - nums[i])) {
        return new int[]{seen.get(target - nums[i]), i};
    }
    seen.put(nums[i], i);
}
```

It works for Two Sum, pairs with a given difference, pairs with a given product (watch for zero), and any problem where you can compute what you *need* from what you *have*.

### Prefix Sums

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "subarray sum equals k" (negatives allowed) | Prefix sum + hash map | A subarray `a[i..j]` sums to k when `prefix[j+1] - prefix[i] = k`. Rearranging: `prefix[i] = prefix[j+1] - k`. So count how many earlier prefixes equal the current prefix minus k. | O(n) |
| "sum between i and j" repeatedly | Prefix sum array | Build once in O(n), then answer any range sum in O(1) by subtraction: `prefix[r+1] - prefix[l]`. | O(n) build, O(1) per query |
| "running total", "cumulative" | Prefix sum array | The prefix sum at position i is the cumulative total up to i. | O(n) |

**Critical detail:** Seed the hash map with `{0: 1}` to handle subarrays that start at index 0. This is the most common bug in prefix sum + hash map solutions.

**Why use `long[]` for prefix sums:** If you have n elements each up to 10^9, the prefix sum can reach 10^18, which overflows `int`. Always use `long[]` for prefix sums.

### Two Pointers

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "sorted array" + "pairs/triples" | Opposite-end pointers | On a sorted array, if the sum is too small, the left pointer must move right (increasing the sum). If too large, the right pointer moves left. Each pointer moves at most n times. | O(n) per pass |
| "in place", "remove", "compact" | Read/write pointers | One pointer reads every element; the other marks where the next kept element goes. The invariant: everything before the write pointer is final output. | O(n) |
| "cycle detection", "duplicate in [1, n]" | Fast/slow (Floyd's) | In a sequence that eventually cycles, a pointer moving at 2x speed will catch a 1x pointer inside the cycle. A second phase from the start finds the cycle entry. | O(n) |
| "palindrome", "from both ends" | Converging pointers | Compare characters from both ends moving inward, skipping non-alphanumeric characters. | O(n) |

**The three two-pointer patterns are fundamentally different:**

- **Opposite-end** requires sorted input. The pointers start at opposite ends and converge.
- **Read/write** works on any input. One pointer scans; the other writes. They both move left to right.
- **Fast/slow** works on linked structures or implicit graphs. They start at the same place; one moves faster.

Don't confuse them. "Two pointers" is not one technique — it's three.

### Sliding Window

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "longest substring/subarray such that..." | Variable window (longest) | Expand right to grow. Shrink left while invalid. Record when valid. The key insight: if shrinking can only make a window "more valid" (monotonicity), each pointer moves at most n times total. | O(n) |
| "shortest substring/subarray such that..." | Variable window (shortest) | Expand right to grow. While valid, record and shrink. The difference from "longest": you record while valid and shrink, instead of recording after shrinking. | O(n) |
| "exactly k distinct", "window of size k" | Fixed window | Maintain a window of exactly size k. Slide it by adding the right element and removing the left. | O(n) |
| "at most k distinct" | Variable window + frequency map | Keep a frequency map of elements in the window. When `map.size() > k`, shrink. The merge-to-null idiom keeps the map clean. | O(n) |

**The sliding window monotonicity requirement** is the most important thing to understand. A sliding window works ONLY when:

- Expanding the window can make it invalid (adding makes it worse)
- Shrinking the window can make it valid again (removing makes it better)

This holds for:

- Sum constraints with non-negative values
- Distinct-count constraints
- Character-frequency constraints

This does NOT hold for:

- Sum constraints with negative values (use prefix sums + hash map instead)
- Problems where removing an element could make the window worse

### Intervals

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "overlapping intervals", "merge" | Sort by start, then sweep | After sorting, overlapping intervals are adjacent. Walk left to right, extending or starting a new merged interval. | O(n log n) |
| "minimum rooms/workers/servers" | Sweep line (+1/−1 events) | Create a +1 event at each start and a −1 event at each end. Sort events by time. Walk through them, maintaining a running count. The peak is your answer. | O(n log n) |
| "insert an interval" | Binary search or linear scan | Find where the new interval overlaps, merge with those, keep the rest. | O(n) |
| "free time", "available slots" | Merge busy intervals, then find gaps | Merge all busy intervals, then the gaps between them are the free slots. | O(n log n) |

**Sweep line deep dive:** The sweep line is more general than merge intervals. It works for:

- Counting concurrent events (meetings, server connections)
- Finding peak load
- Car pooling (weighted events: +passengers at pickup, -passengers at dropoff)
- Calendar booking

The pattern is always the same:

```java
TreeMap<Integer, Integer> events = new TreeMap<>();
for (int[] interval : intervals) {
    events.merge(interval[0], +1, Integer::sum);  // start
    events.merge(interval[1], -1, Integer::sum);  // end
}
int active = 0, peak = 0;
for (int delta : events.values()) {
    active += delta;
    peak = Math.max(peak, active);
}
```

### Stacks

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "matching brackets", "nested", "balanced" | Stack of opening symbols | Push opening symbols. On a closing symbol, check that the top of the stack matches. If the stack is empty at the end and every match succeeded, the input is valid. | O(n) |
| "evaluate expression", "calculator" | Stack of operands and operators | Process tokens left to right. Numbers go on the operand stack. Operators go on the operator stack, but first pop and apply any operator with higher or equal precedence. | O(n) |
| "decode", "nested encoding" | Stack of frames | Push the current state when entering a nested context. Pop and combine when leaving. Each "frame" holds the partial result for that nesting level. | O(output length) |
| "next greater/smaller element" | Monotonic stack | The stack holds indices of elements waiting for their "answer." Each new element resolves all smaller (or larger) elements it can see on the stack. | O(n) |
| "sliding window min/max" | Monotonic deque | A deque where elements are ordered (e.g., increasing for min). Expire old elements from the front. Drop dominated elements from the back. The front is always the answer. | O(n) |
| "undo", "back button" | Stack of operations | Push each operation. Undo = pop and reverse. | O(1) per operation |

**Monotonic stack — the complete mental model:**

A monotonic stack maintains an invariant: values from bottom to top are monotonically non-increasing (for "next greater") or non-decreasing (for "next smaller"). When a new element arrives that would violate the invariant, it pops and resolves every element it dominates.

Why is this O(n)? Each element is pushed once and popped at most once. That's 2n operations total, regardless of how many elements get popped in a single step.

**Variants:**

- Next greater: stack decreasing, pop when `a[stack.peek()] < a[i]`
- Next smaller: stack increasing, pop when `a[stack.peek()] > a[i]`
- Previous greater: iterate right to left with the same logic
- Next greater or equal: use `<=` instead of `<`

### Heaps and Priority Queues

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "k largest/smallest/closest/frequent" | Size-k heap | Keep a heap of size k. For "k largest," use a min-heap: the head is the weakest element kept, so it's easy to check and evict. | O(n log k) |
| "merge k sorted lists/streams" | k-way merge with a heap | Put the head of each list in a min-heap. Poll the smallest, push the next element from that list. The heap always holds exactly one element per list. | O(N log k) |
| "running median", "streaming percentile" | Two heaps (max-heap for lower half, min-heap for upper half) | The max-heap holds the smaller half, the min-heap holds the larger half. Rebalance after each insert so sizes differ by at most 1. The median is at the top of the larger heap (or the average of both tops). | O(log n) per insert |
| "schedule by deadline", "process by priority" | Min-heap of deadlines/priorities | Natural priority queue usage. Poll the most urgent item. | O(log n) per operation |

**Heap vs TreeSet:** Use a heap when you only need the top element and don't need arbitrary removal. Use a TreeSet when you need the top element AND arbitrary removal/update — because `PriorityQueue.remove(Object)` is O(n), but `TreeSet.remove()` is O(log n).

### Binary Search

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "sorted array", "first/last position" | Binary search with a predicate | Instead of searching for a value, search for the boundary between "no" and "yes." The `firstTrue` template handles all variants. | O(log n) |
| "minimise the maximum", "smallest X such that" | Binary search on the answer | If you can write a function `feasible(x)` that returns true when x is large enough, then binary search over the answer space finds the minimum feasible x. | O(n log range) |
| "sorted but rotated" | Modified binary search | At least one half of the array is sorted. Check which half, determine if the target is in the sorted half, and eliminate the other. | O(log n) |

**Binary search on the answer** is the most underappreciated technique. It applies whenever:

1. The answer is a number in a bounded range
2. There's a monotone relationship: if x works, then x+1 also works (or vice versa)
3. You can check "does x work?" efficiently

Examples: minimum shipping capacity, maximum speed of eating bananas, minimum number of days, smallest feasible radius.

### Trees

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "level by level", "depth", "breadth" | BFS with level-counting | Use a queue. Freeze `queue.size()` at the start of each level. Process that many nodes, enqueueing their children. | O(n) |
| "path from root", "depth-first" | DFS (pre/in/post-order) | Recursion naturally follows tree structure. Choose the order based on when you need to process the node relative to its children. | O(n) |
| "diameter", "height", "balanced" | Post-order DFS returning a value | Compute the answer for both subtrees, then combine at the parent. Return a record when you need multiple facts (e.g., height AND balanced). | O(n) |
| "BST", "sorted order", "kth smallest" | In-order traversal | In-order traversal of a BST visits nodes in sorted order. For kth smallest, count nodes during in-order and stop at k. | O(n) or O(h + k) |
| "serialise/deserialise", "reconstruct" | Pre-order + null markers | Pre-order traversal with explicit null markers gives enough information to reconstruct the tree. Use a queue of tokens for deserialisation. | O(n) |
| "hierarchy from flat data" | Build children map, then DFS | Parse parent-child rows into a `Map<id, List<children>>`. Find the root (no parent). DFS from root. | O(n) |
| "nested structure", "flatten" | Recursion or explicit stack | Recursion matches nested structure naturally. Use an explicit stack when depth could be large. | O(n) |

### Graphs

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "shortest path" (unweighted) | BFS | A FIFO queue processes all nodes at distance d before distance d+1, so the first time you reach a node, it's by the shortest path. | O(V + E) |
| "shortest path" (weighted, non-negative) | Dijkstra | Always settle the closest unsettled node. Non-negative weights guarantee that no future path can be shorter than the one being settled. | O((V+E) log V) |
| "connected components", "regions" | BFS, DFS, or Union-Find | BFS/DFS from each unvisited node finds one component. Union-Find merges components as edges arrive. | O(V + E) |
| "dependencies", "prerequisites", "ordering" | Topological sort | Repeatedly take a node with no remaining prerequisites (in-degree 0). If some nodes are never taken, there's a cycle. | O(V + E) |
| "cycle detection" (directed) | Three-color DFS | WHITE = unvisited, GRAY = on current path, BLACK = finished. A GRAY-to-GRAY edge is a back edge, meaning a cycle. Two colours aren't enough because a diamond (two paths to the same node) would look like a cycle. | O(V + E) |
| "grid", "islands", "flood fill" | BFS/DFS treating each cell as a node | Each cell has up to 4 neighbours (DIRS array). Mark visited on enqueue, not dequeue. | O(rows * cols) |
| "merge accounts/records that share X" | Union-Find | Each shared attribute links two records. Union them. At the end, each component is one merged record. | ~O(n) |

### Backtracking

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "all subsets", "power set" | Backtracking: at each element, choose to include or exclude | The recursion tree has 2^n leaves, each corresponding to a subset. | O(2^n) |
| "all permutations" | Backtracking with a "used" set | At each position, try every unused element. | O(n!) |
| "all combinations of size k" | Backtracking with a start index | Start each recursive call at the next index to avoid duplicates. Prune when not enough elements remain. | O(C(n,k)) |
| "generate all valid X" | Backtracking with validity check | At each step, check if the partial solution is still valid. If not, prune. | Depends on pruning |

**The three rules of backtracking:**

1. **Copy the path** when recording a result. The path keeps mutating.
2. **Un-choose** after the recursive call. Restore the state exactly as it was.
3. **Prune** early. If you need k more elements but only k-1 remain, don't recurse.

### Dynamic Programming

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "number of ways" | DP counting | Sum the ways to reach the current state from all predecessor states. | states * transitions |
| "minimum cost", "maximum value" | DP optimisation | Take the min/max over all predecessor states plus the transition cost. | states * transitions |
| "can this string be broken into words?" | DP over positions | `dp[i]` = can the prefix `s[0..i)` be segmented? For each dictionary word that ends at i, check if the prefix before it can be segmented. | O(n * m) |
| "edit distance", "longest common subsequence" | 2D DP over two strings | `dp[i][j]` represents the answer for the first i characters of one string and first j of the other. | O(m * n) |
| "knapsack", "subset sum" | DP over items and capacity | `dp[i][w]` = best value using the first i items with capacity w. | O(n * W) |

**The four-rung DP ladder** (use this to develop any DP solution):

1. **Plain recursion** — write the recurrence. Verify it's correct on small inputs.
2. **Memoisation** — add a cache. Now each subproblem is computed once.
3. **Tabulation** — fill the table bottom-up. No recursion, no stack overflow risk.
4. **Space optimisation** — if each row only depends on the previous row, keep only two rows (or one).

**When to use greedy instead of DP:** If you can prove that a locally optimal choice is globally optimal, use greedy. Common proof techniques: exchange argument (swapping any other choice for the greedy choice doesn't improve the result) or staying-ahead (the greedy solution is at least as good at every step). When in doubt, look for a counterexample. Coins {1, 3, 4} with target 6: greedy picks 4+1+1=3 coins, but 3+3=2 coins is optimal.

---

## 2. Operations to Data Structure

For design problems, start by listing the operations the solution must support. Then pick the structure that makes the most frequent operation cheap.

| You must support... | Structure | Cost | Why not the alternative? |
|---------------------|-----------|------|------------------------|
| Find or update by id | `HashMap<Id, State>` | O(1) | TreeMap is O(log n) — only worth it if you also need ordering |
| The "best" item by a changing score, with removals | `TreeSet<Record>` where the comparator ends with the id | O(log n) per add/remove | PriorityQueue: `remove(Object)` is O(n) |
| The "best" item, append-only or rarely updated | `PriorityQueue` + lazy invalidation | O(log n) | Simpler than TreeSet when you don't need arbitrary removal |
| A FIFO queue; requeue at front | `ArrayDeque` (offerLast / offerFirst) | O(1) | LinkedList has worse cache performance |
| Most recent N events; sliding time window | `ArrayDeque` of timestamps, evict from head | O(1) amortised | ArrayList: removing from front is O(n) |
| "Value as of time t"; time-range queries | `TreeMap<Long, V>` (floorEntry, subMap) | O(log n) | HashMap can't answer "latest before t" |
| Iteration in insertion order | `LinkedHashMap` / `LinkedHashSet` | O(1) | HashMap iteration order is undefined |
| Count of keys per value (O(1) COUNT) | Second `HashMap<Value, Integer>` kept in sync | O(1) | Single map requires O(n) scan to count |
| Prefix matches, autocomplete | Trie, or `TreeMap.subMap` | O(L) per prefix | HashMap requires checking all keys |
| Groups that merge over time | Union-Find | ~O(1) amortised | Re-running BFS after each merge is O(V+E) |
| Dual-index: by id AND by score | `HashMap<Id, Record>` + `TreeSet<Record>` | O(log n) per update | Single structure can't be O(1) for id lookup AND O(log n) for best-by-score |

**The dual-index pattern** deserves special attention. Many practical problems require both fast lookup by ID and fast access to the "best" element. The solution is always two structures kept in sync:

```java
Map<String, Record> byId = new HashMap<>();
TreeSet<Record> ranked = new TreeSet<>(COMPARATOR);

void update(String id, int newScore) {
    Record old = byId.get(id);
    if (old != null) ranked.remove(old);  // remove from TreeSet FIRST
    Record updated = new Record(id, newScore);
    byId.put(id, updated);
    ranked.add(updated);
}
```

**Critical rule:** Never mutate a field that a comparator reads while the object is inside a TreeSet or PriorityQueue. Always remove, change, re-insert.

---

## 3. When Nothing Comes to Mind

Being stuck is normal. What matters is that you keep moving and keep talking. This routine takes about 60 seconds:

### Step 1: State the brute force

Say it out loud with its complexity. "The brute force is to check every pair, which is O(n^2)." This is almost always correct, and it shows the interviewer your starting point. More importantly, the brute force's wasted work often reveals the optimisation.

### Step 2: Ask what work is repeated

- A repeated lookup → hash map
- A repeated "find the minimum" → heap
- A repeated range sum → prefix sums
- A repeated "largest at or below x" → TreeMap or binary search
- The same subproblem computed many times → memoisation / DP

### Step 3: Ask whether sorting would help

Intervals, pairs, greedy choices, and binary search all become easier on sorted data. If the problem gives unsorted input and you're not sure what to do, try sorting by the most natural key and see if a pattern emerges.

### Step 4: Ask whether it's a graph in disguise

Grids, dependencies, "connected", "reachable", and "fewest steps" are all graph problems. If entities influence each other or you need to explore states, think graph.

### Step 5: Name the state and transition

If a decision depends on the same smaller subproblem many times, it's DP. Can you define `dp[i]` or `dp[i][j]` that captures everything you need to make the next decision?

### Step 6: If still stuck, say so

Name the two options you're weighing and your concern with each. "I'm torn between a sliding window and prefix sums — the window feels right for the 'contiguous' constraint, but I'm worried about negative values breaking the monotonicity." Interviewers give hints to candidates who show precisely where they're stuck.

---

## 4. Input Size to Complexity Target

In Java, assume roughly 10^8 simple operations per second. Divide that budget by the input size to get the complexity you can afford:

| n up to | Acceptable complexity | Typical techniques |
|---------|----------------------|-------------------|
| 12 | O(n!) | Permutations, brute-force enumeration |
| 20–25 | O(2^n) | Subsets, bitmask DP |
| 500 | O(n^3) | Floyd-Warshall, triple nested loops |
| 5,000 | O(n^2) | All pairs, 2D DP, simple simulation |
| 100,000 | O(n log n) | Sorting, heap operations, TreeMap, binary search |
| 1,000,000 | O(n) | Hashing, two pointers, sliding window, one pass |
| More than 10^7 | O(n) or O(log n) | Mathematical formula, binary search on answer |

**For design problems** where no explicit n is given, ask for expected scale. If the answer is "large," aim for O(n log n) or better per batch operation and O(log n) or better per individual operation.

**The complexity target narrows your options dramatically.** If n is 10^5, you can't use O(n^2). That eliminates brute-force all-pairs and tells you to look for sorting, hashing, or a heap. Let the constraint guide your technique selection.

---

## 5. Pattern Families — Quick Reference

### One-pass patterns (O(n))
Hash map counting, complement lookup, prefix sums, two pointers, sliding window, monotonic stack

### Sort-then-process patterns (O(n log n))
Interval merging, sweep line, greedy scheduling, binary search on sorted data

### Heap patterns (O(n log k) or O(n log n))
Top-k, k-way merge, two-heap median, schedule by priority

### Graph patterns (O(V + E))
BFS shortest path, DFS traversal, topological sort, union-find

### Search patterns (O(log n) to O(n log range))
Binary search on arrays, binary search on the answer, TreeMap navigation

### Exhaustive search (exponential)
Backtracking (subsets, permutations, combinations), bitmask DP

### Optimisation patterns (states * transitions)
Dynamic programming (1D, 2D, knapsack, interval DP)
