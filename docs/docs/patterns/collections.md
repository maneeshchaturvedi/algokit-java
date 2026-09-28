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

**Guarantees:**

- O(1) random access by index (`get`, `set`)
- O(1) amortised append (`add` to the end)
- O(n) insert/remove anywhere except the end (elements shift)
- O(n) `contains` and `indexOf` (linear scan)

**When to use:** Almost always. It's the right choice unless you specifically need something it can't do.

**When NOT to use:**

- Removing from the front in a loop → use `ArrayDeque`
- Membership testing in a loop → use `HashSet`
- The size is known and values are primitive → use `int[]`

**Common mistakes:**

```java
// MISTAKE: remove(int) removes by INDEX, not value
List<Integer> list = new ArrayList<>(List.of(5, 6, 7));
list.remove(5);  // throws IndexOutOfBoundsException! Index 5 doesn't exist.
list.remove(Integer.valueOf(5));  // removes the VALUE 5 ✓

// MISTAKE: contains() in a loop is O(n^2)
for (int x : data) {
    if (list.contains(x)) { ... }  // O(n) per call × n calls = O(n^2)
}
// FIX: convert to a set first
Set<Integer> set = new HashSet<>(list);
for (int x : data) {
    if (set.contains(x)) { ... }  // O(1) per call ✓
}

// MISTAKE: structural modification during for-each
for (Integer x : list) {
    if (x < 0) list.remove(x);  // ConcurrentModificationException!
}
// FIX: use removeIf
list.removeIf(x -> x < 0);  // ✓
```

### `int[]` — when you know the size

**When to prefer it over ArrayList:**

- Character counting: `int[26]` for lowercase, `int[128]` for ASCII
- DP tables: `int[n]`, `int[m][n]`
- Distance arrays: `int[n]` filled with -1 or MAX_VALUE
- Any time you know the size up front and values are primitive

**Advantages:** No boxing overhead, much less memory, better cache locality, can use `Arrays.sort`, `Arrays.fill`, `Arrays.copyOfRange`.

**Pitfall: arrays use identity equality**

```java
new int[]{1, 2}.equals(new int[]{1, 2})  // FALSE — uses Object.equals (identity)
Arrays.equals(new int[]{1, 2}, new int[]{1, 2})  // TRUE ✓
```

Never use `int[]` as a HashMap key — use a `record`, `List<Integer>`, or pack two ints into a `long`.

### `LinkedList` — almost never

In theory, O(1) insertion at both ends. In practice, `ArrayDeque` is faster for stacks and queues, and `ArrayList` is faster for indexed access. The only legitimate use case is when you need O(1) removal from the middle using a direct node reference — and in that case, you write your own doubly-linked list nodes (as in LRU Cache).

---

## Maps

### `HashMap` — the default map

**Guarantees:**

- O(1) average for `get`, `put`, `remove`, `containsKey`
- No ordering guarantee whatsoever

**Essential idioms:**

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

**Caveats in depth:**

**1. The equals/hashCode contract**

Objects that are `equals` must have the same `hashCode`. If you override one without the other, lookups silently fail. Java `record` types get this right automatically — they are the best choice for composite keys.

```java
// GOOD: records have correct equals/hashCode
record Point(int x, int y) {}
Map<Point, String> map = new HashMap<>();
map.put(new Point(1, 2), "a");
map.get(new Point(1, 2));  // returns "a" ✓

// BAD: int[] uses identity equals
Map<int[], String> map = new HashMap<>();
map.put(new int[]{1, 2}, "a");
map.get(new int[]{1, 2});  // returns null! Different object.
```

**2. Never mutate a key in the map**

If you change a field that affects `hashCode` while the object is in the map, the entry becomes stranded: `get` can't find it, but it still occupies space. To change a key, remove first, change, then re-insert.

**3. `get` returning null is ambiguous**

It means either "key absent" or "key present with null value." Use `getOrDefault` when you want a fallback, or `containsKey` when the distinction matters. And beware of auto-unboxing:

```java
Map<String, Integer> map = new HashMap<>();
int val = map.get("missing");  // NullPointerException! Unboxing null to int.
int val = map.getOrDefault("missing", 0);  // Safe ✓
```

**4. Iteration order is arbitrary**

Never let output depend on HashMap iteration order — it can change as the map grows. Sort the entries, or use `LinkedHashMap` or `TreeMap`.

### `LinkedHashMap` — insertion-order iteration

Same O(1) costs as HashMap, but iteration follows insertion order. Two modes:

**Insertion order (default):**

```java
Map<String, Integer> ordered = new LinkedHashMap<>();
```

**Access order (for LRU cache):**

```java
// Third parameter: true = access order
Map<String, Integer> lru = new LinkedHashMap<>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
        return size() > capacity;
    }
};
```

In access-order mode, every `get` or `put` moves the entry to the tail. The eldest entry (head) is the least recently used.

### `TreeMap` — sorted keys with navigation

**Guarantees:**

- O(log n) for `get`, `put`, `remove`
- Keys are always sorted
- Supports navigation: `floorKey`, `ceilingKey`, `firstKey`, `lastKey`
- Supports range views: `headMap`, `tailMap`, `subMap`

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

**The "value as of time t" pattern:**

```java
TreeMap<Long, String> history = new TreeMap<>();
history.put(10L, "starter");
history.put(20L, "pro");

// What was the value at time 15?
Map.Entry<Long, String> entry = history.floorEntry(15L);
// entry.getValue() == "starter" — the latest write at or before t=15
```

**Caveat: navigation methods return null.** Always null-check before unboxing:

```java
Integer floor = tree.floorKey(5);  // might be null
if (floor != null) { ... }         // ✓
int f = tree.floorKey(5);          // NullPointerException if no key ≤ 5!
```

---

## Sets

### `HashSet` — the default set

O(1) `add`, `remove`, `contains`. Same caveats as HashMap (since it's backed by one).

### `TreeSet` — sorted with navigation

O(log n) for all operations. Supports `first`, `last`, `floor`, `ceiling`, `headSet`, `tailSet`.

**The critical TreeSet caveat: comparator defines equality.**

A TreeSet treats two elements as the same if `compare` returns 0, regardless of what `equals` says. If your comparator only compares by priority, two different items with the same priority silently collapse into one:

```java
// WRONG: drops duplicates by priority
TreeSet<Task> set = new TreeSet<>(Comparator.comparingInt(Task::priority));
// Two tasks with priority 3 → only one survives

// RIGHT: end the chain with a unique key
TreeSet<Task> set = new TreeSet<>(
    Comparator.comparingInt(Task::priority)
              .thenComparing(Task::id)  // unique tie-break
);
```

**Update pattern for TreeSet:** Never mutate a field that the comparator reads while the object is in the set. Always remove, change, re-insert:

```java
Task old = byId.get(taskId);
ranked.remove(old);                    // remove with OLD comparator key
Task updated = old.withPriority(5);    // create updated version
ranked.add(updated);                   // insert with NEW comparator key
byId.put(taskId, updated);
```

---

## Stacks, Queues, and Deques

### `ArrayDeque` — the universal choice

`ArrayDeque` serves as a stack, a FIFO queue, or a double-ended queue. It outperforms `Stack` (legacy, synchronised), `LinkedList` (pointer overhead), and is the recommended choice for all three use cases.

**As a stack:**

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1);       // add to head
stack.peek();        // look at head (null if empty)
stack.pop();         // remove from head (throws if empty)
```

**As a queue:**

```java
Deque<int[]> queue = new ArrayDeque<>();
queue.offer(item);   // add to tail
queue.peek();        // look at head (null if empty)
queue.poll();        // remove from head (null if empty)
```

**As a deque (for monotonic deque, BFS with priority, etc.):**

```java
dq.offerFirst(x);   // add to head
dq.offerLast(x);    // add to tail
dq.peekFirst();      // look at head
dq.peekLast();       // look at tail
dq.pollFirst();      // remove from head
dq.pollLast();       // remove from tail
```

**Caveats:**

- Rejects null (because `poll`/`peek` use null to mean "empty")
- `pop` throws on empty; `poll` returns null on empty — pick the family you want
- `contains` and `remove(Object)` are O(n)
- Not thread-safe
- **Iteration order differs from Stack:** `ArrayDeque` iterates head-to-tail (top-to-bottom for stack usage), while legacy `Stack` iterates bottom-to-top

---

## Priority Queues

### `PriorityQueue` — give me the best element

A min-heap by default. The head is always the smallest element by the comparator.

```java
// Min-heap (default)
PriorityQueue<Integer> minHeap = new PriorityQueue<>();

// Max-heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());

// Custom: sort by array's first element
PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x[0]));
```

**Costs:**

- `peek()`: O(1)
- `offer()` and `poll()`: O(log n)
- `remove(Object)`: **O(n)** — this is why you often need TreeSet instead
- Building from a collection: O(n) — faster than n individual offers

**Critical: iteration is NOT sorted.**

```java
PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(5, 1, 4, 2, 3));
System.out.println(pq);  // NOT [1, 2, 3, 4, 5] — internal heap order
// To get sorted output, poll() repeatedly
```

**Lazy deletion pattern (used in Dijkstra):**

When you can't efficiently remove/update elements, leave stale entries in the heap and skip them when polled:

```java
pq.offer(new long[]{newDist, node});
// ... later, when polling:
long[] top = pq.poll();
if (top[0] > dist[(int) top[1]]) continue;  // stale, skip
```

---

## Comparators

### Building comparators safely

**Never use subtraction.** `a - b` overflows for large values of opposite sign:

```java
// WRONG: overflows when a = Integer.MIN_VALUE, b = 1
Comparator<int[]> broken = (a, b) -> a[0] - b[0];

// RIGHT: always use compare methods
Comparator<int[]> correct = (a, b) -> Integer.compare(a[0], b[0]);
Comparator<int[]> also = Comparator.comparingInt(x -> x[0]);
```

### Multi-key comparators

```java
// Priority DESC, then createdAt ASC, then id ASC (total order)
Comparator<Task> order = Comparator
    .comparingInt(Task::priority).reversed()    // reversed() reverses ONLY priority
    .thenComparingLong(Task::createdAt)
    .thenComparing(Task::id);                   // unique tie-break for TreeSet
```

**`reversed()` reverses the ENTIRE chain built before it.** If you put it at the end, it reverses everything:

```java
// This reverses BOTH priority AND createdAt — probably not what you want
Comparator.comparingInt(Task::priority)
          .thenComparingLong(Task::createdAt)
          .reversed();

// This reverses ONLY priority
Comparator.comparing(Task::priority, Comparator.reverseOrder())
          .thenComparingLong(Task::createdAt);
```

### Comparator for Map entries

```java
// Sort entries: count DESC, then key ASC
Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
    .thenComparing(Map.Entry.comparingByKey());
```

---

## Sorting

### Object sorting is stable, primitive sorting is not

- `List.sort()`, `Collections.sort()`, `Arrays.sort(Object[])`: **stable** merge sort, O(n log n) worst case. Equal elements keep their relative order.
- `Arrays.sort(int[])`: dual-pivot quicksort, NOT stable. Fine for primitives since equal ints are indistinguishable.

### Sorting primitives in custom order

There's no `Arrays.sort(int[], Comparator)`. Options:

- Sort ascending and iterate backwards
- Box to `Integer[]` (wasteful for large arrays)
- Use a `List<Integer>` and `Collections.sort`

### Sorting 2D arrays

```java
// Sort by first column ascending
Arrays.sort(intervals, Comparator.comparingInt(iv -> iv[0]));

// Sort by first column, then by second column
Arrays.sort(intervals, Comparator.comparingInt((int[] iv) -> iv[0])
                                  .thenComparingInt(iv -> iv[1]));
```

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
