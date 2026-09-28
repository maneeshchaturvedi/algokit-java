# Getting Started

## Installation

### Option 1: Maven

```xml
<dependency>
    <groupId>io.algokit</groupId>
    <artifactId>algokit-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Option 2: Gradle

```groovy
implementation 'io.algokit:algokit-java:0.1.0'
```

### Option 3: Copy what you need

Clone the repo and copy individual files into your project. Every class is self-contained with no internal dependencies between packages.

```bash
git clone https://github.com/maneeshchaturvedi/algokit-java.git
```

## Your first five minutes

AlgoKit is designed around **static imports**. Each package is a toolbox — import the functions you need and use them directly.

```java
import static io.algokit.search.BinarySearch.*;
import static io.algokit.arrays.Arrays2.*;
import static io.algokit.window.SlidingWindow.*;
import static io.algokit.graph.Graphs.*;

public class Solution {
    public int[] twoSum(int[] nums, int target) {
        // Use frequencies() to count occurrences
        // Use lowerBound() for sorted array searches
        // Use longestSumAtMost() for window problems
        // Use bfsDistances() for graph problems
    }
}
```

## How to use AlgoKit in practice

### During daily practice

Import AlgoKit into your practice project. When you encounter a problem that needs a sliding window, binary search, or graph traversal, use the AlgoKit template as your starting point and focus your energy on the problem-specific logic.

```java
// Instead of rewriting binary search from scratch every time:
int idx = firstTrue(0, n, i -> canShipInDays(weights, i, days));

// Instead of rebuilding a graph from scratch:
var adj = buildUndirected(n, edges);
int[] dist = bfsDistances(adj, 0);
```

### Before an interview

Read the source code of every function you plan to use. AlgoKit functions are designed to be understood — every one fits on a single screen. The goal is not to memorise the code but to internalise the pattern so you can write it from memory under pressure.

### During an interview

You won't have AlgoKit available. But because you've practised with clean, well-named templates, the patterns will be muscle memory. The naming conventions in AlgoKit match what interviewers expect to see:

- `firstTrue` for binary search (not a bespoke loop)
- `UnionFind` with `find`, `union`, `connected` (standard API)
- Level-by-level BFS with `queue.size()` freeze (standard idiom)

## Package overview

| Package | Purpose | Key entry points |
|---------|---------|-----------------|
| `arrays` | Counting, grouping, prefix sums | `Arrays2.frequencies()`, `prefixSums()`, `groupBy()` |
| `search` | Binary search on predicates | `BinarySearch.firstTrue()`, `lowerBound()`, `upperBound()` |
| `window` | Sliding window templates | `SlidingWindow.maxSumWindow()`, `longestSumAtMost()` |
| `stack` | Monotonic stack and deque | `MonotonicStack.nextGreaterIndex()`, `windowMin()` |
| `list` | Linked list nodes and utilities | `ListNode`, `Lists.of()`, `middleNode()` |
| `tree` | Binary tree traversals and checks | `Trees.levelOrder()`, `isBalanced()`, `fromLevelOrder()` |
| `graph` | BFS, DFS, Dijkstra, topo sort, union-find | `Graphs.bfsDistances()`, `dijkstra()`, `UnionFind` |
| `intervals` | Interval merging | `Intervals.merge()` |
| `dp` | DP templates and utilities | `DynamicProgramming.coinChange()`, `runWithStack()` |
| `backtrack` | Subset and combination generation | `Backtracking.subsets()`, `combine()` |
| `structures` | Versioned store, LRU cache | `Versioned`, `LruCache` |
| `text` | Tokenization and TF-IDF | `Text.tokenize()`, `idf()` |

## Requirements

- Java 17 or later
- No other dependencies
