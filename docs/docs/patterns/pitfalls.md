# Pitfall Checklist

This is the list of bugs that cost people interviews. Each one is silent — the code compiles, often passes simple test cases, and fails on edge cases or large inputs. Scan this list before you say "done."

---

## 1. Boxed equality: `==` vs `.equals()`

**The bug:** Comparing `Integer`, `Long`, or other boxed types with `==` instead of `.equals()`.

**Why it's subtle:** Java caches `Integer` values from -128 to 127. So `==` works for small values and breaks for large ones — which means your code passes small test cases and fails on large inputs.

```java
Integer a = 127, b = 127;
a == b;           // true — cached range, same object

Integer c = 128, d = 128;
c == d;           // FALSE — outside cache, different objects
c.equals(d);      // true ✓
c.intValue() == d.intValue();  // true ✓ — unbox then compare
```

**Where it hides:** Comparing values retrieved from a `HashMap<String, Integer>`:

```java
map.get("x") == map.get("y")     // WRONG — reference comparison
map.get("x").equals(map.get("y"))  // RIGHT — but NPE if either is null
Objects.equals(map.get("x"), map.get("y"))  // SAFEST
```

**Rule:** Never use `==` on boxed types. Use `.equals()`, or unbox to primitive first.

---

## 2. Overflow: silent wrapping of `int`

**The bug:** Arithmetic on `int` silently wraps around at 2^31 - 1 instead of throwing an error.

```java
int big = Integer.MAX_VALUE;  // 2,147,483,647
big + 1;  // -2,147,483,648 — wrapped to MIN_VALUE, silently

// Midpoint overflow
int lo = 2_000_000_000, hi = 2_100_000_000;
(lo + hi) / 2;               // NEGATIVE — overflow
lo + (hi - lo) / 2;          // CORRECT — overflow-safe ✓
```

**Common danger zones:**

- **Prefix sums:** n elements of value 10^9 → prefix sum reaches 10^18, far beyond `int`
- **Products:** `100_000 * 100_000` overflows `int`. Cast BEFORE the multiply: `(long) x * x`
- **Timestamp math in milliseconds:** Milliseconds since epoch exceed `int` range

```java
// WRONG: the multiply happens in int, THEN casts to long
long result = (long) (x * x);  // still overflows!

// RIGHT: cast one operand first, multiply happens in long
long result = (long) x * x;  // ✓
```

**Rule:** Use `long` for sums, products, timestamps, and money. Widen BEFORE the arithmetic.

---

## 3. Comparator: no subtraction, `reversed()` placement, unique tie-break

### 3a. Never subtract in a comparator

```java
// WRONG: overflows when a = Integer.MIN_VALUE, b = 1
// MIN_VALUE - 1 = MAX_VALUE → says MIN_VALUE > 1
(a, b) -> a[0] - b[0];

// RIGHT
(a, b) -> Integer.compare(a[0], b[0]);
Comparator.comparingInt(x -> x[0]);
```

### 3b. `reversed()` reverses the entire preceding chain

```java
// This reverses BOTH priority and createdAt
Comparator.comparingInt(T::priority)
          .thenComparingLong(T::createdAt)
          .reversed();  // reverses EVERYTHING before it

// To reverse only priority:
Comparator.comparing(T::priority, Comparator.reverseOrder())
          .thenComparingLong(T::createdAt);  // ✓
```

### 3c. TreeSet needs a unique tie-break

A `TreeSet` treats `compare(a, b) == 0` as "a and b are the same element" and silently drops one. If your comparator only compares by score, two different items with the same score collapse:

```java
TreeSet<Task> set = new TreeSet<>(Comparator.comparingInt(Task::score));
set.add(new Task("a", 5));
set.add(new Task("b", 5));
set.size();  // 1! "b" was silently dropped.

// FIX: end the chain with a unique field
Comparator.comparingInt(Task::score).thenComparing(Task::id);
```

### 3d. Comparators must be transitive

A comparator that isn't transitive (if a < b and b < c, then a < c) can make `sort()` throw "Comparison method violates its general contract." The subtraction bug is one cause. Another: comparing floating-point values with `==` for "equal" but a fuzzy epsilon for ordering.

---

## 4. Null unboxing: `map.get`, `floorKey`, `ceilingKey`

**The bug:** Auto-unboxing a `null` `Integer` into an `int` throws `NullPointerException`.

```java
Map<String, Integer> map = new HashMap<>();
int val = map.get("missing");  // NPE! map.get returns null, unboxing null → NPE

TreeMap<Integer, String> tree = new TreeMap<>();
int floor = tree.floorKey(5);  // NPE! No key ≤ 5, floorKey returns null
```

**Rule:** Always assign to the boxed type first and null-check:

```java
Integer val = map.get("missing");
if (val != null) { ... }

// Or use getOrDefault
int val = map.getOrDefault("missing", 0);

// For TreeMap navigation
Integer floor = tree.floorKey(5);
if (floor != null) { ... }
```

---

## 5. Mutation inside collections

**The bug:** Changing a field that affects `hashCode` or comparator ordering while the object is inside a `HashMap`, `HashSet`, `TreeSet`, or `PriorityQueue`.

**What happens:**

- In a HashMap/HashSet: the entry is stranded under its old hash. `get` can't find it, but it still takes space.
- In a TreeSet: the tree's ordering invariant is violated. Future lookups, additions, and removals may behave incorrectly.
- In a PriorityQueue: the heap property is violated. `poll` may not return the minimum.

```java
// WRONG
List<Integer> key = new ArrayList<>(List.of(1));
Set<List<Integer>> set = new HashSet<>();
set.add(key);
key.add(2);  // mutated while in the set!
set.contains(key);  // false — stranded under old hash
set.size();          // 1 — still there, just unreachable
```

**Rule:** To change an element inside a sorted/hashed collection, always remove first, change, then re-insert:

```java
ranked.remove(oldRecord);
Record updated = new Record(newScore, id);
ranked.add(updated);
byId.put(id, updated);
```

---

## 6. `remove(int)` vs `remove(Object)` on `List<Integer>`

**The bug:** `List<Integer>` has two `remove` methods:

- `remove(int index)` — removes by position
- `remove(Object o)` — removes by value

```java
List<Integer> list = new ArrayList<>(List.of(5, 6, 7));
list.remove(5);                    // IndexOutOfBoundsException! Tries to remove index 5.
list.remove(Integer.valueOf(5));   // Removes the VALUE 5 ✓
list.remove((Integer) 5);         // Also works ✓
```

**Rule:** When calling `remove` on a `List<Integer>`, always cast to `Integer` or use `Integer.valueOf()` when you mean to remove by value.

---

## 7. `String.split` regex and trailing empties

**The bug:** `split` takes a regular expression, not a literal string. And it drops trailing empty strings by default.

```java
"a|b".split("|");    // ["a", "|", "b"] — "|" is regex alternation!
"a|b".split("\\|");  // ["a", "b"] ✓

"a.b".split(".");    // [] — "." matches everything, produces only empty strings!
"a.b".split("\\.");  // ["a", "b"] ✓

"a,b,,".split(",");       // ["a", "b"] — trailing empties dropped!
"a,b,,".split(",", -1);   // ["a", "b", "", ""] — limit -1 keeps them ✓

"".split("\\s+");    // [""] — NOT empty array! One empty string.
" a b".split("\\s+");  // ["", "a", "b"] — leading separator produces leading empty string
```

**Rule:** Escape regex metacharacters (`.|*+?()[]{}\\^$`). Use `-1` limit for CSV parsing. Trim before splitting if leading/trailing whitespace exists.

---

## 8. BFS: mark visited on enqueue, not dequeue

**The bug:** Checking visited status when dequeueing instead of enqueueing. This causes the same node to be enqueued multiple times, wasting time and potentially causing incorrect distance calculations.

```java
// WRONG: marks on dequeue
while (!queue.isEmpty()) {
    int u = queue.poll();
    if (visited[u]) continue;  // may have been enqueued 100 times already
    visited[u] = true;
    for (int v : adj.get(u)) {
        queue.offer(v);  // v gets enqueued once per neighbour that sees it
    }
}

// RIGHT: marks on enqueue
dist[source] = 0;
queue.offer(source);
while (!queue.isEmpty()) {
    int u = queue.poll();
    for (int v : adj.get(u)) {
        if (dist[v] == -1) {      // first time seeing v
            dist[v] = dist[u] + 1;
            queue.offer(v);        // enqueued exactly once ✓
        }
    }
}
```

**Why it matters:** In the wrong version, a node with 1000 neighbours that all point to node X will enqueue X 1000 times. This turns O(V+E) BFS into potentially O(E^2) and produces incorrect distances if you're counting levels.

---

## 9. Backtracking: copy the path

**The bug:** Adding the live path reference to the result list instead of a copy. Since the path keeps mutating (that's how backtracking works), every entry in the result list ends up as the same empty list.

```java
// WRONG: all results point to the same (now-empty) list
out.add(path);

// RIGHT: snapshot the current state
out.add(new ArrayList<>(path));
```

**This applies to:** Subsets, combinations, permutations, path enumeration — any backtracking that accumulates results.

---

## 10. Loop bounds: `<` vs `<=`, half-open intervals

**The bug:** Off-by-one errors from confusing inclusive and exclusive bounds.

**Java conventions:**

- `substring(begin, end)` → `[begin, end)` — end is exclusive
- `subList(from, to)` → `[from, to)` — to is exclusive
- `Arrays.copyOfRange(a, from, to)` → `[from, to)` — to is exclusive
- `headMap(key)` → keys < key (exclusive by default)
- `tailMap(key)` → keys ≥ key (inclusive by default)

**Prefix sums use half-open convention:**

```java
// prefix[i] = sum of a[0..i) = sum of first i elements
// Sum of a[l..r] inclusive = prefix[r+1] - prefix[l]
```

**Binary search bounds:**

```java
// [lo, hi) — hi is exclusive, meaning "no answer found"
firstTrue(0, a.length, ...)  // returns a.length if no element qualifies
```

**Rule:** Decide on inclusive or exclusive bounds before writing the loop, and be consistent. Comment it if it's not obvious.

---

## 11. Edge cases: empty, single, equal, negative

**The bug:** Code that works for typical inputs but crashes or gives wrong answers on edge cases.

**Always test:**

- **Empty input:** `[]`, `""`, `null`, `n = 0`
- **Single element:** `[5]`, `"x"`, `n = 1`
- **All equal values:** `[3, 3, 3, 3]`
- **Negative numbers:** especially in sum/product calculations
- **Minimum/maximum values:** `Integer.MIN_VALUE`, `Integer.MAX_VALUE`
- **Already sorted / reverse sorted input**

**Common edge case bugs:**

```java
// Bug: division by zero when array is empty
int avg = sum / nums.length;

// Bug: middleNode returns null for empty list
ListNode mid = middleNode(null);  // returns null, fine — but what if you call mid.val?

// Bug: binary search on empty array
lowerBound(new int[]{}, 5);  // returns 0, which is == a.length — handle this case

// Bug: Math.abs(Integer.MIN_VALUE) is still negative!
Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE;  // true! There's no positive counterpart.
```

---

## 12. Missing requirements

**The bug:** Solving the problem correctly but forgetting one of the requirements stated in the problem.

**Prevention routine:**

1. Before coding, write each requirement as a numbered comment: `// R1: ...`, `// R2: ...`
2. After coding, tick each one off by tracing through the code
3. Trace through the given example by hand
4. Test the edge cases from item 11

This is especially important for multi-part problems ("return the indices", "handle duplicates", "return results in sorted order", "the answer should be 1-indexed").

---

## Pre-Submit Scan (30 seconds)

Before telling the interviewer you're done, scan your code for these in order:

- [ ] All boxed comparisons use `.equals()`, never `==`
- [ ] `long` wherever overflow is possible (sums, products, timestamps)
- [ ] Comparator uses `Integer.compare`, not subtraction
- [ ] `reversed()` is placed correctly (per-key, not end-of-chain)
- [ ] TreeSet comparator ends with a unique tie-break
- [ ] `map.get()` and `floorKey()` results are null-checked before unboxing
- [ ] No object is mutated while inside a HashMap/TreeSet/PriorityQueue
- [ ] BFS marks on enqueue
- [ ] Backtracking copies the path
- [ ] Empty input, single element, and negative values are handled
- [ ] Every stated requirement is satisfied
- [ ] The given example is traced through by hand
