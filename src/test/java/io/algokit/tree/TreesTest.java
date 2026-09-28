package io.algokit.tree;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TreesTest {

    @Test void traversals() {
        //        1
        //      /   \
        //     2     3
        //    / \     \
        //   4   5     6
        TreeNode t = Trees.fromLevelOrder(1, 2, 3, 4, 5, null, 6);
        List<Integer> pre = new ArrayList<>();
        Trees.preorder(t, pre);
        assertEquals(List.of(1, 2, 4, 5, 3, 6), pre);
        assertEquals(pre, Trees.preorderIterative(t));

        List<Integer> in = new ArrayList<>();
        Trees.inorder(t, in);
        assertEquals(List.of(4, 2, 5, 1, 3, 6), in);
        assertEquals(in, Trees.inorderIterative(t));
    }

    @Test void levelOrder() {
        TreeNode t = Trees.fromLevelOrder(1, 2, 3, 4, 5, null, 6);
        assertEquals(List.of(List.of(1), List.of(2, 3), List.of(4, 5, 6)), Trees.levelOrder(t));
        assertEquals(List.of(), Trees.levelOrder(null));
    }

    @Test void height() {
        TreeNode t = Trees.fromLevelOrder(1, 2, 3, 4, 5, null, 6);
        assertEquals(3, Trees.height(t));
        assertEquals(3, Trees.heightIterative(t));
        assertEquals(0, Trees.height(null));
    }

    @Test void isBalanced() {
        assertTrue(Trees.isBalanced(Trees.fromLevelOrder(1, 2, 3, 4, 5, null, 6)));
        assertFalse(Trees.isBalanced(Trees.fromLevelOrder(1, 2, null, 3)));
        assertTrue(Trees.isBalanced(null));
    }
}
