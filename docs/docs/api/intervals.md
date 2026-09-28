# Intervals

**Package:** `io.algokit.intervals`  
**Source:** [`Intervals.java`](https://github.com/maneeshchaturvedi/algokit-java/blob/main/src/main/java/io/algokit/intervals/Intervals.java)

Interval problems are a recurring category in interviews: scheduling, calendar conflicts, resource allocation, and range coverage all reduce to questions about sets of `[start, end]` pairs. This package provides the foundational merge operation that underlies most of them.

---

## When Intervals Arise

Recognizing interval problems is the first skill. They appear when:

- **Scheduling:** meetings, tasks, or jobs with start and end times — "find minimum meeting rooms required", "detect conflicts", "find gaps in a schedule."
- **Range coverage:** "do these ranges cover the entire number line?", "what is the union of all intervals?"
- **Resource allocation:** "minimum number of arrows to burst all balloons", "minimum number of intervals to remove to make them non-overlapping."
- **Coordinate compression:** transforming a set of ranges into a smaller index space before a DP or segment tree computation.

The universal first step for almost all interval problems is **sort by start time**. Once sorted, you only ever need to compare the current interval against the most recent one (or a small structure tracking active intervals), reducing what would otherwise be O(n²) comparisons to a single linear sweep.

---

## The Sort-then-Sweep Approach

After sorting by `start`:

```
Sorted intervals: [1,3], [2,6], [8,10], [9,11], [15,18]
                    ^
                    Last merged end: 6

Step 1: [1,3]  -> output start new: [[1,3]]
Step 2: [2,6]  -> 2 <= 3 (overlaps), extend end to max(3,6)=6:  [[1,6]]
Step 3: [8,10] -> 8 > 6  (gap), output new interval: [[1,6],[8,10]]
Step 4: [9,11] -> 9 <= 10 (overlaps), extend end to 11: [[1,6],[8,11]]
Step 5: [15,18]-> 15 > 11 (gap), output new interval: [[1,6],[8,11],[15,18]]
```

The invariant: the last element in `out` always represents the current running merged interval. A new interval either extends it (if they overlap) or becomes a new separate interval. Two intervals `[a,b]` and `[c,d]` (with `a <= c` after sorting) overlap if and only if `c <= b`.

---

## `merge`

### Signature
```java
public static List<int[]> merge(int[][] intervals)
```

### How and why it works
Clones the input array (so the original is not modified), sorts by start value using `Comparator.comparingInt(iv -> iv[0])`, then does one linear pass. For each interval, it checks whether `iv[0] <= out.getLast()[1]` — if the new interval starts before or at the end of the last merged interval, they overlap and the end is extended to `Math.max(existing_end, iv[1])`. Using `Math.max` handles the "fully contained" case where the new interval's end is smaller than the current merged end. Otherwise, the current interval is appended as a new, separate entry.

### Complexity
- **Time:** O(n log n) — dominated by the sort; the sweep is O(n)
- **Space:** O(n) for the cloned sorted array and the output list

### Source code
```java
public static List<int[]> merge(int[][] intervals) {
    int[][] sorted = intervals.clone();
    Arrays.sort(sorted, Comparator.comparingInt(iv -> iv[0]));
    List<int[]> out = new ArrayList<>();
    for (int[] iv : sorted) {
        if (!out.isEmpty() && iv[0] <= out.get(out.size() - 1)[1]) {
            out.get(out.size() - 1)[1] = Math.max(out.get(out.size() - 1)[1], iv[1]);
        } else {
            out.add(new int[]{iv[0], iv[1]});
        }
    }
    return out;
}
```

### Usage example
```java
// LC 56: standard merge
int[][] intervals = {{1,3},{2,6},{8,10},{15,18}};
List<int[]> merged = Intervals.merge(intervals);
// merged: [[1,6],[8,10],[15,18]]

// Verify
for (int[] iv : merged) System.out.println(Arrays.toString(iv));

// Input unmodified (clone was sorted internally)
System.out.println(intervals[0][0]); // still 1

// Single interval — trivially merged
Intervals.merge(new int[][]{{5, 10}});  // [[5,10]]

// All overlapping — one big interval
Intervals.merge(new int[][]{{1,4},{2,5},{3,6}}); // [[1,6]]

// Using merge to count gaps (unoccupied ranges)
List<int[]> coverage = Intervals.merge(busySlots);
// gaps between coverage[i][1] and coverage[i+1][0]
```

### Related LeetCode problems
- **56**  – Merge Intervals (this exact problem)
- **57**  – Insert Interval (insert a new interval and re-merge; binary search for position)
- **252** – Meeting Rooms (do any two intervals overlap? sort + check consecutive pairs)
- **253** – Meeting Rooms II (minimum rooms = maximum concurrent intervals; use a min-heap or sweep-line event counter)
- **435** – Non-overlapping Intervals (greedy: remove minimum intervals to make them non-overlapping)
- **452** – Minimum Number of Arrows to Burst Balloons (same greedy structure)
- **1288** – Remove Covered Intervals
- **986** – Interval List Intersections (two-pointer merge of two sorted interval lists)

### Common variants

**Insert interval (LC 57):** binary search for the insertion point, then re-run merge. Or iterate manually, passing through non-overlapping intervals and merging overlapping ones into the new interval:

```java
List<int[]> insert(int[][] existing, int[] newIv) {
    List<int[]> result = new ArrayList<>();
    for (int[] iv : existing) {
        if (iv[1] < newIv[0]) {
            result.add(iv);               // completely before new interval
        } else if (iv[0] > newIv[1]) {
            result.add(newIv);            // new interval is now finalized
            newIv = iv;                   // continue with current
        } else {
            newIv[0] = Math.min(newIv[0], iv[0]); // overlap: merge
            newIv[1] = Math.max(newIv[1], iv[1]);
        }
    }
    result.add(newIv);
    return result;
}
```

**Meeting Rooms II (LC 253) — minimum concurrent intervals:**

```java
int minRooms(int[][] intervals) {
    int n = intervals.length;
    int[] starts = new int[n], ends = new int[n];
    for (int i = 0; i < n; i++) { starts[i] = intervals[i][0]; ends[i] = intervals[i][1]; }
    Arrays.sort(starts); Arrays.sort(ends);
    int rooms = 0, maxRooms = 0, e = 0;
    for (int s : starts) {
        if (s < ends[e]) rooms++;
        else e++;
        maxRooms = Math.max(maxRooms, rooms);
    }
    return maxRooms;
}
```

**Interval intersection (LC 986):** merge two sorted lists of intervals into their intersecting segments using a two-pointer approach — advance the pointer whose interval ends first.

### The Sweep Line Pattern

The sweep line is a generalization of the sort-then-sweep approach. Instead of thinking about intervals as wholes, convert them to a list of **events** — each interval `[start, end]` generates a "start event" at `start` and an "end event" at `end`. Sort all events by time (breaking ties by type), then process them sequentially, maintaining a counter or data structure of "active" intervals.

```java
// Minimum meeting rooms via sweep line
List<int[]> events = new ArrayList<>();
for (int[] iv : intervals) {
    events.add(new int[]{iv[0], 1});   // +1 at start
    events.add(new int[]{iv[1], -1});  // -1 at end
}
events.sort((a, b) -> a[0] != b[0] ? a[0] - b[0] : a[1] - b[1]); // end before start on ties
int active = 0, maxActive = 0;
for (int[] e : events) {
    active += e[1];
    maxActive = Math.max(maxActive, active);
}
// maxActive = minimum rooms needed
```

The sweep line generalizes to:
- **Skyline problem (LC 218):** sweep building starts/ends with a max-heap of active heights.
- **Count points covered by intervals:** sweep with a difference array.
- **Segment covering:** merge intervals then check if the union covers a target range.

### Gotchas
- `intervals.clone()` performs a **shallow clone** — it copies the array of references, not the `int[]` sub-arrays themselves. The sort operates on the clone (reordering references) without mutating the sub-arrays, so `intervals[i]` still refers to the same `int[]` objects. However, the `out.get(out.size()-1)[1] = ...` line mutates a newly created `int[]` in `out`, not the original — this is safe.
- The overlap condition is `iv[0] <= last[1]`, not `iv[0] < last[1]`. Adjacent intervals like `[1,3]` and `[3,5]` have `iv[0] == last[1]` and should be merged into `[1,5]`. Using strict `<` would incorrectly treat them as separate.
- `Math.max(existing_end, iv[1])` is necessary because after sorting by start, a later interval's end can still be less than the current merged end (e.g., `[1,10]` then `[2,5]` — `[2,5]` is contained within `[1,10]`).
- The output list contains newly allocated `int[]{iv[0], iv[1]}` arrays, not references into the input. Mutating the output does not affect the input.
- For problems where intervals are `long`-valued (timestamps in milliseconds, etc.), change `int[]` to `long[]` throughout.
