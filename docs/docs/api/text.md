# Text Processing API Reference

Package: `io.algokit.text`

Text utilities for tokenisation, term frequency weighting, and TF-IDF scoring. All methods are
static utilities on `Text`.

---

## Why text processing appears in interviews

Text processing problems surface in two contexts:

1. **Direct string manipulation** — anagrams, word frequency, pattern matching.
2. **Search and ranking systems** — "design a document search engine" or "rank results by
   relevance." These are more common in system design rounds but occasionally appear as coding
   problems when the interviewer wants to see whether you know standard IR (information
   retrieval) concepts.

`Text` provides a minimal, self-contained TF-IDF pipeline. Understanding it prepares you for
both direct text problems and the "design" questions.

---

## tokenize

```java
public static List<String> tokenize(String text)
```

**What it does.** Converts raw text into a canonical list of meaningful tokens:

1. Lowercases the entire string (locale-safe via `Locale.ROOT`).
2. Splits on any run of non-alphanumeric characters (`[^a-z0-9]+`), treating punctuation,
   spaces, and symbols as delimiters.
3. Drops empty strings produced by leading/trailing delimiters.
4. Drops stop words — common words that carry no discriminative signal.

**Stop words.** The built-in list covers the most frequent English function words:

```
a, an, the, and, or, but, in, on, at, to, for, of, with, by, is, it, this, that, are, was
```

Stop word removal is a classic space-versus-precision tradeoff: removing them reduces index
size and noise; leaving them in allows phrase queries like "to be or not to be."

**Complexity.** O(n) where n is the length of the input string. Regex splitting is O(n);
stream filtering and `toList()` are O(k) where k is the token count, k ≤ n.

**Usage**

```java
Text.tokenize("The quick brown fox jumps over the lazy dog");
// ["quick", "brown", "fox", "jumps", "over", "lazy", "dog"]
// "the" removed (stop word), "The" lowercased to "the" then removed

Text.tokenize("Hello, world! Hello again.");
// ["hello", "world", "hello", "again"]
// comma and exclamation mark are delimiters; duplicates kept (frequency matters)
```

**Related LeetCode problems.** 819 (Most Common Word — stop word removal + frequency count),
884 (Uncommon Words from Two Sentences), 1078 (Occurrences After Bigram — bigram extraction,
same tokenisation step), 1804 (Implement Trie II — operates on tokenised input).

**Gotcha.** `Locale.ROOT` is used instead of `Locale.getDefault()` to avoid locale-specific
casing (e.g. Turkish `i` → `İ` with the default locale). Always use `Locale.ROOT` or
`Locale.ENGLISH` in competitive programming and tests.

---

## weightedTermFrequency

```java
public static Map<String, Integer> weightedTermFrequency(String title, String body)
// title tokens get weight 2, body tokens get weight 1.
```

**What it does.** Builds a term frequency map for a document that has two fields with different
importance. Title terms are counted as if they appeared twice because title words are stronger
signals of topic than body words.

**How it works.** Tokenise both fields separately, then merge into a single `HashMap` using
`Map.merge`:

```java
for (String w : tokenize(title)) tf.merge(w, 2, Integer::sum);
for (String w : tokenize(body))  tf.merge(w, 1, Integer::sum);
```

`merge(key, value, remappingFn)` inserts `value` if the key is absent; otherwise applies
`remappingFn(existing, value)`. Using `Integer::sum` accumulates counts correctly.

**Usage**

```java
Map<String, Integer> tf = Text.weightedTermFrequency(
    "Java sorting algorithms",
    "This guide covers sorting in Java including quicksort and mergesort"
);
// "java"    → 2 (title) + 1 (body) = 3
// "sorting" → 2 (title) + 1 (body) = 3
// "algorithms" → 2 (title only)
// "quicksort"  → 1 (body only)
```

**Complexity.** O(|title| + |body|) time and O(unique tokens) space.

**Related LeetCode problems.** 347 (Top K Frequent Words — frequency map + heap/sort), 692
(Top K Frequent Words), 1865 (Finding Pairs with a Certain Sum — frequency map lookups).

**Variant — unweighted TF.** Call `tokenize` once on the full text and count with frequency 1:
```java
for (String w : tokenize(text)) tf.merge(w, 1, Integer::sum);
```

---

## idf

```java
public static double idf(int totalDocuments, int docFrequency)
// Returns log(1 + N / df)
```

**What it computes.** IDF (Inverse Document Frequency) measures how rare a term is across a
corpus. A term that appears in every document has `idf ≈ log(2)` (low signal). A term that
appears in only one document has `idf ≈ log(1 + N)` (high signal).

**The formula.** This implementation uses the smoothed IDF variant:

```
idf(t) = log(1 + N / df(t))
```

where N = total documents and df(t) = number of documents containing term t. Adding 1 inside
the log prevents division-by-zero and smooths the curve for very common terms.

**TF-IDF score.** The full relevance score for a term in a document is:

```
score(t, d) = tf(t, d) × idf(t)
```

where `tf(t, d)` comes from `weightedTermFrequency`. To score a document against a query:

```java
double score(Map<String, Integer> docTf, List<String> query,
             Map<String, Integer> docFrequency, int totalDocs) {
    double s = 0;
    for (String term : query) {
        int tf = docTf.getOrDefault(term, 0);
        int df = docFrequency.getOrDefault(term, 1);
        s += tf * Text.idf(totalDocs, df);
    }
    return s;
}
```

**Complexity.** O(1) per call (a single `Math.log` and division).

**Usage**

```java
Text.idf(1000, 1);    // log(1001) ≈ 6.91 — very rare term
Text.idf(1000, 500);  // log(3)    ≈ 1.10 — appears in half the corpus
Text.idf(1000, 1000); // log(2)    ≈ 0.69 — appears in every document
```

**Related LeetCode problems.** No direct LeetCode match, but TF-IDF knowledge is expected for:
- System design: "Design a web search engine" or "Design autocomplete"
- 692 (Top K Frequent Words — TF without IDF)
- Any problem asking you to "rank documents by relevance"

---

## Putting it together: a minimal search engine

The three functions compose into a full document indexing and ranking pipeline.

### Indexing (offline, run once per document)

```java
// 1. Build the TF map for each document
Map<String, Integer> tf = Text.weightedTermFrequency(doc.title, doc.body);

// 2. Update the inverted index: term → set of document IDs
for (String term : tf.keySet()) {
    invertedIndex.computeIfAbsent(term, k -> new HashSet<>()).add(docId);
}
```

### Querying (online, per query)

```java
List<String> queryTerms = Text.tokenize(query);

// 3. Retrieve candidate documents (union of postings lists)
Set<Integer> candidates = new HashSet<>();
for (String term : queryTerms) {
    candidates.addAll(invertedIndex.getOrDefault(term, Set.of()));
}

// 4. Score and rank candidates
candidates.stream()
    .sorted(Comparator.comparingDouble(id -> -score(tfMap.get(id), queryTerms)))
    .limit(10)
    .forEach(System.out::println);
```

**The inverted index.** An inverted index maps each term to the list (or set) of documents
containing it. It is the core data structure of every search engine. Without it, answering a
query requires scanning all documents — O(N × avg doc length). With it, retrieval is O(sum of
posting list sizes for query terms), typically orders of magnitude faster.

**Complexity of full pipeline.**

| Step                     | Time               | Space               |
|--------------------------|--------------------|---------------------|
| Index all documents      | O(total tokens)    | O(total tokens)     |
| Tokenize query           | O(|query|)         | O(tokens in query)  |
| Retrieve candidates      | O(sum posting lengths) | O(candidates)  |
| Score + sort candidates  | O(C × Q + C log C) | O(C)               |

where C = candidate count, Q = query term count.

---

## Text processing patterns in interviews

| Problem type                               | Key technique                          |
|--------------------------------------------|----------------------------------------|
| Word frequency / most common word          | `tokenize` + `HashMap` + heap/sort    |
| Stop word removal                          | `Set.of(...)` + filter                |
| Document search / relevance ranking        | TF-IDF + inverted index               |
| Anagram detection                          | Sort chars or `int[26]` frequency     |
| Autocomplete / prefix search               | Trie                                  |
| Fuzzy matching / spell check               | Edit distance (DP)                    |
| Streaming word count                       | Sliding window + `HashMap`             |

**Gotcha — case and locale.** Always normalise case before comparing. Use `Locale.ROOT`
in production code. In a two-hour interview, mention the locale issue even if you don't
implement it — it signals production awareness.

**Gotcha — splitting.** `String.split(" ")` splits on single spaces only. `split("\\s+")` or
the `[^a-z0-9]+` pattern used here handle runs of whitespace and mixed punctuation correctly.
`"hello  world".split(" ")` yields `["hello", "", "world"]` with an empty string in the middle.
