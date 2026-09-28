# Java Collections Guide

Choosing the right collection is half the battle in a coding interview. This guide covers every collection you'll reach for, with the guarantees that matter, the caveats that bite, and the situations where each one shines.

---

## Decision Flowchart

Ask yourself these questions in order:

1. **Do I need key-value pairs?** → Map (go to Maps section)
2. **Do I just need membership / uniqueness?** → Set (go to Sets section)
3. **Do I need ordered access by index?** → `ArrayList` or `int[]`
4. **Do I need LIFO (stack) or FIFO (queue)?** → `ArrayDeque`
5. **Do I need "give me the best element"?** → `PriorityQueue` or `TreeSet`

---

## Lists

### `ArrayList` — the default sequence

`ArrayList` should be your starting point for any ordered sequence. The reason is simple: it gives you O(1) random access by index and O(1) amortised append, which covers the vast majority of what you need from a list. The cost you pay is that inserting or removing anywhere except the end requires shifting elements, making those operations O(n). Similarly, `contains` and `indexOf` perform a linear scan, so they are O(n) as well.

You should move away from `ArrayList` in three specific situations. First, if you're repeatedly removing from the front inside a loop, use `ArrayDeque` instead — removing the head of an ArrayList shifts every remaining element. Second, if you're testing membership in a loop, the O(n) cost of `contains` turns the whole loop into O(n²); convert to a `HashSet` first. Third, if the size is known up front and values are primitive integers (frequencies, distances, DP tables), a plain `int[]` is much faster due to no boxing overhead and better cache behaviour.

There are three mistakes that appear constantly with `ArrayList`:

```java
// MISTAKE: remove(int) removes by INDEX, not value
List<Integer> list = new ArrayList<>(List.of(5, 6, 7));
list.remove(5);  // throws IndexOutOfBoundsException! Index 5 doesn't exist.
list.remove(Integer.valueOf(5));  // removes the VALUE 5

// MISTAKE: contains() in a loop is O(n^2)
for (int x : data) {
    if (list.contains(x)) { ... }  // O(n) per call × n calls = O(n^2)
}
// FIX: convert to a set first
Set<Integer> set = new HashSet<>(list);
for (int x : data) {
    if (set.contains(x)) { ... }  // O(1) per call
}

// MISTAKE: structural modification during for-each
for (Integer x : list) {
    if (x < 0) list.remove(x);  // ConcurrentModificationException!
}
// FIX: use removeIf
list.removeIf(x -> x < 0);
```

### `int[]` — when you know the size

When you know the size up front and your values are primitive, reach for `int[]` rather than `ArrayList<Integer>`. The advantages compound: no autoboxing means less garbage and less CPU; the memory layout is contiguous so cache misses are rare; and `Arrays.sort`, `Arrays.fill`, and `Arrays.copyOfRange` work directly on it.

The most common use cases are character counting (`int[26]` for lowercase letters, `int[128]` for ASCII), DP tables (`int[n]` or `int[m][n]`), and distance arrays initialised to `-1` or `Integer.MAX_VALUE`.

The one trap to know: arrays use identity equality, not structural equality.

```java
new int[]{1, 2}.equals(new int[]{1, 2})  // FALSE — uses Object.equals (identity)
Arrays.equals(new int[]{1, 2}, new int[]{1, 2})  // TRUE
```

This also means you should never use `int[]` as a `HashMap` key. Two arrays with the same contents are different objects, so the second lookup will always return null. Use a `record`, a `List<Integer>`, or pack two ints into a `long` instead.

### `LinkedList` — almost never

In theory, `LinkedList` offers O(1) insertion at both ends and O(1) removal given a direct node reference. In practice, the pointer-chasing overhead makes it slower than `ArrayDeque` for stacks and queues, and slower than `ArrayList` for indexed access. The only legitimate use case is when you genuinely need O(1) removal from the middle using a direct node reference — and in that scenario, you typically write your own doubly-linked list nodes (as in LRU Cache) rather than using `LinkedList` through the `List` interface.

---

## Maps

### `HashMap` — the default map

`HashMap` gives you O(1) average time for `get`, `put`, `remove`, and `containsKey`. It makes no promises about the order in which keys are stored or iterated — that order is effectively arbitrary and can change as the map grows. For the majority of interview problems, this is exactly what you need.

The modern idioms are more powerful than the old `get`-then-`put` pattern:

```java
// Counting
freq.merge(key, 1, Integer::sum);

// Grouping
groups.computeIfAbsent(key, k -> new ArrayList<>()).add(value);

// Decrement and auto-remove at zero
freq.merge(key, -1, (old, delta) -> old + delta == 0 ? null : old + delta);

// Safe lookup with default
freq.getOrDefault(key, 0);
```

There are four caveats worth internalising.

**The equals/hashCode contract.** HashMap finds entries by first computing `hashCode` to locate a bucket, then calling `equals` to confirm the match. Objects that are `equals` must return the same `hashCode`. If you override one without the other, lookups silently fail. Java `record` types handle this automatically, making them the best choice for composite keys:

```java
// GOOD: records have correct equals/hashCode automatically
record Point(int x, int y) {}
Map<Point, String> map = new HashMap<>();
map.put(new Point(1, 2), "a");
map.get(new Point(1, 2));  // returns "a"

// BAD: int[] uses identity equals, so a fresh array never matches
Map<int[], String> map = new HashMap<>();
map.put(new int[]{1, 2}, "a");
map.get(new int[]{1, 2});  // returns null — different object
```

**Never mutate a key.** If you change a field that affects `hashCode` while the object sits in the map, the entry becomes stranded: `get` can't find it because it looks in the wrong bucket, but the entry still occupies space. To change a key, remove it first, change the value, then re-insert.

**`get` returning null is ambiguous.** It means either "key absent" or "key present with a null value." Use `getOrDefault` when you want a fallback, and be especially careful with auto-unboxing:

```java
Map<String, Integer> map = new HashMap<>();
int val = map.get("missing");           // NullPointerException — unboxing null to int
int val = map.getOrDefault("missing", 0);  // Safe
```

**Iteration order is arbitrary.** Never let output correctness depend on HashMap iteration order. If the problem requires a specific order, sort the entries afterward, or use `LinkedHashMap` or `TreeMap` from the start.

### `LinkedHashMap` — insertion-order iteration

`LinkedHashMap` has the same O(1) costs as `HashMap` but maintains a doubly-linked list through its entries, so iteration follows a predictable order. By default that order is insertion order. By passing `true` as the third constructor argument, you get access order instead: every `get` or `put` moves the accessed entry to the tail, making the head the least recently used entry.

```java
// Insertion order (default)
Map<String, Integer> ordered = new LinkedHashMap<>();

// Access order — the foundation for an LRU cache
Map<String, Integer> lru = new LinkedHashMap<>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
        return size() > capacity;
    }
};
```

The LRU cache pattern works because `removeEldestEntry` is called after every `put`. When the map exceeds capacity, it evicts the entry at the head — which is, by access-order semantics, the one that hasn't been touched the longest.

### `TreeMap` — sorted keys with navigation

`TreeMap` pays O(log n) for every operation in exchange for two things `HashMap` cannot offer: keys are always sorted, and you can navigate by value with methods like `floorKey` and `ceilingKey`. This makes it the right choice whenever a problem involves range queries, "what was the value as of time t", or "find the nearest key to x".

The navigation methods are what set `TreeMap` apart. `floorKey(k)` returns the greatest key that is less than or equal to k; `ceilingKey(k)` returns the least key that is greater than or equal to k. The `lower` and `higher` variants are the strict counterparts (strictly less than, strictly greater than). Range views like `headMap`, `tailMap`, and `subMap` return live views of the map within a key range.

A common pattern is using a `TreeMap` as a versioned history:

```java
TreeMap<Long, String> history = new TreeMap<>();
history.put(10L, "starter");
history.put(20L, "pro");

// What was the value at time 15?
Map.Entry<Long, String> entry = history.floorEntry(15L);
// entry.getValue() == "starter" — the latest write at or before t=15
```

One caveat: all navigation methods return null when no matching key exists. Never unbox the result directly:

```java
Integer floor = tree.floorKey(5);  // might be null
if (floor != null) { ... }         // safe
int f = tree.floorKey(5);          // NullPointerException if no key <= 5
```

**Navigation methods — complete reference:**

| Method | Returns | Example on {10, 20, 30} |
|--------|---------|------------------------|
| `floorKey(25)` | Greatest key ≤ 25 | 20 |
| `lowerKey(20)` | Greatest key < 20 | 10 |
| `ceilingKey(25)` | Least key ≥ 25 | 30 |
| `higherKey(20)` | Least key > 20 | 30 |
| `firstKey()` | Smallest key | 10 (throws if empty) |
| `lastKey()` | Largest key | 30 (throws if empty) |
| `floorEntry(k)` | Entry with greatest key ≤ k | The entry, or null |
| `pollFirstEntry()` | Remove and return the smallest entry | Removes 10's entry |
| `headMap(20)` | View of keys < 20 | {10} |
| `headMap(20, true)` | View of keys ≤ 20 | {10, 20} |
| `tailMap(20)` | View of keys ≥ 20 | {20, 30} |
| `subMap(10, 30)` | View of keys in [10, 30) | {10, 20} |
| `descendingMap()` | Reverse-order view | {30, 20, 10} |

**Quick reference — choosing a Map:**

| You need... | Choose | Because |
|-------------|--------|---------|
| Fast lookup, no ordering needed | `HashMap` | O(1) average |
| Same, plus insertion or recency order | `LinkedHashMap` | Order kept at same O(1) cost |
| Sorted keys, floor/ceiling, range queries | `TreeMap` | O(log n) per query vs O(n log n) sort-on-demand |

---

## Sets

### `HashSet` — the default set

`HashSet` gives you O(1) `add`, `remove`, and `contains`. It is backed by a `HashMap`, so all the same caveats apply: correct `equals`/`hashCode` on elements is essential, and iteration order is arbitrary.

Reach for `HashSet` any time you need to answer "have I seen this before?" in O(1). It's the right fix when you catch yourself calling `ArrayList.contains` in a loop.

### `TreeSet` — sorted with navigation

`TreeSet` is to `HashSet` what `TreeMap` is to `HashMap`: O(log n) for all operations, keys always sorted, and navigation methods (`first`, `last`, `floor`, `ceiling`, `headSet`, `tailSet`) available. Use it when you need to efficiently find the nearest element to a value, or when you need a sorted collection with fast insertion and deletion.

The critical thing to understand is how `TreeSet` defines equality. Unlike `HashSet`, which uses `equals`, a `TreeSet` considers two elements identical if and only if `compare` returns 0. This has a sharp consequence: if your comparator only compares by one field and two elements share the same value for that field, the second insert silently drops the first.

```java
// WRONG: two tasks with priority 3 — only one survives
TreeSet<Task> set = new TreeSet<>(Comparator.comparingInt(Task::priority));

// RIGHT: add a unique tie-break so compare never returns 0 for distinct objects
TreeSet<Task> set = new TreeSet<>(
    Comparator.comparingInt(Task::priority)
              .thenComparing(Task::id)  // id is unique
);
```

A related trap: if you mutate a field that the comparator reads while the object is in the set, the set loses track of it. The element is still present — remove by iteration would find it — but `contains` and `remove(element)` will return false because they look in the wrong position. Always remove, update, and re-insert:

```java
Task old = byId.get(taskId);
ranked.remove(old);                    // remove with OLD comparator key
Task updated = old.withPriority(5);   // create updated version
ranked.add(updated);                   // insert with NEW comparator key
byId.put(taskId, updated);
```

---

## Stacks, Queues, and Deques

### `ArrayDeque` — the universal choice

`ArrayDeque` is the right tool whether you need a stack, a FIFO queue, or a double-ended queue. It outperforms `Stack` (legacy, unnecessarily synchronised) and `LinkedList` (pointer overhead per node) in all three roles. Under the hood it's a resizable circular array, so push and pop at both ends are O(1) amortised.

As a stack, use the `push`/`peek`/`pop` methods, which operate on the head:

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1);       // add to head
stack.peek();        // look at head (null if empty)
stack.pop();         // remove from head (throws if empty)
```

As a FIFO queue, use `offer`/`peek`/`poll`, which add to the tail and remove from the head:

```java
Deque<int[]> queue = new ArrayDeque<>();
queue.offer(item);   // add to tail
queue.peek();        // look at head (null if empty)
queue.poll();        // remove from head (null if empty)
```

As a double-ended deque — needed for monotonic deque problems and sliding window maximum — use the explicit `First`/`Last` variants:

```java
dq.offerFirst(x);    // add to head
dq.offerLast(x);     // add to tail
dq.peekFirst();      // look at head
dq.peekLast();       // look at tail
dq.pollFirst();      // remove from head
dq.pollLast();       // remove from tail
```

A few caveats to keep in mind. `ArrayDeque` rejects null elements because `poll` and `peek` use null as their sentinel for "empty" — storing null would make those results ambiguous. The `pop` family throws on an empty deque; the `poll` family returns null — pick one family and stick to it within a method. `contains` and `remove(Object)` are O(n) since there's no index. And iteration order is head-to-tail (top-to-bottom for stack usage), which is the opposite of the legacy `Stack` class's bottom-to-top iteration — worth knowing if you're printing a stack trace for debugging.

---

## Priority Queues

### `PriorityQueue` — give me the best element

A `PriorityQueue` is a min-heap by default: `peek` and `poll` always return the smallest element according to the comparator. Offer and poll are O(log n); peek is O(1); and building from an existing collection is O(n) — faster than n individual offers, so use the constructor when you have data up front.

```java
// Min-heap (default)
PriorityQueue<Integer> minHeap = new PriorityQueue<>();

// Max-heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());

// Custom: sort by array's first element
PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x[0]));
```

The most common misconception is that iterating a `PriorityQueue` gives you sorted order. It does not. The heap property only guarantees that the root is the minimum; the rest of the internal array is in heap order, not sorted order. To consume elements in order, `poll` repeatedly:

```java
PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(5, 1, 4, 2, 3));
System.out.println(pq);  // NOT [1, 2, 3, 4, 5] — internal heap order
// To get sorted output, poll() repeatedly
```

The other important cost to know: `remove(Object)` is O(n) because the heap has no index for arbitrary elements. If your algorithm needs to cancel or reprioritise elements already in the queue, use `TreeSet` instead (with a unique tie-break comparator so duplicates aren't silently dropped).

When reprioritisation is rare or you can afford to leave stale entries, the lazy deletion pattern avoids the O(n) remove entirely. Insert the updated entry alongside the old one, and skip stale entries when you poll:

```java
pq.offer(new long[]{newDist, node});
// ... later, when polling:
long[] top = pq.poll();
if (top[0] > dist[(int) top[1]]) continue;  // stale entry, skip it
```

This is exactly the pattern used in Dijkstra's algorithm.

---

## Comparators

### Building comparators safely

The single most important rule is: never use subtraction in a comparator. `a - b` overflows for large values of opposite sign, producing a result with the wrong sign and silently corrupting your sorted order or heap.

```java
// WRONG: overflows when a = Integer.MIN_VALUE, b = 1
Comparator<int[]> broken = (a, b) -> a[0] - b[0];

// RIGHT: use compare methods
Comparator<int[]> correct = (a, b) -> Integer.compare(a[0], b[0]);
Comparator<int[]> also    = Comparator.comparingInt(x -> x[0]);
```

### Multi-key comparators

When one field isn't enough to break ties — which is common with `TreeSet` and `TreeMap` — chain comparisons with `thenComparing`. The last field in the chain should be a unique key so that `compare` never returns 0 for two distinct objects:

```java
// Priority DESC, then createdAt ASC, then id ASC (guarantees total order)
Comparator<Task> order = Comparator
    .comparingInt(Task::priority).reversed()   // reversed() reverses ONLY priority
    .thenComparingLong(Task::createdAt)
    .thenComparing(Task::id);                  // unique tie-break for TreeSet safety
```

There is a subtle trap with `reversed()`: it reverses the entire chain built up to that point, not just the immediately preceding comparison. Placing it at the end of a chain reverses all fields, which is almost never what you want:

```java
// This reverses BOTH priority AND createdAt
Comparator.comparingInt(Task::priority)
          .thenComparingLong(Task::createdAt)
          .reversed();

// This reverses ONLY priority
Comparator.comparing(Task::priority, Comparator.reverseOrder())
          .thenComparingLong(Task::createdAt);
```

### Comparator for Map entries

When you need to sort map entries — for example, to return the top-k frequent words — use the built-in entry comparator helpers:

```java
// Sort entries: count DESC, then key ASC
Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
    .thenComparing(Map.Entry.comparingByKey());
```

---

## Sorting

### Object sorting is stable; primitive sorting is not

`List.sort()`, `Collections.sort()`, and `Arrays.sort(Object[])` all use a stable merge sort. Equal elements keep their relative order, and the worst-case time is O(n log n). This stability matters whenever you sort by one key and then sort by another — the second sort preserves the ordering established by the first.

`Arrays.sort(int[])` uses dual-pivot quicksort. It is not stable, but for primitive arrays that doesn't matter: equal integers are indistinguishable, so there's no relative order to preserve.

### Sorting primitives in custom order

`Arrays.sort(int[], Comparator)` does not exist because comparators require objects. When you need to sort primitives in descending order or by some other custom rule, your options are:

- Sort ascending (the default) and then iterate or consume in reverse.
- Box to `Integer[]` and sort with a comparator — wasteful for large arrays, but simple.
- Use a `List<Integer>` and `Collections.sort`.

### Sorting 2D arrays

Multi-column sorts on 2D arrays are common in interval and scheduling problems:

```java
// Sort by first column ascending
Arrays.sort(intervals, Comparator.comparingInt(iv -> iv[0]));

// Sort by first column, then by second column
Arrays.sort(intervals, Comparator.comparingInt((int[] iv) -> iv[0])
                                  .thenComparingInt(iv -> iv[1]));
```

The explicit type annotation `(int[] iv)` on the first lambda is necessary when chaining; without it the compiler cannot infer the array type for the second lambda.

---

## Choosing at a Glance

| You need... | Choose | Instead of | Because |
|-------------|--------|-----------|---------|
| Lookup/insert/delete by key, no order needed | `HashMap` / `HashSet` | `TreeMap` | O(1) vs O(log n) |
| Same, plus insertion or recency order | `LinkedHashMap` | `HashMap` + extra list | Order kept at the same cost |
| Sorted keys, floor/ceiling, ranges | `TreeMap` / `TreeSet` | `HashMap` + sort per query | O(log n) per query vs O(n log n) |
| Repeated "take the best", few deletions | `PriorityQueue` | `TreeSet` | Simpler, smaller constant factor |
| Best element plus arbitrary removal/update | `TreeSet` (unique tie-break) | `PriorityQueue` | PQ `remove(Object)` is O(n) |
| Stack, queue, deque, sliding window | `ArrayDeque` | `Stack`, `LinkedList` | O(1) at both ends, no legacy baggage |
| Indexed sequence | `ArrayList` | `LinkedList` | O(1) index access |
| Counts over small dense integer keys | `int[]` | `HashMap<Integer, Integer>` | No boxing, far less memory |
| Groups that merge over time | `UnionFind` | Re-running BFS | ~O(1) amortised per operation |
