package io.algokit.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Graph building, traversal, and algorithm templates: BFS, DFS, topological sort,
 * Dijkstra, cycle detection, and grid BFS.
 */
public final class Graphs {
    private Graphs() {}

    public static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // --- Adjacency list builders ---

    /** Nodes 0..n-1, undirected. edges[i] = {u, v}. */
    public static List<List<Integer>> buildUndirected(int n, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }
        return adj;
    }

    /** Nodes 0..n-1, directed. edges[i] = {u, v}. */
    public static List<List<Integer>> buildDirected(int n, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) adj.get(e[0]).add(e[1]);
        return adj;
    }

    /** Adjacency map for string/sparse node ids. */
    public static <T> Map<T, List<T>> buildUndirectedMap(List<List<T>> edges) {
        Map<T, List<T>> adj = new HashMap<>();
        for (List<T> e : edges) {
            adj.computeIfAbsent(e.get(0), k -> new ArrayList<>()).add(e.get(1));
            adj.computeIfAbsent(e.get(1), k -> new ArrayList<>()).add(e.get(0));
        }
        return adj;
    }

    // --- BFS ---

    /** Unweighted shortest distances from source; -1 = unreachable. O(V + E). */
    public static int[] bfsDistances(List<List<Integer>> adj, int source) {
        int[] dist = new int[adj.size()];
        Arrays.fill(dist, -1);
        Deque<Integer> queue = new ArrayDeque<>();
        dist[source] = 0;
        queue.offer(source);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : adj.get(u)) {
                if (dist[v] == -1) {
                    dist[v] = dist[u] + 1;
                    queue.offer(v);
                }
            }
        }
        return dist;
    }

    /** Multi-source BFS: distance to the nearest source. */
    public static int[] multiSourceBfs(List<List<Integer>> adj, Collection<Integer> sources) {
        int[] dist = new int[adj.size()];
        Arrays.fill(dist, -1);
        Deque<Integer> queue = new ArrayDeque<>();
        for (int s : sources) {
            if (dist[s] == -1) { dist[s] = 0; queue.offer(s); }
        }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : adj.get(u)) {
                if (dist[v] == -1) { dist[v] = dist[u] + 1; queue.offer(v); }
            }
        }
        return dist;
    }

    // --- DFS ---

    /** Iterative DFS pre-order from start. */
    public static List<Integer> dfsOrder(List<List<Integer>> adj, int start) {
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[adj.size()];
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (visited[u]) continue;
            visited[u] = true;
            order.add(u);
            List<Integer> nbrs = adj.get(u);
            for (int i = nbrs.size() - 1; i >= 0; i--) {
                if (!visited[nbrs.get(i)]) stack.push(nbrs.get(i));
            }
        }
        return order;
    }

    // --- Cycle detection (directed) ---

    private static final int WHITE = 0, GRAY = 1, BLACK = 2;

    /** Three-color directed cycle detection. O(V + E). */
    public static boolean hasCycle(List<List<Integer>> adj) {
        int[] color = new int[adj.size()];
        for (int u = 0; u < adj.size(); u++) {
            if (color[u] == WHITE && reachesGray(adj, u, color)) return true;
        }
        return false;
    }

    private static boolean reachesGray(List<List<Integer>> adj, int u, int[] color) {
        color[u] = GRAY;
        for (int v : adj.get(u)) {
            if (color[v] == GRAY) return true;
            if (color[v] == WHITE && reachesGray(adj, v, color)) return true;
        }
        color[u] = BLACK;
        return false;
    }

    // --- Topological sort ---

    /** Kahn's BFS topological sort. Returns empty array if the graph has a cycle. */
    public static int[] topoSortKahn(List<List<Integer>> adj) {
        int n = adj.size();
        int[] indegree = new int[n];
        for (List<Integer> out : adj) for (int v : out) indegree[v]++;
        Deque<Integer> ready = new ArrayDeque<>();
        for (int u = 0; u < n; u++) if (indegree[u] == 0) ready.offer(u);
        int[] order = new int[n];
        int count = 0;
        while (!ready.isEmpty()) {
            int u = ready.poll();
            order[count++] = u;
            for (int v : adj.get(u)) {
                if (--indegree[v] == 0) ready.offer(v);
            }
        }
        return count == n ? order : new int[0];
    }

    /** DFS reverse post-order topological sort. Returns null if the graph has a cycle. */
    public static List<Integer> topoSortDfs(List<List<Integer>> adj) {
        int[] color = new int[adj.size()];
        List<Integer> postorder = new ArrayList<>();
        for (int u = 0; u < adj.size(); u++) {
            if (color[u] == WHITE && !finish(adj, u, color, postorder)) return null;
        }
        Collections.reverse(postorder);
        return postorder;
    }

    private static boolean finish(List<List<Integer>> adj, int u, int[] color, List<Integer> postorder) {
        color[u] = GRAY;
        for (int v : adj.get(u)) {
            if (color[v] == GRAY) return false;
            if (color[v] == WHITE && !finish(adj, v, color, postorder)) return false;
        }
        color[u] = BLACK;
        postorder.add(u);
        return true;
    }

    // --- Dijkstra ---

    /** Shortest distances from src. edges[i] = {u, v, weight}. Unreachable = Long.MAX_VALUE. */
    public static long[] dijkstra(int n, int[][] edges, int src) {
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) adj.get(e[0]).add(new int[]{e[1], e[2]});
        long[] dist = new long[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[src] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(x -> x[0]));
        pq.offer(new long[]{0, src});
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            int u = (int) top[1];
            if (top[0] > dist[u]) continue;
            for (int[] e : adj.get(u)) {
                long nd = dist[u] + e[1];
                if (nd < dist[e[0]]) { dist[e[0]] = nd; pq.offer(new long[]{nd, e[0]}); }
            }
        }
        return dist;
    }

    // --- Grid BFS ---

    /** BFS distances from (sr, sc) on a grid. '.' = open, '#' = wall. -1 = unreachable. */
    public static int[][] gridDistances(char[][] grid, int sr, int sc) {
        int rows = grid.length, cols = grid[0].length;
        int[][] dist = new int[rows][cols];
        for (int[] row : dist) Arrays.fill(row, -1);
        Deque<int[]> queue = new ArrayDeque<>();
        dist[sr][sc] = 0;
        queue.offer(new int[]{sr, sc});
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            for (int[] d : DIRS) {
                int r = cell[0] + d[0], c = cell[1] + d[1];
                if (r < 0 || r >= rows || c < 0 || c >= cols) continue;
                if (grid[r][c] == '#' || dist[r][c] != -1) continue;
                dist[r][c] = dist[cell[0]][cell[1]] + 1;
                queue.offer(new int[]{r, c});
            }
        }
        return dist;
    }

    /** Fewest steps on an integer grid (0 = open) from (sr,sc) to (tr,tc), or -1. */
    public static int shortestPath(int[][] grid, int sr, int sc, int tr, int tc) {
        int rows = grid.length, cols = grid[0].length;
        if (grid[sr][sc] != 0 || grid[tr][tc] != 0) return -1;
        boolean[][] seen = new boolean[rows][cols];
        Deque<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{sr, sc});
        seen[sr][sc] = true;
        for (int steps = 0; !queue.isEmpty(); steps++) {
            for (int n = queue.size(); n > 0; n--) {
                int[] cur = queue.poll();
                if (cur[0] == tr && cur[1] == tc) return steps;
                for (int[] d : DIRS) {
                    int r = cur[0] + d[0], c = cur[1] + d[1];
                    if (r < 0 || c < 0 || r >= rows || c >= cols || seen[r][c] || grid[r][c] != 0) continue;
                    seen[r][c] = true;
                    queue.offer(new int[]{r, c});
                }
            }
        }
        return -1;
    }
}
