# Data Structures API Reference

Package: `io.algokit.structures`

Purpose-built data structures for two recurring interview patterns: time-versioned key-value
stores and LRU caches. Both are thin, focused wrappers around JDK collections.

---

## Versioned\<V\>

```java
public final class Versioned<V>

public void put(String key, long time, V value)
public V    get(String key, long time)
// get returns the value of the latest write at or before `time`, or null.
```

### What problem does it solve?

A normal `HashMap` stores only one value per key. `Versioned` stores a *history* of values per
key, indexed by a timestamp, and answers queries of the form "what was the value of `key` at
time `t`?" — returning the most recently written value at or before `t`.

This is the "time-based key-value store" pattern that appears directly as LeetCode 981.

### Internal representation

```
history: HashMap<String, TreeMap<Long, V>>
           key  →  { timestamp → value }
```

Each key has its own `TreeMap` of `(timestamp → value)` pairs. `TreeMap` is a sorted map
backed by a red-black tree. Its entries are always ordered by timestamp, enabling efficient
range queries.

### The `floorEntry` pattern

`TreeMap.floorEntry(t)` returns the entry with the greatest key less than or equal to `t`, or
`null` if no such entry exists. This is the single operation that implements the entire "latest
version at or before time t" query:

```
put("color", 1, "red")
put("color", 3, "blue")
put("color", 7, "green")

get("color", 5) → floorEntry(5) → entry(3, "blue") → "blue"
get("color", 1) → floorEntry(1) → entry(1, "red")  → "red"
get("color", 0) → floorEntry(0) → null              → null
```

`TreeMap` also provides `ceilingEntry`, `higherEntry`, `lowerEntry`, `subMap`, and
`headMap`/`tailMap`. When you see an interview problem asking for "nearest value ≤ x" or
"nearest value ≥ x", a `TreeMap` is usually the right structure.

### Complexity

| Operation | Time complexity |
|-----------|-----------------|
| `put`     | O(log k) where k = number of timestamps for that key |
| `get`     | O(log k) |
| Space     | O(total number of writes) |

### Usage

```java
Versioned<String> store = new Versioned<>();
store.put("theme", 100L, "light");
store.put("theme", 500L, "dark");

store.get("theme", 50L);  // null   — no write before time 50
store.get("theme", 100L); // "light"
store.get("theme", 300L); // "light" — floor of 300 is 100
store.get("theme", 500L); // "dark"
store.get("theme", 999L); // "dark"  — most recent write
```

### Related LeetCode problems

- 981 (Time Based Key-Value Store — exact match for this data structure)
- 715 (Range Module — `TreeMap` range operations)
- 729 (My Calendar I — `TreeMap.floorKey` to check for overlap)
- 732 (My Calendar III — `TreeMap` sweep line)
- 1244 (Design A Leaderboard — sorted map)

### When to use TreeMap vs HashMap

| Need                                          | Use            |
|-----------------------------------------------|----------------|
| O(1) average get/put, no ordering needed      | `HashMap`      |
| Sorted iteration, floor/ceiling queries       | `TreeMap`      |
| Timestamp-versioned values                    | `TreeMap` per key |
| Range existence queries ("any write in [a,b]?")| `TreeMap.subMap` |

---

## LruCache\<K, V\>

```java
public class LruCache<K, V> extends LinkedHashMap<K, V>

public LruCache(int capacity)
// Inherits: get(K), put(K, V), containsKey(K), size(), entrySet(), ...
```

### What problem does it solve?

An LRU (Least Recently Used) cache evicts the entry that was accessed least recently when
capacity is exceeded. It is a O(1) key-value store with automatic eviction. LeetCode 146 asks
you to implement one from scratch; this implementation shows the JDK shortcut.

### The LinkedHashMap access-order trick

`LinkedHashMap` maintains a doubly-linked list of entries in either *insertion order* (default)
or *access order* (set by the third constructor parameter).

With access order (`true`):
- Every `get` and `put` moves the accessed entry to the **tail** of the linked list.
- The **head** of the list is therefore always the least recently used entry.

`removeEldestEntry` is called by `LinkedHashMap` after every `put`. Returning `true` causes the
head entry (the LRU entry) to be removed automatically.

```java
super(capacity, 0.75f, true);  // accessOrder = true
```

The three constructor arguments are: initial capacity, load factor, and access order.

### How the constructor works

```java
public LruCache(int capacity) {
    super(capacity, 0.75f, true);   // access-order linked map
    this.capacity = capacity;
}

@Override
protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > capacity;       // evict LRU when over capacity
}
```

That is the complete implementation. The JDK `LinkedHashMap` provides all the O(1) mechanics:
- `get` / `put` — O(1) average (hash table)
- Move-to-tail on access — O(1) (doubly-linked list pointer surgery)
- Eviction of head — O(1) (unlink head node)

### Usage

```java
LruCache<Integer, String> cache = new LruCache<>(3);
cache.put(1, "a");
cache.put(2, "b");
cache.put(3, "c");
cache.get(1);         // access 1 → moves to tail; LRU order: 2, 3, 1
cache.put(4, "d");    // over capacity → evict LRU (key 2)
cache.containsKey(2); // false — evicted
cache.containsKey(1); // true
```

### Complexity

| Operation | Time        | Reason                                 |
|-----------|-------------|----------------------------------------|
| `get`     | O(1) average | Hash lookup + linked-list pointer move |
| `put`     | O(1) average | Hash insert + eviction if needed        |
| Eviction  | O(1)         | Unlink head of doubly-linked list       |
| Space     | O(capacity)  | At most `capacity` entries retained     |

### Related LeetCode problems

- 146 (LRU Cache — implement from scratch; this shows the JDK shortcut)
- 460 (LFU Cache — evicts least *frequently* used; requires two HashMaps + doubly-linked lists
  per frequency bucket; `LinkedHashMap` alone does not suffice)
- 432 (All O`one Data Structure — O(1) get-max/get-min; doubly-linked list of buckets)

### LRU from scratch vs LinkedHashMap

In an interview, the canonical "from scratch" LRU solution uses:
- A `HashMap<K, Node>` for O(1) key lookup.
- A doubly-linked list (sentinel head + sentinel tail) for O(1) move-to-tail and O(1)
  remove-from-head.

`LruCache` here delegates both responsibilities to `LinkedHashMap`. The interviewer may ask you
to explain the underlying mechanics — knowing that `LinkedHashMap` maintains a doubly-linked
list internally is the key insight.

### LruCache vs Versioned: choosing the right structure

| Requirement                                        | Use           |
|----------------------------------------------------|---------------|
| "Most recent value per key" — one value per key    | `HashMap`     |
| "Value at time t" — history of writes per key      | `Versioned`   |
| "Bounded cache with eviction on access"            | `LruCache`    |
| "Cache with frequency-based eviction"              | LFU (manual)  |
| "Sorted map with range queries"                    | `TreeMap`     |
