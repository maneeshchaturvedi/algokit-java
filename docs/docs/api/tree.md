# Tree API Reference

Package: `io.algokit.tree`

Binary tree algorithms built around the `TreeNode` shape used by LeetCode. Every public method
is a static utility on `Trees`; the node class itself is a plain data holder.

---

## TreeNode

```java
public class TreeNode {
    public int val;
    public TreeNode left;
    public TreeNode right;

    public TreeNode(int val) { ... }
    public TreeNode(int val, TreeNode left, TreeNode right) { ... }
}
```

Intentionally matches the LeetCode class definition so solutions written here compile in the
judge without change. Fields are `public` (not getters) for the same reason.

---

## Recursive Traversals

```java
public static void preorder (TreeNode node, List<Integer> out)
public static void inorder  (TreeNode node, List<Integer> out)
public static void postorder(TreeNode node, List<Integer> out)
```

**How they work.** Each function is a three-line skeleton differing only in where `out.add()`
appears relative to the recursive calls:

| Traversal  | Visit order                  | When to use                                  |
|------------|------------------------------|----------------------------------------------|
| Pre-order  | root → left → right          | Serialise/copy a tree; prefix expression trees|
| In-order   | left → root → right          | Sorted output from a BST; validate BST order |
| Post-order | left → right → root          | Compute subtree info before the parent        |

**Complexity.** O(n) time, O(h) space (call stack), where h is the height. For a balanced tree
h = O(log n); for a degenerate (linked-list) tree h = O(n).

**Usage**

```java
TreeNode root = Trees.fromLevelOrder(3, 1, 5, null, 2);
List<Integer> vals = new ArrayList<>();
Trees.inorder(root, vals);
// vals = [1, 2, 3, 5]  — sorted, as expected for a BST
```

**Related LeetCode problems.** 94 (Binary Tree Inorder), 144 (Preorder), 145 (Postorder),
98 (Validate BST uses in-order), 230 (Kth Smallest in BST).

**Gotcha.** Recursive traversal on a degenerate tree (n = 100 000 right-only nodes) will
overflow the default JVM stack (~10K frames). Use the iterative variants for production or
wrap in `DynamicProgramming.runWithStack`.

---

## Iterative Traversals

```java
public static List<Integer> preorderIterative (TreeNode root)
public static List<Integer> inorderIterative  (TreeNode root)
public static List<Integer> postorderIterative(TreeNode root)
```

**Why iterative?** The recursive versions rely on the JVM call stack, which is limited. The
iterative versions use an explicit `Deque<TreeNode>` on the heap, which can grow to O(n) without
a stack overflow. Prefer iterative when tree depth is unbounded.

### Iterative pre-order

Push right before left so the left child is popped first, preserving root → left → right order.

```java
stack.push(root);
while (!stack.isEmpty()) {
    TreeNode node = stack.pop();
    out.add(node.val);               // visit
    if (node.right != null) stack.push(node.right);
    if (node.left  != null) stack.push(node.left);
}
```

### Iterative in-order

Walk as far left as possible, pushing nodes, then backtrack and turn right. The `cur` pointer
acts as "current path tip"; the stack holds the return addresses.

```java
while (cur != null || !stack.isEmpty()) {
    while (cur != null) { stack.push(cur); cur = cur.left; }
    cur = stack.pop();
    out.add(cur.val);                // visit at back-track point
    cur = cur.right;
}
```

### Iterative post-order

The trickiest iterative traversal. A node is emitted only after both subtrees are done. The
`lastEmitted` sentinel prevents the algorithm from re-entering the right subtree when returning
from it.

**Invariant:** at the moment of emission, `lastEmitted` is set to that node. When the stack
next peeks at the parent, `top.right == lastEmitted` signals "right subtree finished — emit
parent now."

**Related LeetCode problems.** 341 (Flatten Nested List Iterator mirrors iterative in-order),
426 (BST to sorted doubly linked list), 897 (Increasing Order Search Tree).

---

## levelOrder

```java
public static List<List<Integer>> levelOrder(TreeNode root)
```

**How it works.** Standard BFS with a snapshot trick: at the start of each iteration, record
`queue.size()` — that is exactly how many nodes belong to the current level. Process that many
nodes, then move to the next level.

```
queue: [3]
  snapshot size=1 → level=[3], enqueue 1, 5
queue: [1, 5]
  snapshot size=2 → level=[1,5], enqueue 2 (child of 1)
queue: [2]
  snapshot size=1 → level=[2]
```

**Complexity.** O(n) time, O(w) space where w is the maximum width (at most n/2 for the last
level of a complete tree).

**Usage**

```java
// LC 102 pattern
List<List<Integer>> levels = Trees.levelOrder(root);
int lastLevel = levels.get(levels.size() - 1).size(); // width of bottom level
```

**Related LeetCode problems.** 102 (Level Order Traversal), 103 (Zigzag Level Order — reverse
alternate levels), 107 (Bottom-up Level Order), 199 (Right Side View — last element of each
level), 513 (Find Bottom-Left Tree Value), 637 (Average of Levels).

**Variant — right side view.** Instead of collecting all nodes per level, keep only the last:
```java
for (List<Integer> level : Trees.levelOrder(root))
    result.add(level.get(level.size() - 1));
```

---

## height / heightIterative

```java
public static int height         (TreeNode node)   // recursive
public static int heightIterative(TreeNode root)   // BFS level count
```

**Recursive height.** Classic post-order: compute left height, compute right height, return
`1 + max(left, right)`. Base case: null → 0.

**Iterative height.** Reuses the `levelOrder` BFS structure but counts levels instead of
recording values. The `h` counter increments once per non-empty level sweep.

**Complexity.** Both are O(n) time, O(h) / O(w) space respectively.

**Usage**

```java
TreeNode skewed = Trees.fromLevelOrder(1, null, 2, null, 3); // right-skewed
Trees.height(skewed);          // 3 — safe for small trees
Trees.heightIterative(skewed); // 3 — preferred for deep trees
```

**Gotcha.** The recursive version defines an empty tree as height 0 and a single node as
height 1. Some problems define height as the number of edges (single node = 0). Adjust the
base case to `return -1` and `1 + Math.max(...)` stays the same.

---

## isBalanced

```java
public static boolean isBalanced(TreeNode root)
```

**The naive O(n²) trap.** Calling `height(left)` and `height(right)` at every node re-traverses
subtrees repeatedly. For a right-skewed tree this degrades to O(n²).

**The O(n) single-pass fix.** `balanceHeight` (private) serves dual purpose: it returns the
subtree height when balanced, or the sentinel -1 on the first detected imbalance. Once -1
propagates up, short-circuit returns skip all remaining computation.

```
balanceHeight(node):
    left  = balanceHeight(node.left)  ; if -1 return -1
    right = balanceHeight(node.right) ; if -1 return -1
    if |left - right| > 1 return -1
    return 1 + max(left, right)
```

**The "return a record" pattern.** `balanceHeight` returns an `int` that encodes two pieces of
information (height and balance status) via a sentinel. A cleaner generalization uses a small
record/class when you need more than one value from a post-order helper:

```java
record Info(int height, int diameter) {}
Info dfs(TreeNode node) {
    if (node == null) return new Info(0, 0);
    Info L = dfs(node.left), R = dfs(node.right);
    int h = 1 + Math.max(L.height, R.height);
    int d = Math.max(L.height + R.height, Math.max(L.diameter, R.diameter));
    return new Info(h, d);
}
```

**Related LeetCode problems.** 110 (Balanced Binary Tree), 543 (Diameter — classic record
pattern), 124 (Binary Tree Maximum Path Sum — similar multi-value post-order).

---

## fromLevelOrder

```java
public static TreeNode fromLevelOrder(Integer... vals)
```

**Purpose.** Converts the LeetCode array format (e.g. `[3,9,20,null,null,15,7]`) into a live
tree. Null entries mark absent children; children of null nodes are never inserted.

**How it works.** Maintain a BFS queue of nodes whose children have not yet been assigned.
Consume `vals` in pairs (left child, right child) for each dequeued parent. If a value is null,
that child is absent and is not enqueued, so its children are skipped automatically.

**Usage**

```java
// Builds:      3
//             / \
//            9  20
//               / \
//              15   7
TreeNode root = Trees.fromLevelOrder(3, 9, 20, null, null, 15, 7);
```

**Complexity.** O(n) time and space.

**Gotcha.** Indices in the input correspond to the *BFS position*, not the raw `2i+1 / 2i+2`
formula — because null entries remove nodes from the queue, compressing the array. For a
perfect binary tree the two representations are identical, but they diverge as soon as any null
appears.

---

## Choosing the right traversal

| You need to...                             | Use                         |
|--------------------------------------------|-----------------------------|
| Sort / validate BST                        | `inorder`                   |
| Serialise or copy the tree                 | `preorder`                  |
| Compute subtree aggregates (height, sum)   | `postorder`                 |
| Work level by level (BFS)                  | `levelOrder`                |
| Avoid stack overflow on deep trees         | Any iterative variant        |
| Build test trees from LeetCode examples    | `fromLevelOrder`            |

## Iterative vs recursive tradeoff

| Dimension          | Recursive                     | Iterative                     |
|--------------------|-------------------------------|-------------------------------|
| Code clarity       | Very concise                  | More verbose                  |
| Stack safety       | Overflows at ~10K depth       | Heap-limited only             |
| Debugging          | Easy with debugger call stack | Requires inspecting Deque     |
| Interviewer signal | Fine for balanced trees       | Shows depth awareness         |

In an interview, start recursive to show the logic clearly, then mention "for a degenerate tree
I'd switch to the iterative version to avoid stack overflow" — this is the response that
distinguishes senior candidates.
