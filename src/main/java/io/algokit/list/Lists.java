package io.algokit.list;

import java.util.ArrayList;
import java.util.List;

/**
 * Linked list utilities: construction, conversion, and common operations.
 */
public final class Lists {
    private Lists() {}

    /** Builds a linked list from varargs values. */
    public static ListNode of(int... values) {
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        for (int v : values) {
            tail.next = new ListNode(v);
            tail = tail.next;
        }
        return dummy.next;
    }

    /** Converts an acyclic linked list to an int array. Fails loudly if the list appears cyclic. */
    public static int[] toArray(ListNode head) {
        List<Integer> out = new ArrayList<>();
        int limit = 10_000_000;
        for (ListNode n = head; n != null; n = n.next) {
            out.add(n.val);
            if (out.size() > limit) throw new IllegalStateException("list too long: cycle?");
        }
        int[] a = new int[out.size()];
        for (int i = 0; i < a.length; i++) a[i] = out.get(i);
        return a;
    }

    /** Collects all nodes into a list. Useful for index-based access in tests. */
    public static List<ListNode> nodes(ListNode head) {
        List<ListNode> out = new ArrayList<>();
        for (ListNode n = head; n != null; n = n.next) out.add(n);
        return out;
    }

    /** Removes every node with the given value. Head may change (dummy-head pattern). */
    public static ListNode removeAll(ListNode head, int target) {
        ListNode dummy = new ListNode(0, head);
        ListNode prev = dummy;
        while (prev.next != null) {
            if (prev.next.val == target) prev.next = prev.next.next;
            else prev = prev.next;
        }
        return dummy.next;
    }

    /** Middle node via fast/slow pointers. For even length, returns the second middle (LC 876). */
    public static ListNode middleNode(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }
}
