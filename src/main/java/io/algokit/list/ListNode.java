package io.algokit.list;

/**
 * Singly linked list node matching the LeetCode shape.
 * Public fields for direct access, as is conventional in interview code.
 */
public class ListNode {
    public int val;
    public ListNode next;

    public ListNode(int val) {
        this.val = val;
    }

    public ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }
}
