package ourplayer.utils;

public class Node {
    public Node next;
    public Node prev;
    public int value;

    public Node(int value) {
        this(value, null, null);
    }

    public Node(int value, Node prev) {
        this(value, null, prev);
    }

    public Node(int value, Node next, Node prev) {
        this.value = value;
        this.next = next;
        this.prev = prev;
    }

    public Node remove() {
        prev.next = next;
        if (next != null)
            next.prev = prev;
        return prev;
    }

    public void add(int value) {
        if (next != null) {
            next = new Node(value, next, this);
            next.next.prev = next;
        } else {
            next = new Node(value, this);
        } 
    }
}
