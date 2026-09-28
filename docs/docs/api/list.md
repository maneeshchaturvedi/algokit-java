# Linked Lists

**Package:** `io.algokit.list`  
**Source:** [`ListNode.java`](https://github.com/maneeshchaturvedi/algokit-java/blob/main/src/main/java/io/algokit/list/ListNode.java), [`Lists.java`](https://github.com/maneeshchaturvedi/algokit-java/blob/main/src/main/java/io/algokit/list/Lists.java)

This package provides the standard LeetCode-shaped `ListNode` class and a set of utilities for constructing, inspecting, and transforming singly linked lists. The utilities exist primarily to remove boilerplate from test code, but several of them — `removeAll` and `middleNode` — also demonstrate the two most important linked-list coding patterns: the **dummy-head** pattern and the **fast/slow pointer** pattern.

---

## `ListNode`

### Definition
```java
public class ListNode {
    public int val;
    public ListNode next;

    public ListNode(int val) { this.val = val; }
    public ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}
```

### How and why it works
`ListNode` deliberately mirrors the class definition used on LeetCode and in most interview environments. Fields are `public` (not private with getters) because interview code prioritizes brevity, and the two-argument constructor `ListNode(val, next)` allows compact inline construction:

```java
// Build 1 -> 2 -> 3 in one expression
ListNode head = new ListNode(1, new ListNode(2, new ListNode(3, null)));
```

### Related LeetCode problems
Every linked-list problem on LeetCode uses this exact class. Key problems:
- **206** – Reverse Linked List
- **21**  – Merge Two Sorted Lists
- **141** – Linked List Cycle

### Gotchas
- `next` is `null` by default in Java (fields are zero-initialized). The single-argument constructor does not set `next` explicitly, which is correct.
- There is no `equals` override — two `ListNode` objects are equal only if they are the same object reference. Use `Arrays.equals(Lists.toArray(a), Lists.toArray(b))` to compare lists by value in tests.

---

## `Lists.of`

### Signature
```java
public static ListNode of(int... values)
```

### How and why it works
Uses the **dummy-head pattern** to avoid a special case for the first node. A sentinel `dummy` node (value 0, never returned) serves as the perpetual "previous" node, so every new node is appended the same way: `tail.next = new ListNode(v); tail = tail.next`. After the loop, `dummy.next` is the true head. Without the dummy, you would need to branch on whether `head == null` to handle the first element differently — a source of bugs. This construction pattern appears in at least a dozen interview problems.

### Complexity
- **Time:** O(n)
- **Space:** O(n) for the new nodes

### Usage example
```java
ListNode head = Lists.of(1, 2, 3, 4, 5);
// Builds: 1 -> 2 -> 3 -> 4 -> 5 -> null

ListNode empty = Lists.of();  // returns null

// Useful in tests
assertArrayEquals(new int[]{1, 2, 3}, Lists.toArray(Lists.of(1, 2, 3)));
```

### Related LeetCode problems
- Primarily a test utility, but the dummy-head construction pattern is directly applicable to:
- **21** – Merge Two Sorted Lists
- **23** – Merge K Sorted Lists
- **82** – Remove Duplicates from Sorted List II

### Common variants
- Build from a `List<Integer>`: iterate over the list in the same way.
- Build a cyclic list for testing cycle-detection algorithms: after calling `of(...)`, follow the tail to a chosen node and set `tail.next = cycleEntry`.

### Gotchas
- `Lists.of()` with no arguments returns `null` (an empty list), not a dummy node.
- Varargs `int...` is copied by the compiler — `values` is a fresh array, so modifying it does not affect the list.

---

## `Lists.toArray`

### Signature
```java
public static int[] toArray(ListNode head)
```

### How and why it works
Walks the list collecting values into an `ArrayList<Integer>`, then converts to `int[]`. The limit of 10 million nodes is a cycle guard — if a buggy test accidentally creates a cyclic list, this function throws `IllegalStateException` rather than looping forever. The two-step collection (list first, array second) avoids needing to know the length upfront.

### Complexity
- **Time:** O(n)
- **Space:** O(n)

### Usage example
```java
ListNode head = Lists.of(3, 1, 4, 1, 5);
int[] values = Lists.toArray(head);
// values = [3, 1, 4, 1, 5]

// Compare expected result in tests
assertArrayEquals(new int[]{1, 2, 3}, Lists.toArray(mergeResult));
```

### Related LeetCode problems
- Test utility. No direct LeetCode equivalent.

### Gotchas
- Throws `IllegalStateException` (not an infinite loop) if the list has a cycle. This is intentional — it fails loudly rather than hanging the test suite.
- Returns `new int[0]` for a null (empty) head.

---

## `Lists.nodes`

### Signature
```java
public static List<ListNode> nodes(ListNode head)
```

### How and why it works
Traverses the list once and collects node references (not just values) into a `List<ListNode>`. This enables index-based access to nodes — useful for problems that need to reach the k-th node without a second traversal, or for testing that node identity (reference equality) is preserved after an in-place operation.

### Complexity
- **Time:** O(n)
- **Space:** O(n)

### Usage example
```java
ListNode head = Lists.of(10, 20, 30, 40);
List<ListNode> nodeList = Lists.nodes(head);

// Access k-th node
ListNode third = nodeList.get(2); // node with val=30

// Verify in-place reversal preserved node identity
ListNode reversed = reverse(head);
List<ListNode> revNodes = Lists.nodes(reversed);
assertSame(nodeList.get(3), revNodes.get(0)); // same object
```

### Related LeetCode problems
- Test/inspection utility. Useful when verifying problems like:
- **25**  – Reverse Nodes in k-Group
- **61**  – Rotate List

### Gotchas
- No cycle guard. Unlike `toArray`, this will loop forever on a cyclic list. Only call on acyclic lists.

---

## `Lists.removeAll`

### Signature
```java
public static ListNode removeAll(ListNode head, int target)
```

### How and why it works
Demonstrates the **dummy-head pattern** for deletion. A sentinel `dummy` node whose `next` points to `head` is created so that the head node can be deleted using the same logic as any other node — no special-casing required. The loop walks `prev` forward: if `prev.next.val == target`, unlink it by setting `prev.next = prev.next.next` (without advancing `prev`, since the new `prev.next` may also need deletion); otherwise advance `prev`. At the end, `dummy.next` is the new head (which may differ from the original `head` if head nodes were removed).

### Complexity
- **Time:** O(n)
- **Space:** O(1) — modifies in place, only allocates the dummy node

### Source code
```java
public static ListNode removeAll(ListNode head, int target) {
    ListNode dummy = new ListNode(0, head);
    ListNode prev = dummy;
    while (prev.next != null) {
        if (prev.next.val == target) prev.next = prev.next.next;
        else prev = prev.next;
    }
    return dummy.next;
}
```

### Usage example
```java
// Remove all 3s from 1->3->3->2->3->4
ListNode result = Lists.removeAll(Lists.of(1, 3, 3, 2, 3, 4), 3);
// result: 1 -> 2 -> 4

// Remove head node
ListNode result2 = Lists.removeAll(Lists.of(1, 1, 2), 1);
// result2: 2  (head changed — dummy-head handles this)
```

### Related LeetCode problems
- **203** – Remove Linked List Elements (this exact operation)
- **82**  – Remove Duplicates from Sorted List II (extend with a "skip while equal" inner loop)
- **83**  – Remove Duplicates from Sorted List (simpler variant without dummy)

### The dummy-head pattern explained

The dummy head solves a fundamental asymmetry: deleting any non-head node requires a reference to its predecessor, but deleting the head has no predecessor. Rather than handling "is this the head?" everywhere, attach a permanent dummy predecessor:

```
dummy(0) -> head(1) -> ... 
  ^
  prev starts here
```

Now every node has a predecessor (`prev`), and `return dummy.next` gives the (possibly changed) head at the end. This pattern appears in: list reversal, list partitioning, merging sorted lists, and any in-place linked-list transformation.

### Gotchas
- Do **not** advance `prev` after an unlink — the new `prev.next` might also be a target. Only advance when the current `prev.next` is kept.
- The returned node may be different from `head` if leading nodes were removed. Always use the return value; ignoring it and keeping the old `head` reference is a common bug.

---

## `Lists.middleNode`

### Signature
```java
public static ListNode middleNode(ListNode head)
```

### How and why it works
Uses the **fast/slow pointer** (Floyd's tortoise and hare) approach. `slow` advances one step at a time; `fast` advances two steps at a time. When `fast` reaches the end (`fast == null` or `fast.next == null`), `slow` is at the middle. The loop condition `fast != null && fast.next != null` handles both odd-length lists (fast lands on the last node) and even-length lists (fast lands on null). For even-length lists, `slow` lands on the **second** middle node — this matches LeetCode 876's definition.

### Complexity
- **Time:** O(n) — slow traverses n/2 nodes
- **Space:** O(1)

### Source code
```java
public static ListNode middleNode(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow;
}
```

### Usage example
```java
// Odd length: 1->2->3->4->5 -> middle is 3
ListNode mid = Lists.middleNode(Lists.of(1, 2, 3, 4, 5));
mid.val; // 3

// Even length: 1->2->3->4 -> second middle is 3 (index 2, 0-based)
ListNode mid2 = Lists.middleNode(Lists.of(1, 2, 3, 4));
mid2.val; // 3

// Common use: split list in half for merge sort
ListNode mid3 = Lists.middleNode(head);
ListNode secondHalf = mid3.next;
mid3.next = null;  // break the link
```

### Related LeetCode problems
- **876** – Middle of the Linked List (this exact problem)
- **234** – Palindrome Linked List (find middle, reverse second half, compare)
- **148** – Sort List (merge sort: find middle, split, sort each half, merge)
- **143** – Reorder List (find middle, reverse second half, interleave)

### The fast/slow pointer pattern explained

The fast/slow pointer pattern detects cycle-related structural properties by exploiting the difference in speeds:

| Use case | Condition | What slow reaches |
|---|---|---|
| Find middle | `fast != null && fast.next != null` | Middle node |
| Detect cycle | `fast != null && fast.next != null` | Meeting point if cycle exists |
| Find cycle entry | After meeting, reset one to head, advance both at speed 1 | Cycle entry node |
| Find k-th from end | Advance fast k steps first, then move both | k-th from end |

The key invariant: when fast has traveled distance `2d`, slow has traveled `d`. So when fast reaches position `n`, slow is at `n/2` — the middle.

### Common variants
- **First middle (for even lists):** change the loop condition to `fast.next != null && fast.next.next != null` so `slow` stops at the first middle for even-length lists.
- **k-th from end:** advance `fast` exactly `k` steps before starting the dual advance. When `fast` reaches the end, `slow` is k steps from the end.
- **Cycle detection:** reuse the same loop structure; if `slow == fast` at any point during the traversal (after both have moved), a cycle exists.

### Gotchas
- The current implementation returns the **second** middle for even-length lists. Confirm which middle your problem requires — LC 876 explicitly asks for the second middle, but split-and-sort algorithms often need the first middle.
- For cycle detection, the loop terminates normally (returns `slow`) on acyclic lists. Do not use the return value for cycle detection — check `slow == fast` inside the loop instead.
- `fast.next.next` in the loop body is safe only because `fast.next != null` was checked first. Swapping the conditions (`fast.next.next != null && fast != null`) would cause a null pointer exception.
