# Arrays & Strings

**Package:** `io.algokit.arrays`  
**Source:** [`Arrays2.java`](https://github.com/maneeshchaturvedi/algokit-java/blob/main/src/main/java/io/algokit/arrays/Arrays2.java)

This class provides the counting, grouping, and prefix-sum primitives that appear in the majority of array and string interview problems. The functions are stateless and allocation-minimal wherever possible — fixed-size `int[]` arrays for character counts, `long` arithmetic to avoid overflow in sums.

---

## `letterCounts`

### Signature
```java
public static int[] letterCounts(String s)
```

### How and why it works
Maps each character `c` to a bucket via `c - 'a'`, producing a 26-element array indexed 0–25. Because the alphabet is fixed-size, the result array is O(1) in space regardless of the input length and can be compared element-by-element in O(1) time, making it the fastest way to test anagram equivalence. The single-pass traversal means the work is proportional to the string length and nothing else.

### Complexity
- **Time:** O(n) where n = `s.length()`
- **Space:** O(1) — always 26 ints

### Usage example
```java
// Anagram check
int[] a = Arrays2.letterCounts("listen");
int[] b = Arrays2.letterCounts("silent");
boolean isAnagram = Arrays.equals(a, b); // true

// Character difference between two strings
int[] freq = Arrays2.letterCounts("aabbcc");
freq[0]; // 2  ('a' -> index 0)
freq[1]; // 2  ('b' -> index 1)
```

### Related LeetCode problems
- **242** – Valid Anagram
- **438** – Find All Anagrams in a String (combine with sliding window)
- **567** – Permutation in String
- **49**  – Group Anagrams (use the count array as a map key)

### Common variants
- For Unicode or mixed-case input, use `asciiCounts` or `frequencies` instead.
- To compare two windows incrementally (as the window slides), maintain a single diff count rather than rebuilding the array each step.
- Encoding the 26-element array as a `String` (e.g., `Arrays.toString(counts)`) lets it serve as a `HashMap` key for grouping anagrams.

### Gotchas
- Assumes strictly lowercase a–z. Any character outside that range will produce a negative index or an out-of-bounds exception. Use `Character.toLowerCase` and a bounds check if the input is untrusted.
- The returned array is mutable — callers that store it must copy if they intend to modify it later.

---

## `asciiCounts`

### Signature
```java
public static int[] asciiCounts(String s)
```

### How and why it works
Identical logic to `letterCounts` but with a 128-element array covering all 7-bit ASCII characters (indices 0–127). Using `s.charAt(i)` directly as the index eliminates any arithmetic, making this marginally faster for ASCII-clean inputs while handling digits, punctuation, and uppercase characters without extra preprocessing.

### Complexity
- **Time:** O(n)
- **Space:** O(1) — always 128 ints

### Usage example
```java
int[] counts = Arrays2.asciiCounts("Hello, World!");
int spaces  = counts[' '];  // 1
int uppH    = counts['H'];  // 1
int lowO    = counts['o'];  // 2
```

### Related LeetCode problems
- **387** – First Unique Character in a String
- **383** – Ransom Note
- **791** – Custom Sort String

### Common variants
- Extend to 256 for extended ASCII / Latin-1.
- For full Unicode, fall back to `frequencies`.

### Gotchas
- Characters with code points above 127 (accented letters, emoji, CJK) will throw `ArrayIndexOutOfBoundsException`. Always validate the input character set or use `frequencies` for general text.

---

## `frequencies`

### Signature
```java
public static <T> Map<T, Integer> frequencies(Iterable<T> items)
```

### How and why it works
Iterates once over any `Iterable`, using `Map.merge(key, 1, Integer::sum)` to increment counts. `merge` is the idiomatic Java one-liner for this operation: if the key is absent it inserts `1`; if present it applies `Integer::sum` to the old and new values. This avoids a verbose `getOrDefault` + `put` pattern. The generic type parameter makes it equally useful for characters, words, custom objects, or enum values.

### Complexity
- **Time:** O(n) average — each `merge` is O(1) amortized for `HashMap`
- **Space:** O(k) where k = number of distinct items

### Usage example
```java
// Word frequency
List<String> words = List.of("the", "cat", "sat", "on", "the", "mat");
Map<String, Integer> freq = Arrays2.frequencies(words);
freq.get("the"); // 2

// Character frequency of a string
Map<Character, Integer> charFreq = Arrays2.frequencies(
    "banana".chars().mapToObj(c -> (char) c).toList()
);
charFreq.get('a'); // 3
```

### Related LeetCode problems
- **347** – Top K Frequent Elements
- **451** – Sort Characters By Frequency
- **692** – Top K Frequent Words
- **1207** – Unique Number of Occurrences

### Common variants
- Use `frequencies` on a `List<Character>` rather than a raw array when working with non-ASCII strings.
- Pair with `Collections.max(freq.entrySet(), Map.Entry.comparingByValue())` for the most-frequent element.
- A `TreeMap` instead of `HashMap` gives sorted iteration over keys at the cost of O(log k) per insert.

### Gotchas
- `Map.merge` with a boxing-based sum can allocate many `Integer` objects at high call rates. For hot paths, prefer the array-based variants above.
- The returned `HashMap` has no guaranteed iteration order. Use a `LinkedHashMap` if insertion order matters.

---

## `groupBy`

### Signature
```java
public static <K, V> Map<K, List<V>> groupBy(List<V> items, Function<V, K> keyOf)
```

### How and why it works
Applies a classifier function to each item and routes it into the matching bucket via `computeIfAbsent`, which creates the `ArrayList` lazily on the first insertion for each key. Items within each group appear in the same relative order as in the input, which is important when the problem requires stable grouping (e.g., group strings by sorted characters to find anagram families). This is the functional equivalent of `Stream.collect(Collectors.groupingBy(...))` but without the stream overhead.

### Complexity
- **Time:** O(n · f) where f = cost of `keyOf`; typically O(n) or O(n log n) for sort-based keys
- **Space:** O(n) total across all groups

### Usage example
```java
// Group anagrams (LeetCode 49)
List<String> words = List.of("eat", "tea", "tan", "ate", "nat", "bat");
Map<String, List<String>> groups = Arrays2.groupBy(
    words,
    w -> { char[] c = w.toCharArray(); Arrays.sort(c); return new String(c); }
);
// groups: {"aet"->["eat","tea","ate"], "ant"->["tan","nat"], "abt"->["bat"]}

// Group numbers by parity
Map<Integer, List<Integer>> byParity = Arrays2.groupBy(
    List.of(1, 2, 3, 4, 5),
    n -> n % 2
);
```

### Related LeetCode problems
- **49**  – Group Anagrams
- **1636** – Sort Array by Increasing Frequency (group then sort groups)
- **2150** – Find All Lonely Numbers in the Array

### Common variants
- Compose `keyOf` functions: sort characters, then lowercase, for case-insensitive anagram grouping.
- Follow `groupBy` with a filter to keep only groups of a specific size.

### Gotchas
- The `keyOf` function must produce keys that implement `equals` and `hashCode` correctly. Primitive arrays (`char[]`, `int[]`) do not — convert to `String` or wrap in a `List` if using them as keys.
- Each group `List` is a live `ArrayList`; callers that intend to consume the map concurrently should not modify the lists.

---

## `prefixSums`

### Signature
```java
public static long[] prefixSums(int[] a)
```

### How and why it works
Builds a sentinel-prefixed cumulative sum array of length `n + 1` where `prefix[0] = 0` and `prefix[i] = a[0] + ... + a[i-1]`. The off-by-one sentinel eliminates all index-adjustment arithmetic at query time: the sum of any subarray `a[l..r]` is exactly `prefix[r+1] - prefix[l]`. Using `long` for the output prevents silent overflow when summing large arrays of `int` values — a common source of wrong answers. This data structure turns any range-sum query from O(n) to O(1) after O(n) preprocessing.

### Complexity
- **Time:** O(n) to build
- **Space:** O(n) for the prefix array

### Usage example
```java
int[] a = {3, -1, 4, 1, -5, 9};
long[] p = Arrays2.prefixSums(a);

// Sum of a[1..4] = -1 + 4 + 1 + (-5) = -1
long s = Arrays2.rangeSum(p, 1, 4); // -1

// Count subarrays with zero sum
Map<Long, Integer> seen = new HashMap<>();
seen.put(0L, 1);
int count = 0;
for (int i = 0; i < p.length - 1; i++) {
    count += seen.getOrDefault(p[i + 1], 0);
    seen.merge(p[i + 1], 1, Integer::sum);
}
```

### Related LeetCode problems
- **303** – Range Sum Query - Immutable
- **304** – Range Sum Query 2D - Immutable (extend to 2D)
- **560** – Subarray Sum Equals K (combine with HashMap)
- **974** – Subarray Sums Divisible by K
- **1480** – Running Sum of 1d Array

### Common variants
- **2D prefix sums:** `p[i][j] = sum of rectangle (0,0)→(i-1,j-1)`. Rectangle sum = `p[r2+1][c2+1] - p[r1][c2+1] - p[r2+1][c1] + p[r1][c1]`.
- **Difference arrays:** The inverse operation — use when range-update queries are more common than range reads.
- **XOR prefix:** Replace `+` with `^` for range XOR queries.

### Gotchas
- The output array has length `n + 1`, not `n`. Passing `prefix[n]` (not `prefix[n-1]`) is the full-array sum.
- `prefixSums` does not modify the input array; the caller owns both arrays separately.
- Do not pass the `prefix` array directly to `rangeSum` with indices intended for the original array — `l` and `r` are indices into `a`, not `prefix`.

---

## `rangeSum`

### Signature
```java
public static long rangeSum(long[] prefix, int l, int r)
```

### How and why it works
A one-line O(1) query that returns `prefix[r+1] - prefix[l]`, giving the sum of the original array from index `l` to `r` inclusive. The design is intentionally minimal — the arithmetic is trivial once you understand the sentinel convention, but having a named function eliminates off-by-one errors at call sites and makes the intent self-documenting.

### Complexity
- **Time:** O(1)
- **Space:** O(1)

### Usage example
```java
int[] heights = {1, 3, 2, 5, 4};
long[] p = Arrays2.prefixSums(heights);
long totalHeight = Arrays2.rangeSum(p, 0, 4);  // 15
long midSection  = Arrays2.rangeSum(p, 1, 3);  // 10
```

### Related LeetCode problems
- **303** – Range Sum Query - Immutable
- **1524** – Number of Sub-arrays With Odd Sum

### Gotchas
- `l` and `r` are both inclusive and must satisfy `0 <= l <= r < a.length` (where `a` is the original array that produced `prefix`). Passing `r = prefix.length - 1` will throw.

---

## `hasPairWithDifference`

### Signature
```java
public static boolean hasPairWithDifference(int[] a, int diff)
```

### How and why it works
For each element `x`, the question "is there already a seen element `y` such that `|x - y| == diff`?" has exactly two cases: `y = x - diff` or `y = x + diff`. Both lookups are O(1) in a `HashSet`. By checking before inserting `x`, the function handles the i ≠ j constraint automatically (you cannot match `x` against itself). Storing as `long` prevents overflow when `diff` is close to `Integer.MAX_VALUE`. The single-pass nature means the answer is found as early as possible.

### Complexity
- **Time:** O(n)
- **Space:** O(n)

### Usage example
```java
Arrays2.hasPairWithDifference(new int[]{1, 5, 3, 4, 2}, 2); // true  (1,3 or 3,5 etc.)
Arrays2.hasPairWithDifference(new int[]{1, 5, 3, 4, 2}, 8); // false

// With diff == 0: finds duplicate elements
Arrays2.hasPairWithDifference(new int[]{1, 2, 3, 1}, 0); // true
```

### Related LeetCode problems
- **532** – K-diff Pairs in an Array
- **1**   – Two Sum (sum variant: look for `target - x` instead of `x ± diff`)
- **219** – Contains Duplicate II

### Common variants
- For the sum variant (Two Sum), replace the two `contains` checks with a single `contains(target - x)` check before inserting.
- To count all such pairs rather than just detecting one, switch `HashSet` to a frequency map and iterate with deduplication logic.

### Gotchas
- When `diff == 0`, both checks reduce to `seen.contains((long) x)`, correctly finding the first duplicate.
- Does not find pairs where the same index is used twice — this is the correct behavior for "pair (i, j) with i ≠ j".
- The `(b & 0xFFFFFFFFL)` idiom used in `packKey` is not needed here because subtraction/addition on `long`-cast values is naturally correct.

---

## `countSubarraysWithSum`

### Signature
```java
public static int countSubarraysWithSum(int[] a, int k)
```

### How and why it works
Uses the prefix-sum identity: a subarray `a[l..r]` sums to `k` if and only if `prefix[r+1] - prefix[l] == k`, i.e., `prefix[l] == prefix[r+1] - k`. As we scan right and build the running prefix sum, we check how many previous prefix values equal `currentPrefix - k` using a frequency map. The sentinel `seen.put(0L, 1)` accounts for subarrays starting at index 0. This approach works with negative numbers, which eliminates the use of a two-pointer window (which requires non-negative values for monotone invariance).

### Complexity
- **Time:** O(n)
- **Space:** O(n) for the prefix-sum frequency map

### Usage example
```java
// [1,1,1] with k=2: subarrays [1,1](0..1) and [1,1](1..2) => 2
Arrays2.countSubarraysWithSum(new int[]{1, 1, 1}, 2); // 2

// Works with negatives
Arrays2.countSubarraysWithSum(new int[]{1, -1, 1}, 1); // 3
// subarrays: [1], [1,-1,1], [1]
```

### Related LeetCode problems
- **560** – Subarray Sum Equals K (this exact problem)
- **974** – Subarray Sums Divisible by K (replace map key with `prefix % k`)
- **525** – Contiguous Array (map `0` to `-1`, then find longest subarray with sum 0)
- **930** – Binary Subarrays With Sum

### Common variants
- **Divisible by k:** store `prefix % k` as the map key (handle negative modulo with `((prefix % k) + k) % k`).
- **Longest subarray with sum k:** store the first index where each prefix value is seen instead of a count.
- **At most k:** compute `atMost(k) - atMost(k-1)` for exactly-k variants when values are non-negative.

### Gotchas
- The function counts ordered subarrays, not subsets. `[1,-1,1]` with `k=1` returns 3, not 2.
- The `seen` map key uses `Long` to avoid overflow on large arrays with large values. Using `int` prefix sums would silently wrap.

---

## `packKey`

### Signature
```java
public static long packKey(int a, int b)
```

### How and why it works
Packs two 32-bit integers into a single 64-bit `long` by shifting `a` into the upper 32 bits (`a << 32`) and masking `b` with `0xFFFFFFFFL` before OR-ing it into the lower 32 bits. The mask is critical: without it, a negative `b` would sign-extend to 64 bits and corrupt the upper half. The result fits in a `long` (a primitive), so it can be used as a `HashMap<Long, ...>` key with no object allocation and a well-defined hash — dramatically cheaper than `Map<List<Integer>, ...>` or a custom record as a composite key.

### Complexity
- **Time:** O(1)
- **Space:** O(1)

### Usage example
```java
// 2D grid cell → Long key
Map<Long, Integer> visited = new HashMap<>();
int row = 3, col = -5;
long key = Arrays2.packKey(row, col);
visited.put(key, 1);

// Unpack if needed
int unpackedA = (int)(key >> 32);
int unpackedB = (int)(key & 0xFFFFFFFFL);
```

### Related LeetCode problems
- **36**  – Valid Sudoku (pack row/col/box index pairs)
- **1074** – Number of Submatrices That Sum to Target
- **2316** – Count Unreachable Pairs of Nodes (encode edge pairs)
- Any BFS/DFS on a 2D grid where you need a visited set without a `boolean[][]`

### Common variants
- Pack three `short` values into a `long` for 3D coordinates: `((long)a << 32) | ((long)b << 16) | (c & 0xFFFFL)`.
- For small non-negative values (e.g., grid size ≤ 1000), a simpler `a * 1001 + b` multiplication works without bitwise ops and may be more readable.

### Gotchas
- Both inputs must fit in 32 bits — this is always true for Java `int`, but if you pass values derived from `long` arithmetic, truncation may occur silently.
- Unpacking requires `(int)(key >> 32)` for `a` and `(int)(key & 0xFFFFFFFFL)` for `b`. Forgetting the mask on unpack gives the wrong value for negative `b`.
- `packKey` produces a unique key only when the pair `(a, b)` uniquely identifies the entry — it does not hash; collisions are impossible by construction.
