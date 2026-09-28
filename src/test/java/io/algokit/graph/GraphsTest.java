package io.algokit.graph;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GraphsTest {

    @Test void bfsDistances() {
        var g = Graphs.buildUndirected(6, new int[][]{{0,1},{0,2},{1,3},{2,3},{3,4}});
        assertArrayEquals(new int[]{0, 1, 1, 2, 3, -1}, Graphs.bfsDistances(g, 0));
    }

    @Test void dfsOrder() {
        var g = Graphs.buildUndirected(6, new int[][]{{0,1},{0,2},{1,3},{2,3},{3,4}});
        assertEquals(List.of(0, 1, 3, 2, 4), Graphs.dfsOrder(g, 0));
    }

    @Test void hasCycleDetection() {
        var dag = Graphs.buildDirected(4, new int[][]{{0,1},{0,2},{1,3},{2,3}});
        assertFalse(Graphs.hasCycle(dag));
        var cyc = Graphs.buildDirected(3, new int[][]{{0,1},{1,2},{2,1}});
        assertTrue(Graphs.hasCycle(cyc));
    }

    @Test void topoSortKahn() {
        var dag = Graphs.buildDirected(4, new int[][]{{0,1},{0,2},{1,3},{2,3}});
        int[] order = Graphs.topoSortKahn(dag);
        assertEquals(4, order.length);
        // cycle returns empty
        var cyc = Graphs.buildDirected(3, new int[][]{{0,1},{1,2},{2,0}});
        assertEquals(0, Graphs.topoSortKahn(cyc).length);
    }

    @Test void dijkstra() {
        long[] dist = Graphs.dijkstra(4, new int[][]{{0,1,1},{1,2,2},{0,2,5},{2,3,1}}, 0);
        assertArrayEquals(new long[]{0, 1, 3, 4}, dist);
    }

    @Test void gridDistances() {
        char[][] grid = {"..#".toCharArray(), ".##".toCharArray(), "...".toCharArray()};
        int[][] d = Graphs.gridDistances(grid, 0, 0);
        assertEquals(4, d[2][2]);
        assertEquals(-1, d[0][2]);
    }

    @Test void shortestPath() {
        int[][] grid = {{0,0,0},{1,1,0},{0,0,0}};
        assertEquals(6, Graphs.shortestPath(grid, 0, 0, 2, 0));
        assertEquals(0, Graphs.shortestPath(grid, 0, 0, 0, 0));
    }

    @Test void unionFind() {
        UnionFind uf = new UnionFind(5);
        assertTrue(uf.union(0, 1));
        assertTrue(uf.union(3, 4));
        assertFalse(uf.union(1, 0));
        assertEquals(3, uf.components());
        assertTrue(uf.connected(0, 1));
        assertFalse(uf.connected(1, 3));
        assertEquals(2, uf.sizeOf(4));
    }
}
