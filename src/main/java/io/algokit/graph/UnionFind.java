package io.algokit.graph;

/**
 * Disjoint-set union with path compression and union by size.
 * Amortised ~O(alpha(n)) per operation.
 */
public final class UnionFind {
    private final int[] parent;
    private final int[] size;
    private int components;

    public UnionFind(int n) {
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }

    public int find(int x) {
        int root = x;
        while (parent[root] != root) root = parent[root];
        while (parent[x] != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    /** Returns false if x and y were already connected. */
    public boolean union(int x, int y) {
        int rx = find(x), ry = find(y);
        if (rx == ry) return false;
        if (size[rx] < size[ry]) { int tmp = rx; rx = ry; ry = tmp; }
        parent[ry] = rx;
        size[rx] += size[ry];
        components--;
        return true;
    }

    public boolean connected(int x, int y) { return find(x) == find(y); }

    public int components() { return components; }

    public int sizeOf(int x) { return size[find(x)]; }
}
