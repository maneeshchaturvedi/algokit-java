# AlgoKit Java

Battle-tested building blocks for coding interviews. Import the ones you need, understand them, use them.

## Quick Start

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>io.algokit</groupId>
    <artifactId>algokit-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

Or just clone and copy the classes you need — every function fits on one screen.

## Usage

```java
import static io.algokit.search.BinarySearch.*;
import static io.algokit.graph.Graphs.*;
import static io.algokit.window.SlidingWindow.*;

// Binary search: find the boundary
int idx = lowerBound(sortedArray, target);

// Graph: BFS shortest distances
var adj = buildUndirected(n, edges);
int[] dist = bfsDistances(adj, source);

// Sliding window: longest subarray with sum <= limit
int len = longestSumAtMost(nums, limit);
```

## Packages

| Package | What's inside |
|---------|--------------|
| `io.algokit.arrays` | `letterCounts`, `frequencies`, `groupBy`, `prefixSums`, `rangeSum`, `countSubarraysWithSum`, `packKey` |
| `io.algokit.search` | `firstTrue`, `firstTrueLong`, `lowerBound`, `upperBound`, `indexOf` |
| `io.algokit.window` | `maxSumWindow`, `longestSumAtMost`, `minSubArrayLen`, `longestAtMostKDistinct` |
| `io.algokit.stack` | `nextGreaterIndex`, `windowMin` |
| `io.algokit.list` | `ListNode`, `Lists.of()`, `toArray`, `removeAll`, `middleNode` |
| `io.algokit.tree` | `TreeNode`, traversals (recursive + iterative), `levelOrder`, `height`, `isBalanced`, `fromLevelOrder` |
| `io.algokit.graph` | `buildDirected/Undirected`, `bfsDistances`, `dfsOrder`, `hasCycle`, `topoSortKahn/Dfs`, `dijkstra`, `gridDistances`, `shortestPath`, `UnionFind` |
| `io.algokit.intervals` | `merge` (overlapping intervals) |
| `io.algokit.dp` | `coinChange`, `countGridPaths`, `runWithStack` |
| `io.algokit.backtrack` | `subsets`, `combine` |
| `io.algokit.structures` | `Versioned<V>` (time-based lookup), `LruCache<K,V>` |
| `io.algokit.text` | `tokenize`, `weightedTermFrequency`, `idf` |

## Design Principles

- **Zero dependencies** — just the JDK (17+). No Guava, no Apache Commons.
- **Every method fits on one screen** — click "go to definition" and understand it immediately.
- **Static imports friendly** — all utility methods are static on final classes.
- **Tested as documentation** — every test doubles as a usage example.

## Companion Site

Visit the [AlgoKit companion site](https://maneeshchaturvedi.github.io/algokit-java/) for:
- Pattern recognition guide (signals to technique mapping)
- Collections decision tree
- Per-function documentation with example problems
- Pitfall checklist

## Building

```bash
mvn clean test        # run all tests
mvn package           # build the jar
mvn site              # generate docs
```

## License

MIT
