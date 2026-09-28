# Graph API Reference

Package: `io.algokit.graph`

Graph algorithms covering adjacency-list construction, BFS, DFS, cycle detection, topological
sort, Dijkstra, grid BFS, and disjoint-set union. Every function is a static utility on
`Graphs`; `UnionFind` is a standalone class.

---

## Graph representation

All integer-node algorithms work with `List<List<Integer>> adj`, where `adj.get(u)` is the
neighbour list of node `u` and nodes are numbered `0..n-1`. This is the standard adjacency-list
representation: O(V + E) space, O(degree(u)) per neighbour scan.

```java
public static final int[][] DIRS = {{1,0},{-1,0},{0,1},{0,-1}};
```

`DIRS` encodes the four cardinal directions for grid problems.

---

## buildUndirected / buildDirected

```java
public static List<List<Integer>> buildUndirected(int n, int[][] edges)
public static List<List<Integer>> buildDirected  (int n, int[][] edges)
```

**How they work.** Allocate `n` empty lists, then insert each edge. `buildUndirected` inserts
both `u→v` and `v→u`; `buildDirected` inserts only `u→v`.

**Complexity.** O(V + E) time and space.

**Usage**

```java
int[][] edges = {{0,1},{1,2},{2,3}};
List<List<Integer>> g = Graphs.buildUndirected(4, edges);
// g.get(1) = [0, 2]
```

**Related LeetCode problems.** 133 (Clone Graph), 207 (Course Schedule), 743 (Network Delay
Time), 1584 (Min Cost to Connect All Points).

---

## buildUndirectedMap

```java
public static <T> Map<T, List<T>> buildUndirectedMap(List<List<T>> edges)
```

**When to use it.** When nodes are strings, characters, or any non-integer type. The
`computeIfAbsent` pattern creates neighbour lists on demand.

**Usage**

```java
List<List<String>> edges = List.of(
    List.of("a", "b"), List.of("b", "c")
);
Map<String, List<String>> g = Graphs.buildUndirectedMap(edges);
// g.get("b") = ["a", "c"]
```

**Related LeetCode problems.** 127 (Word Ladder — string nodes), 433 (Minimum Genetic Mutation),
684 (Redundant Connection can use string variant).

---

## bfsDistances

```java
public static int[] bfsDistances(List<List<Integer>> adj, int source)
```

**Why BFS gives shortest distances.** BFS explores nodes in non-decreasing distance order.
When a node `v` is first dequeued at distance `d`, it is impossible to reach `v` in fewer than
`d` steps (because any path of length `< d` would have reached it earlier). This is the key
BFS invariant.

**Implementation detail.** The `dist` array doubles as the visited set: `dist[v] == -1` means
unvisited. Setting `dist[v]` before enqueuing (not after dequeuing) prevents re-queueing the
same node, which is critical for correctness and keeps the O(V + E) bound.

**Complexity.** O(V + E) time, O(V) space.

**Usage**

```java
int[] d = Graphs.bfsDistances(adj, 0);
System.out.println(d[3]); // shortest hops from node 0 to node 3; -1 if unreachable
```

**Related LeetCode problems.** 1091 (Shortest Path in Binary Matrix), 752 (Open the Lock),
994 (Rotting Oranges), 1765 (Map of Highest Peak).

**Gotcha.** Works only on unweighted graphs. For weighted graphs use Dijkstra.

---

## multiSourceBfs

```java
public static int[] multiSourceBfs(List<List<Integer>> adj, Collection<Integer> sources)
```

**The key insight.** Pre-loading all sources at distance 0 before the BFS starts is equivalent
to adding a virtual "super-source" node with zero-weight edges to every real source. The result
is each node's distance to its nearest source.

**Invariant.** The first time any node is reached, it is reached from the nearest source (same
BFS level argument as single-source BFS).

**Usage**

```java
// "nearest infected cell" pattern
Collection<Integer> infected = List.of(2, 7);
int[] dist = Graphs.multiSourceBfs(adj, infected);
```

**Related LeetCode problems.** 994 (Rotting Oranges — classic multi-source), 1765 (Map of
Highest Peak), 286 (Walls and Gates — multi-source on a grid).

---

## dfsOrder

```java
public static List<Integer> dfsOrder(List<List<Integer>> adj, int start)
```

**How it works.** Iterative DFS using an explicit stack. Neighbours are pushed in reverse order
so the first neighbour is processed first, matching recursive DFS pre-order. The `visited` array
guards against re-processing and prevents infinite loops in cyclic graphs.

**Why iterative.** Avoids call-stack overflow on deep graphs (e.g. a path graph with 100 000
nodes).

**Complexity.** O(V + E) time, O(V) space.

**Gotcha.** This visits only nodes reachable from `start`. For a disconnected graph, loop over
all unvisited nodes and call `dfsOrder` from each.

**Related LeetCode problems.** 200 (Number of Islands — DFS component counting), 695 (Max
Area of Island), 417 (Pacific Atlantic Water Flow), 547 (Number of Provinces).

---

## hasCycle

```java
public static boolean hasCycle(List<List<Integer>> adj)
```

**Three-color DFS.** Nodes start WHITE (unvisited). When DFS enters a node it turns GRAY
(in the current path). When DFS finishes (all descendants processed) it turns BLACK (done).

**The invariant.** A back-edge — an edge from a GRAY node to another GRAY node — exists if and
only if the directed graph contains a cycle. Why? A GRAY node is an ancestor of the current
DFS path; an edge to it closes a loop.

```
WHITE = 0   (never entered)
GRAY  = 1   (entered, not yet fully explored)
BLACK = 2   (fully explored)
```

**Why two-color (visited/unvisited) is wrong for directed graphs.** In an undirected graph a
single `visited` boolean suffices. In a directed graph, if two DFS paths share a node (a
"diamond" DAG), the second path sees the node as visited and incorrectly reports a cycle. The
GRAY/BLACK distinction prevents this: a BLACK node was fully explored on a different path and
cannot form a new cycle.

**Complexity.** O(V + E) time, O(V) space.

**Related LeetCode problems.** 207 (Course Schedule), 802 (Find Eventual Safe States), 2360
(Longest Cycle in a Graph).

---

## topoSortKahn

```java
public static int[] topoSortKahn(List<List<Integer>> adj)
```

Returns a topological ordering as an `int[]`, or an empty array if the graph has a cycle.

**How Kahn's algorithm works.**

1. Count in-degrees of all nodes.
2. Enqueue every zero-in-degree node ("sources").
3. Repeatedly dequeue a node, add it to the result, and decrement the in-degree of each
   successor. If a successor's in-degree drops to zero, enqueue it.

**The invariant.** A node enters the queue only when all its prerequisites have been emitted.
This is exactly the topological order guarantee.

**Cycle detection.** If the graph has a cycle, the nodes in the cycle can never reach zero
in-degree, so `count < n` at the end.

**Complexity.** O(V + E) time, O(V) space.

**Usage**

```java
// Course schedule: can you finish all courses?
int[] order = Graphs.topoSortKahn(adj);
boolean canFinish = order.length == numCourses;
```

**Related LeetCode problems.** 207 (Course Schedule), 210 (Course Schedule II — return the
order), 310 (Minimum Height Trees — variant using leaf pruning, same structure), 1462
(Course Schedule IV).

**Kahn vs DFS topo-sort.** Kahn's is BFS-based and naturally detects cycles. DFS topo-sort
produces the reverse post-order, which is also valid. Prefer Kahn's when you need the
"prerequisite" ordering explicitly; prefer DFS when you already have a DFS traversal.

---

## topoSortDfs

```java
public static List<Integer> topoSortDfs(List<List<Integer>> adj)
```

Returns the topological order (reversed DFS finish order), or `null` if the graph has a cycle.

**How it works.** Perform DFS. Add each node to a list when it *finishes* (all descendants
done). Reverse at the end. A node appended after all its successors will appear before all of
them after reversal — the topological guarantee.

**Complexity.** O(V + E) time, O(V) space.

**Gotcha.** Returns `null` (not an empty list) on a cycle, distinguishing "empty graph" from
"cycle detected." Check for null before using the result.

---

## dijkstra

```java
public static long[] dijkstra(int n, int[][] edges, int src)
// edges[i] = {u, v, weight}; unreachable nodes = Long.MAX_VALUE
```

**Why Dijkstra is correct.** Dijkstra is a greedy algorithm whose correctness rests on one
invariant: **once a node is settled (popped from the priority queue with the minimum tentative
distance), its distance is final**.

This invariant holds because all edge weights are non-negative. When we pop node `u` with
tentative distance `d`, any other path to `u` must go through a node with tentative distance
`>= d` (since the queue pops in increasing distance order). Adding more non-negative edges can
only increase the distance, so no shorter path exists.

**The lazy-deletion pattern.** The implementation uses lazy deletion: stale entries remain in
the priority queue but are skipped when popped (`if (top[0] > dist[u]) continue`). This avoids
implementing a decrease-key operation and is idiomatic Java.

**Complexity.** O((V + E) log V) with a binary-heap priority queue.

**Usage**

```java
int[][] edges = {{0,1,4},{0,2,1},{2,1,2},{1,3,1}};
long[] dist = Graphs.dijkstra(4, edges, 0);
// dist[3] = 4  (path 0→2→1→3 with cost 1+2+1)
```

**Related LeetCode problems.** 743 (Network Delay Time), 1514 (Path with Maximum Probability
— negate weights or use max-heap), 1631 (Path with Minimum Effort — min-heap on max edge),
778 (Swim in Rising Water — binary search + BFS or Dijkstra variant).

**Gotcha.** Dijkstra fails with negative edge weights. Use Bellman-Ford (not in this library)
for graphs with negative weights.

---

## gridDistances

```java
public static int[][] gridDistances(char[][] grid, int sr, int sc)
// '.' = open, '#' = wall; returns -1 for unreachable cells
```

**How it works.** Identical to `bfsDistances` but on a 2D grid. Uses `DIRS` for four-directional
movement, bounds-checks before enqueuing, and skips wall cells and already-visited cells.

**Complexity.** O(rows * cols) time and space.

**Usage**

```java
char[][] grid = {
    {'.', '.', '#'},
    {'#', '.', '.'},
    {'.', '.', '.'}
};
int[][] d = Graphs.gridDistances(grid, 0, 0);
// d[2][2] = 4 — four steps to bottom-right
```

**Variant — 8-directional movement.** Replace `DIRS` with an 8-element array including
diagonals for problems like 1091 (Shortest Path in Binary Matrix).

**Related LeetCode problems.** 1091 (Shortest Path in Binary Matrix), 994 (Rotting Oranges),
1765 (Map of Highest Peak), 542 (01 Matrix — multi-source BFS on grid).

---

## shortestPath

```java
public static int shortestPath(int[][] grid, int sr, int sc, int tr, int tc)
// 0 = open, non-zero = blocked; returns step count or -1
```

**Difference from gridDistances.** `gridDistances` returns a full distance map from one source.
`shortestPath` returns a single distance from source to target, stopping early. Uses a separate
`seen` boolean array (appropriate when the grid contains integers, not chars).

**Complexity.** O(rows * cols) worst case (must scan entire grid).

**Usage**

```java
int[][] grid = {{0,0,0},{0,1,0},{0,0,0}};
int steps = Graphs.shortestPath(grid, 0, 0, 2, 2); // 4
```

**Related LeetCode problems.** 1091 (Shortest Path in Binary Matrix), 490 (The Maze), 505
(The Maze II — weighted grid, use Dijkstra).

---

## UnionFind

```java
public UnionFind(int n)
public int  find(int x)              // root of x's component
public boolean union(int x, int y)  // merge; false if already connected
public boolean connected(int x, int y)
public int components()
public int sizeOf(int x)
```

**Data structure.** A forest of trees where each tree is a connected component. Each node
stores a `parent` pointer; find follows pointers to the root; union links two roots.

**Path compression.** After `find(x)` locates the root, the two-pass implementation directly
links every node on the path to the root. Subsequent `find` calls on those nodes become O(1).

```
Before: x → a → b → root
After:  x → root, a → root, b → root
```

**Union by size.** Always attach the smaller tree under the root of the larger. This keeps tree
height O(log n), which bounds the un-compressed path length.

**Combined complexity.** With both optimisations, any sequence of m operations on n elements
runs in O(m · α(n)) amortised time, where α is the inverse Ackermann function — effectively
constant for all practical values of n.

**The `union` return value.** `union` returns `false` if the two nodes were already in the same
component. This is a free cycle-detection signal: if you process E edges and every `union`
returns `true`, the graph is a spanning tree.

**Usage**

```java
UnionFind uf = new UnionFind(5);
uf.union(0, 1);
uf.union(1, 2);
uf.connected(0, 2); // true
uf.components();    // 3 — {0,1,2}, {3}, {4}
uf.sizeOf(0);       // 3
```

**Related LeetCode problems.** 547 (Number of Provinces), 684 (Redundant Connection — union
returns false → that edge forms a cycle), 1319 (Number of Operations to Make Network Connected),
1584 (Min Cost to Connect All Points — Kruskal's MST), 721 (Accounts Merge — string union-find).

**UnionFind vs BFS/DFS for connectivity.** BFS/DFS is one-shot: compute all components once.
UnionFind is dynamic: supports incremental `union` operations and O(α(n)) `connected` queries
afterward. Use UnionFind when edges are added one at a time and you need online connectivity
queries.

---

## Algorithm selection guide

| Problem type                                | Algorithm              |
|---------------------------------------------|------------------------|
| Shortest path, unweighted graph             | BFS (`bfsDistances`)   |
| Shortest path, unweighted, multiple sources | `multiSourceBfs`       |
| Shortest path, weighted, non-negative       | `dijkstra`             |
| Shortest path on a grid                     | `gridDistances` / `shortestPath` |
| Detect cycle in directed graph              | `hasCycle` (DFS 3-color)|
| Topological ordering / prerequisite check   | `topoSortKahn`         |
| Dynamic connectivity / MST (Kruskal)        | `UnionFind`            |
| Reachability, component labeling            | `dfsOrder` or BFS      |
