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

Hashing is the first tool to reach for when a problem asks you to remember things you have seen. The underlying idea is simple: instead of scanning the whole collection every time you need to answer "have I seen this before?" or "how many times has this appeared?", you store answers in a hash map or hash set as you go, and look them up in O(1) later.

**Counting and grouping** are two faces of the same idea. When you see words like "count," "how many times," or "frequency," your instinct should be a hash map that maps each value to how many times it appears. The idiomatic Java pattern is `map.merge(key, 1, Integer::sum)` — it handles both the first occurrence (initialising the count to 1) and subsequent ones (incrementing). When the problem says "group by," "anagram," or "categorise," you need a map of lists instead of a map of integers. The key is whatever property defines the group: for anagrams, that's the sorted characters; for grouping by remainder, it's the modulo result. Use `computeIfAbsent` to create the list on first use: `map.computeIfAbsent(key, k -> new ArrayList<>()).add(value)`.

**Membership testing** — "have I seen this value before?", "first repeat," "duplicates exist" — calls for a hash set. A set gives you `contains` in O(1), and that's all you need to turn an O(n^2) nested scan into a single pass.

**The complement pattern** is the most versatile hashing trick, and it powers Two Sum as well as dozens of variations. For each element x you encounter, ask: "what value would I need to have already seen to satisfy the constraint?" That needed value is the complement. Check if it's in the map; if not, record x and move on. The result is a single pass.

```java
Map<Integer, Integer> seen = new HashMap<>();
for (int i = 0; i < nums.length; i++) {
    if (seen.containsKey(target - nums[i])) {
        return new int[]{seen.get(target - nums[i]), i};
    }
    seen.put(nums[i], i);
}
```

This generalises beyond sums. Any time you can express "what I need" as a function of "what I have," the complement pattern applies — pairs with a given difference, pairs with a given product (watch for zero as a special case), and similar problems all follow the same shape.

**When NOT to use hashing:** If you need elements in sorted order, a hash map won't help — use a TreeMap or sort the data. If the key space is small and dense (say, integers 0–255), a plain array indexed by value will be faster and simpler than a hash map. If you need range queries (sum from i to j, or largest value in a window), hashing alone doesn't help — combine it with prefix sums, or reach for a TreeMap.

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "count", "how many times", "frequency" | Hash map counting (`merge(k, 1, Integer::sum)`) | Counting requires visiting each element once and accumulating into buckets. A hash map gives O(1) per bucket. | O(n) |
| "group by", "anagrams", "categorise" | Hash map grouping (`computeIfAbsent` + `add`) | Grouping is counting where the "bucket" is a list. The key is whatever property defines the group — for anagrams, the sorted characters. | O(n * k) where k is key cost |
| "duplicates", "seen before", "first repeat" | Hash set | You need to answer "have I encountered this value already?" A set answers that in O(1). | O(n) |
| "a pair that sums to", "complement" | Hash map from value to index | For each element x, check if `target - x` is already in the map. This turns an O(n^2) nested loop into a single pass. | O(n) |

---

### Prefix Sums

A prefix sum array answers "what is the sum of elements from index i to index j?" in O(1) time, after a one-time O(n) build. The key identity is that any subarray sum can be expressed as the difference of two prefix sums: `sum(i..j) = prefix[j+1] - prefix[i]`. Build it once; answer any range query instantly.

The more sophisticated application combines prefix sums with a hash map to handle the problem "find the number of subarrays that sum to exactly k." Here's the reasoning: a subarray from index i to j sums to k when `prefix[j+1] - prefix[i] = k`, which rearranges to `prefix[i] = prefix[j+1] - k`. So at each step, after computing the current prefix sum, you ask: "how many earlier prefix sums equal (current prefix sum - k)?" A hash map from prefix sum value to count answers this in O(1). This works even when elements are negative, which is why it's superior to a sliding window for this class of problem.

**The seed value is critical and easy to forget.** Initialise the hash map with `{0: 1}` before you start. This accounts for the case where the subarray starts at index 0 — without the seed, subarrays beginning from the start of the array would be missed.

**Use `long[]` for prefix sums whenever values can be large.** If you have n elements each up to 10^9, the prefix sum can reach n * 10^9, which overflows a 32-bit `int`. The safe default is to declare `long[] prefix`.

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "subarray sum equals k" (negatives allowed) | Prefix sum + hash map | A subarray `a[i..j]` sums to k when `prefix[j+1] - prefix[i] = k`. Rearranging: `prefix[i] = prefix[j+1] - k`. So count how many earlier prefixes equal the current prefix minus k. | O(n) |
| "sum between i and j" repeatedly | Prefix sum array | Build once in O(n), then answer any range sum in O(1) by subtraction: `prefix[r+1] - prefix[l]`. | O(n) build, O(1) per query |
| "running total", "cumulative" | Prefix sum array | The prefix sum at position i is the cumulative total up to i. | O(n) |

---

### Two Pointers

"Two pointers" is actually three distinct techniques that share the name. Conflating them is one of the most common sources of confusion. Understanding what distinguishes them — and which situation calls for which — is what makes the pattern useful.

**Opposite-end pointers** require sorted input. You start one pointer at the leftmost element and one at the rightmost, and they move toward each other. The reasoning: on a sorted array, if the sum of the two pointed elements is too small, the only way to increase it is to move the left pointer right. If the sum is too large, the only way to decrease it is to move the right pointer left. Because each pointer moves at most n times total, you get O(n). This technique naturally extends to 3Sum (fix one element, apply two pointers on the rest) and similar problems. It will not work on unsorted input — the monotonicity of the argument breaks down.

**Read/write pointers** work on any array and are the canonical tool for in-place compaction. One pointer reads every element; the other marks where the next kept element belongs. The invariant you maintain: everything to the left of the write pointer is final output. When you decide to keep the element at the read pointer, copy it to the write position and advance both. When you decide to discard it, advance only the read pointer. At the end, the write pointer is the new length.

**Fast/slow pointers** (Floyd's algorithm) work on sequences with a potential cycle — linked lists, or implicit sequences defined by a function like `f(x) = nums[x]`. The intuition: if a cycle exists, a pointer moving two steps at a time will eventually lap a pointer moving one step at a time and land in the same position. The meeting point does not reveal the cycle entry directly, but a second phase — resetting one pointer to the start and moving both one step at a time — finds the cycle entry precisely. This is the technique for "detect a cycle in a linked list" and "find the duplicate in an array where values are in [1, n]."

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "sorted array" + "pairs/triples" | Opposite-end pointers | On a sorted array, if the sum is too small, the left pointer must move right (increasing the sum). If too large, the right pointer moves left. Each pointer moves at most n times. | O(n) per pass |
| "in place", "remove", "compact" | Read/write pointers | One pointer reads every element; the other marks where the next kept element goes. The invariant: everything before the write pointer is final output. | O(n) |
| "cycle detection", "duplicate in [1, n]" | Fast/slow (Floyd's) | In a sequence that eventually cycles, a pointer moving at 2x speed will catch a 1x pointer inside the cycle. A second phase from the start finds the cycle entry. | O(n) |
| "palindrome", "from both ends" | Converging pointers | Compare characters from both ends moving inward, skipping non-alphanumeric characters. | O(n) |

---

### Sliding Window

A sliding window maintains a contiguous subarray (or substring) as you scan from left to right, expanding the right boundary to include more elements and shrinking the left boundary to satisfy a constraint. The reason it achieves O(n) rather than O(n^2) is that each pointer moves strictly left to right — neither one ever backtracks — so the total number of operations is bounded by 2n.

**The monotonicity requirement** is the most important thing to understand, and it's the reason sliding window sometimes fails where prefix sums succeed. A sliding window only works when:

- Adding an element to the window can make it invalid (it can get "worse").
- Removing an element from the window can make it valid again (it can get "better").

This property holds for sum constraints with non-negative values (adding a positive number only increases the sum), for distinct-count constraints (adding a new element can exceed the limit; removing an element reduces the count), and for character-frequency constraints. It does NOT hold when values can be negative — removing a negative number from the window would increase the sum, meaning shrinking doesn't necessarily make an invalid window valid. For that case, use prefix sums with a hash map.

**Longest vs shortest windows** require subtly different logic. For "longest subarray such that...": expand the right boundary by default; shrink the left boundary while the window is invalid; record the window size once the window is valid after shrinking. For "shortest subarray such that...": expand the right boundary by default; while the window is valid, record the window size and then shrink the left boundary. The difference is when you record: after restoring validity (for longest) vs before losing it (for shortest).

**Fixed-size windows** are a special case: slide by adding the incoming right element and removing the outgoing left element, keeping size constant.

**The at-most-k distinct pattern** deserves mention because it appears frequently. Maintain a frequency map of elements currently in the window. When `map.size() > k`, shrink from the left. When a frequency drops to zero, remove the key — if you don't, the map will accumulate zero-count entries and `map.size()` will give the wrong answer. Use the merge-to-null idiom: `map.merge(key, -1, Integer::sum); if (map.get(key) == 0) map.remove(key);`.

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "longest substring/subarray such that..." | Variable window (longest) | Expand right to grow. Shrink left while invalid. Record when valid. The key insight: if shrinking can only make a window "more valid" (monotonicity), each pointer moves at most n times total. | O(n) |
| "shortest substring/subarray such that..." | Variable window (shortest) | Expand right to grow. While valid, record and shrink. The difference from "longest": you record while valid and shrink, instead of recording after shrinking. | O(n) |
| "exactly k distinct", "window of size k" | Fixed window | Maintain a window of exactly size k. Slide it by adding the right element and removing the left. | O(n) |
| "at most k distinct" | Variable window + frequency map | Keep a frequency map of elements in the window. When `map.size() > k`, shrink. The merge-to-null idiom keeps the map clean. | O(n) |

---

### Intervals

Interval problems share a common structure: you're given a set of ranges and asked to either combine overlapping ones, count concurrent ones, or find gaps between them. The key observation that unlocks most of these problems is that sorting the intervals by start time makes overlapping intervals adjacent to one another.

**Merging overlapping intervals** is straightforward once sorted. Walk left to right. Maintain the "current" merged interval. If the next interval starts at or before the end of the current one, they overlap — extend the current interval's end if needed. If the next interval starts after the current one ends, they don't overlap — emit the current merged interval and start a new one. Two edge cases worth noting: what if the end of the next interval is before the end of the current one (it's nested inside)? The `Math.max` handles this. What if no intervals overlap? Each becomes its own merged interval.

**Sweep line** is more general than merge. It answers questions like "what is the maximum number of concurrent meetings?" and "what is the minimum number of servers needed at peak load?" Instead of thinking about intervals as ranges, think about them as events: a +1 event at the start time and a −1 event at the end time. Sort all events by time, then walk through them maintaining a running count. The maximum value the counter reaches is the peak concurrency. A `TreeMap` handles events at the same timestamp correctly (it preserves order), and storing them in a map rather than a list naturally merges events at the same time.

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

The sweep line generalises to weighted events — car pooling (add passengers at pickup, subtract at dropoff) and calendar booking work the same way.

**When to use sweep line vs merge:** If you're asked for a peak count or concurrent load, use sweep line. If you're asked to produce a merged list of non-overlapping intervals, use the merge approach. If you need to find free time slots, merge first, then look for gaps between the merged intervals.

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "overlapping intervals", "merge" | Sort by start, then sweep | After sorting, overlapping intervals are adjacent. Walk left to right, extending or starting a new merged interval. | O(n log n) |
| "minimum rooms/workers/servers" | Sweep line (+1/−1 events) | Create a +1 event at each start and a −1 event at each end. Sort events by time. Walk through them, maintaining a running count. The peak is your answer. | O(n log n) |
| "insert an interval" | Binary search or linear scan | Find where the new interval overlaps, merge with those, keep the rest. | O(n) |
| "free time", "available slots" | Merge busy intervals, then find gaps | Merge all busy intervals, then the gaps between them are the free slots. | O(n log n) |

---

### Stacks

A stack is the right data structure when the validity or value of an element depends on elements that came immediately before it — and specifically when you might need to go back and revisit them in reverse order (last in, first out).

**Matching and nesting** problems are the clearest use case. "Valid parentheses," "matching brackets," and any problem with nested structure (like decode string) follow the same pattern: push when you enter a context, pop when you leave it, and validate or combine the result. For bracket matching, push opening symbols; on each closing symbol, check that the top of the stack is the matching opener. If the stack is empty at the end and no match ever failed, the input is valid. For nested encoding problems like "3[a2[bc]]", each level of nesting is a "frame" on the stack holding a count and a partial string.

**Expression evaluation** extends the nesting idea. Numbers go on one stack (or into the frame), operators go on another. When you encounter a new operator, first pop and apply any operator already on the stack that has higher or equal precedence — this is what implements left-to-right evaluation and precedence rules.

**Monotonic stacks** solve "next greater element," "next smaller element," "largest rectangle in histogram," and related problems in O(n). The invariant is that elements in the stack (indexed by position) are in monotonically non-increasing order (for next greater) or non-decreasing order (for next smaller). When a new element arrives that would break the invariant, pop elements until the invariant holds again — and each element you pop has just found its "answer" (the current element is its next greater element). Why is this O(n)? Each element is pushed once and popped at most once, giving 2n operations total regardless of how much popping happens at any individual step.

The four monotonic stack variants follow the same structure but differ in direction and comparison:

- **Next greater:** stack holds decreasing values; pop when `a[stack.peek()] < a[i]`
- **Next smaller:** stack holds increasing values; pop when `a[stack.peek()] > a[i]`
- **Previous greater:** iterate right to left with the same logic as next greater
- **Next greater or equal:** use `<=` instead of `<`

**Monotonic deques** are the sliding window version of a monotonic stack. A deque where elements are ordered (increasing for minimum, decreasing for maximum) supports window min/max in O(1) amortised. Expire old elements (outside the window) from the front; drop dominated elements from the back before inserting the new element. The front of the deque is always the answer for the current window.

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "matching brackets", "nested", "balanced" | Stack of opening symbols | Push opening symbols. On a closing symbol, check that the top of the stack matches. If the stack is empty at the end and every match succeeded, the input is valid. | O(n) |
| "evaluate expression", "calculator" | Stack of operands and operators | Process tokens left to right. Numbers go on the operand stack. Operators go on the operator stack, but first pop and apply any operator with higher or equal precedence. | O(n) |
| "decode", "nested encoding" | Stack of frames | Push the current state when entering a nested context. Pop and combine when leaving. Each "frame" holds the partial result for that nesting level. | O(output length) |
| "next greater/smaller element" | Monotonic stack | The stack holds indices of elements waiting for their "answer." Each new element resolves all smaller (or larger) elements it can see on the stack. | O(n) |
| "sliding window min/max" | Monotonic deque | A deque where elements are ordered (e.g., increasing for min). Expire old elements from the front. Drop dominated elements from the back. The front is always the answer. | O(n) |
| "undo", "back button" | Stack of operations | Push each operation. Undo = pop and reverse. | O(1) per operation |

---

### Heaps and Priority Queues

A heap is the right tool when you need repeated access to the "best" element (minimum or maximum) in a changing collection, and when "best" means something you can define with a comparator. The core operations — insert and poll-min — both run in O(log n).

**Top-k problems** ("k largest elements," "k most frequent," "k closest points") follow a single template. Counterintuitively, to find the k largest elements, use a min-heap. The reasoning: keep the heap at size k, with the smallest of the k selected elements at the top. As you process each new element, check whether it is larger than the heap's minimum (i.e., the weakest element currently selected). If it is, evict the minimum and insert the new element. If it isn't, discard it. At the end, the heap contains exactly the k largest. This runs in O(n log k), which is better than sorting (O(n log n)) when k is much smaller than n.

**K-way merge** applies when you have k sorted sequences and need to merge them into one. Put the first element of each sequence into a min-heap, tagged with which sequence it came from. Repeatedly poll the minimum, emit it, and push the next element from that sequence into the heap. The heap always holds exactly k elements (one per sequence), so each insert and poll is O(log k). Total cost is O(N log k) where N is the total number of elements.

**Running median** is the classic two-heap problem. Maintain two heaps: a max-heap holding the smaller half of the values seen so far, and a min-heap holding the larger half. After each insertion, rebalance so that the sizes differ by at most one. The median is the top of the larger heap (if sizes differ by one) or the average of both tops (if sizes are equal). Each insertion requires O(log n) work. This generalises to streaming percentiles.

**Heap vs TreeSet** is an important distinction for design problems. Use a `PriorityQueue` when you only need to access the top element and you never need to remove an arbitrary element. Use a `TreeSet` when you also need arbitrary removal or update — because `PriorityQueue.remove(Object)` scans the entire heap in O(n), while `TreeSet.remove()` runs in O(log n).

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "k largest/smallest/closest/frequent" | Size-k heap | Keep a heap of size k. For "k largest," use a min-heap: the head is the weakest element kept, so it's easy to check and evict. | O(n log k) |
| "merge k sorted lists/streams" | k-way merge with a heap | Put the head of each list in a min-heap. Poll the smallest, push the next element from that list. The heap always holds exactly one element per list. | O(N log k) |
| "running median", "streaming percentile" | Two heaps (max-heap for lower half, min-heap for upper half) | The max-heap holds the smaller half, the min-heap holds the larger half. Rebalance after each insert so sizes differ by at most 1. The median is at the top of the larger heap (or the average of both tops). | O(log n) per insert |
| "schedule by deadline", "process by priority" | Min-heap of deadlines/priorities | Natural priority queue usage. Poll the most urgent item. | O(log n) per operation |

---

### Binary Search

Binary search is most famously used to find a value in a sorted array, but the deeper and more powerful application is binary search on the answer. Both forms rest on the same idea: when you can partition a search space into a "no" region and a "yes" region with a clear boundary, you can find that boundary in O(log n) rather than scanning linearly.

**Finding a position in a sorted array** is the classic form. Rather than searching for a specific value, think of it as searching for the boundary between "false" and "true" for some predicate. A `firstTrue` template handles all variants — first occurrence, last occurrence, first element greater than x — without requiring you to reconstruct the logic from scratch each time. For a rotated sorted array, the key observation is that at least one half is always sorted. Determine which half is sorted, check whether your target lies in that half, and eliminate the other half entirely.

**Binary search on the answer** is the technique that surprises many candidates when they first encounter it. The key insight: many minimisation and maximisation problems that seem to require searching over complex state actually reduce to this: "Is it feasible to achieve result x?" If the feasibility function is monotone — meaning if x is feasible then x+1 is also feasible (or vice versa) — then binary search over the answer space finds the minimum (or maximum) feasible value in O(log range) calls to the feasibility function.

The conditions that make binary search on the answer applicable:

1. The answer is a number in a bounded range (you can state minimum and maximum possible values).
2. There is a monotone relationship: if x works, then x+1 also works (for a minimum problem).
3. You can check "does x work?" in better than O(range) time — usually O(n).

Problems where this applies include: minimum shipping capacity to deliver packages, maximum eating speed for a given time budget, minimum number of days to make a bouquet, and smallest feasible radius for a coverage problem. In each case, the feasibility check is an O(n) scan, making the total complexity O(n log range).

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "sorted array", "first/last position" | Binary search with a predicate | Instead of searching for a value, search for the boundary between "no" and "yes." The `firstTrue` template handles all variants. | O(log n) |
| "minimise the maximum", "smallest X such that" | Binary search on the answer | If you can write a function `feasible(x)` that returns true when x is large enough, then binary search over the answer space finds the minimum feasible x. | O(n log range) |
| "sorted but rotated" | Modified binary search | At least one half of the array is sorted. Check which half, determine if the target is in the sorted half, and eliminate the other. | O(log n) |

---

### Trees

Tree problems almost always reduce to choosing the right traversal and deciding what information to carry up or down the recursion. The three standard orderings — pre-order, in-order, post-order — exist because different problems need the parent processed at different times relative to its children.

**Level-order traversal (BFS)** is the right choice when the problem talks about layers, depth, or width. Use a queue. At the start of each level, capture the current queue size with `int size = queue.size()` — this tells you exactly how many nodes belong to the current level. Process that many nodes, enqueue their children, then move to the next level.

**DFS traversals** work naturally through recursion, since the call stack mirrors the tree's structure. Pre-order (process node, then recurse left, then right) is used for tree serialisation and path problems. In-order (recurse left, process node, recurse right) visits a binary search tree in sorted order, which is how you solve "kth smallest element in a BST" — count nodes during in-order traversal and stop at k. Post-order (recurse left, recurse right, process node) is used whenever you need information from both subtrees before you can answer for the current node.

**Post-order DFS returning a value** is the pattern for problems like "diameter of binary tree," "height," and "is this tree balanced?" The reasoning: you can't compute the diameter at a node until you know the height of both subtrees. Post-order recursion naturally gives you both children's results before you process the current node. When you need to return multiple facts from a recursive call (e.g., both height and a boolean for balance), return a small record or pair rather than trying to thread state through global variables.

**Reconstruction and serialisation** use pre-order with explicit null markers. Pre-order visits root before subtrees, so the root always appears first in a serialised representation — making reconstruction straightforward. Use a queue of tokens when deserialising.

**Building a tree from flat parent-child data** follows a fixed recipe: parse the data into a `Map<id, List<children>>`, find the root (the node that appears as no one's child), and DFS from the root.

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "level by level", "depth", "breadth" | BFS with level-counting | Use a queue. Freeze `queue.size()` at the start of each level. Process that many nodes, enqueueing their children. | O(n) |
| "path from root", "depth-first" | DFS (pre/in/post-order) | Recursion naturally follows tree structure. Choose the order based on when you need to process the node relative to its children. | O(n) |
| "diameter", "height", "balanced" | Post-order DFS returning a value | Compute the answer for both subtrees, then combine at the parent. Return a record when you need multiple facts (e.g., height AND balanced). | O(n) |
| "BST", "sorted order", "kth smallest" | In-order traversal | In-order traversal of a BST visits nodes in sorted order. For kth smallest, count nodes during in-order and stop at k. | O(n) or O(h + k) |
| "serialise/deserialise", "reconstruct" | Pre-order + null markers | Pre-order traversal with explicit null markers gives enough information to reconstruct the tree. Use a queue of tokens for deserialisation. | O(n) |
| "hierarchy from flat data" | Build children map, then DFS | Parse parent-child rows into a `Map<id, List<children>>`. Find the root (no parent). DFS from root. | O(n) |
| "nested structure", "flatten" | Recursion or explicit stack | Recursion matches nested structure naturally. Use an explicit stack when depth could be large. | O(n) |

---

### Graphs

Graph problems are often disguised as something else. If entities can influence or connect to each other, if you're looking for the "fewest steps" between two states, or if you're asked whether a configuration is reachable, you are almost certainly in graph territory — even if the word "graph" never appears.

**BFS for shortest paths** works on unweighted graphs (or graphs where all edges have the same cost) because a first-in-first-out queue naturally processes nodes in order of their distance from the source. The first time you reach any node, you have reached it by the shortest path — so once a node is visited, you don't need to revisit it. For grids, each cell is a node and the at-most-four cardinal neighbours are its edges. A critical implementation note: mark cells visited when you enqueue them, not when you dequeue them. If you mark on dequeue, you may enqueue the same cell multiple times before processing it.

**Dijkstra** extends BFS to weighted graphs where edge costs are non-negative. It processes nodes in order of their tentative distance from the source using a min-heap. The correctness argument: when a node is popped from the heap, its tentative distance is final, because all unprocessed paths go through nodes with equal or greater distance. With negative weights, this argument fails — use Bellman-Ford instead.

**Connected components** can be found by BFS or DFS: start from each unvisited node, mark everything reachable, and count how many times you had to start fresh. Union-Find is an alternative that processes edges incrementally — useful when edges arrive one at a time (streaming) or when you need to merge groups as you go.

**Topological sort** answers "in what order can I process these tasks, given that some must come before others?" The algorithm repeatedly picks any node with no remaining prerequisites (in-degree 0), removes it, and decrements the in-degree of its dependents. If you finish with nodes still unprocessed, there is a cycle — the cycle involves the nodes that never reached in-degree 0.

**Cycle detection in directed graphs** requires three-color DFS: white (unvisited), gray (on the current recursion path), black (fully processed). A back edge — an edge to a gray node — indicates a cycle. Two colors are insufficient because a diamond (two separate paths leading to the same node) would be misidentified as a cycle.

**Union-Find** shines for "merge accounts," "number of components," and problems where you process membership relations in bulk. Its amortised per-operation cost is nearly O(1) with path compression and union by rank.

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "shortest path" (unweighted) | BFS | A FIFO queue processes all nodes at distance d before distance d+1, so the first time you reach a node, it's by the shortest path. | O(V + E) |
| "shortest path" (weighted, non-negative) | Dijkstra | Always settle the closest unsettled node. Non-negative weights guarantee that no future path can be shorter than the one being settled. | O((V+E) log V) |
| "connected components", "regions" | BFS, DFS, or Union-Find | BFS/DFS from each unvisited node finds one component. Union-Find merges components as edges arrive. | O(V + E) |
| "dependencies", "prerequisites", "ordering" | Topological sort | Repeatedly take a node with no remaining prerequisites (in-degree 0). If some nodes are never taken, there's a cycle. | O(V + E) |
| "cycle detection" (directed) | Three-color DFS | WHITE = unvisited, GRAY = on current path, BLACK = finished. A GRAY-to-GRAY edge is a back edge, meaning a cycle. Two colours aren't enough because a diamond (two paths to the same node) would look like a cycle. | O(V + E) |
| "grid", "islands", "flood fill" | BFS/DFS treating each cell as a node | Each cell has up to 4 neighbours (DIRS array). Mark visited on enqueue, not dequeue. | O(rows * cols) |
| "merge accounts/records that share X" | Union-Find | Each shared attribute links two records. Union them. At the end, each component is one merged record. | ~O(n) |

---

### Backtracking

Backtracking is for problems that ask you to enumerate or find all valid configurations: all subsets, all permutations, all combinations, all valid arrangements satisfying some constraint. The technique is a depth-first search over a decision tree, where at each node you make a choice, recurse to explore its consequences, and then undo the choice to explore alternatives.

The core recursive structure is always the same: make a choice, recurse, unmake the choice. The "unmake" step is what makes backtracking backtracking rather than plain DFS — you restore state so that sibling branches see a clean slate. If you forget to undo, the state at each level is corrupted by choices made in earlier branches.

**The three mandatory rules:**

First, when you record a result, copy the path — don't add the live path object to your results list. The path is a single mutable object that gets modified throughout the recursion. If you add the reference, all result entries will end up pointing to the same (now-empty) collection. Use `new ArrayList<>(path)`.

Second, always undo what you did. If you added to a list, remove the last element. If you set a flag, clear it. If you modified a count, restore it. The state before recursing and the state after must be identical.

Third, prune aggressively. If you need k more elements to complete a valid solution but only m < k remain, don't recurse. Every pruning check eliminates an entire subtree from the search.

**Subsets** visit a binary decision at each element: include it or exclude it. The recursion tree has 2^n leaves. **Permutations** maintain a "used" set and try every unused element at each position — the tree has n! leaves. **Combinations of size k** use a start index to avoid revisiting earlier elements, and prune when fewer than k elements remain.

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "all subsets", "power set" | Backtracking: at each element, choose to include or exclude | The recursion tree has 2^n leaves, each corresponding to a subset. | O(2^n) |
| "all permutations" | Backtracking with a "used" set | At each position, try every unused element. | O(n!) |
| "all combinations of size k" | Backtracking with a start index | Start each recursive call at the next index to avoid duplicates. Prune when not enough elements remain. | O(C(n,k)) |
| "generate all valid X" | Backtracking with validity check | At each step, check if the partial solution is still valid. If not, prune. | Depends on pruning |

---

### Dynamic Programming

Dynamic programming applies when a problem has optimal substructure (the optimal solution to the whole can be built from optimal solutions to subproblems) and overlapping subproblems (the same subproblem is needed many times). Recognising DP often means noticing that a recursive brute force would recompute the same function call with the same arguments over and over.

**Counting problems** ("number of ways to...") use DP by summing the number of ways to reach the current state from all valid predecessor states. The key is correctly identifying what "state" means — what information do you need to know to determine valid next states? For climbing stairs, the state is the current step. For string segmentation, the state is the current position.

**Optimisation problems** ("minimum cost," "maximum value") use DP by taking the min or max over all valid predecessor states plus the cost of the transition. For the knapsack problem, the state is (item index, remaining capacity), and you take the max of "skip this item" and "take this item" (if capacity allows).

**2D DP over two strings** — edit distance, longest common subsequence, regular expression matching — has a standard shape: `dp[i][j]` represents the answer for the first i characters of one string and the first j of the other. The base cases are the empty string prefixes. The transitions compare characters at positions i and j and either extend a matching prefix or consider insertions/deletions.

**The four-rung ladder** is the standard development process for any DP solution:

1. **Plain recursion** — write the recurrence directly. Verify correctness on small inputs before optimising. The recursive definition should be clean and easy to reason about.
2. **Memoisation** — add a cache (a `HashMap` or array indexed by the parameters). Now each unique subproblem is computed exactly once; subsequent calls return the cached result.
3. **Tabulation** — fill a table bottom-up in the order that ensures each cell's dependencies are already computed when you need them. This eliminates recursion overhead and stack overflow risk.
4. **Space optimisation** — if each row of the table only depends on the previous row (or the current row only), keep only those rows in memory.

**When to use greedy instead of DP:** A greedy algorithm makes the locally optimal choice at each step. It works when you can prove that a locally optimal choice is also globally optimal — usually via an exchange argument (swapping any other choice for the greedy choice never improves the result) or a staying-ahead argument (the greedy solution is at least as good at every intermediate step). When in doubt, look for a counterexample. The coins {1, 3, 4} with target 6 is the classic case: greedy picks 4+1+1 for 3 coins, but 3+3 gives 2 coins. If you find a counterexample, use DP.

| Signal | Technique | Why it works | Complexity |
|--------|-----------|-------------|-----------|
| "number of ways" | DP counting | Sum the ways to reach the current state from all predecessor states. | states * transitions |
| "minimum cost", "maximum value" | DP optimisation | Take the min/max over all predecessor states plus the transition cost. | states * transitions |
| "can this string be broken into words?" | DP over positions | `dp[i]` = can the prefix `s[0..i)` be segmented? For each dictionary word that ends at i, check if the prefix before it can be segmented. | O(n * m) |
| "edit distance", "longest common subsequence" | 2D DP over two strings | `dp[i][j]` represents the answer for the first i characters of one string and first j of the other. | O(m * n) |
| "knapsack", "subset sum" | DP over items and capacity | `dp[i][w]` = best value using the first i items with capacity w. | O(n * W) |

---

## 2. Operations to Data Structure

For design problems, start by listing the operations the solution must support. Then pick the structure that makes the most frequent operation cheap.

The single most common mistake in design problems is choosing a structure that handles the obvious operation well but fails on a secondary operation. A `PriorityQueue` looks right for "get the best element" until you realise you also need to remove arbitrary elements — and `PriorityQueue.remove(Object)` is O(n). A `HashMap` looks right for lookup until you realise you need the "largest element below a given threshold" — and a hash map can't answer range queries. Always think through all the required operations before committing to a structure.

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

**Critical rule:** Never mutate a field that a comparator reads while the object is inside a TreeSet or PriorityQueue. The ordering invariant of the structure depends on the comparator producing consistent results. Mutating a key field without removing and re-inserting first silently corrupts the structure. Always remove, change, re-insert.

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

In Java, assume roughly 10^8 simple operations per second. Divide that budget by the input size to get the complexity you can afford.

The constraint is not just a number — it is a technique selector. If n is 10^5, you cannot afford O(n^2) (that's 10^10 operations), so all-pairs brute force and 2D DP over the full input are out. You must find a sorting, hashing, or heap-based solution. If n is only 20, then even exponential algorithms are within budget, which tells you backtracking and bitmask DP are on the table. Reading the constraint before reading the problem body helps you immediately discard wrong approaches.

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
