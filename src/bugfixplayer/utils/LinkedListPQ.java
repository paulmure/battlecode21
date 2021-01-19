package bugfixplayer.utils;

public class LinkedListPQ {
    public static class Node {
        public double key;
        public int value;
        public Node next;
        public Node prev;

        public Node(double key, int value, Node next, Node prev) {
            this.key = key;
            this.value = value;
            this.next = next;
            this.prev = prev;
        }
    }

    Node head;
    Node min;

    public LinkedListPQ() {
        this.head = null;
        this.min = null;
    }

    public boolean isEmpty() {
        return this.head == null;
    }

    public Node push(double key, int value) {
        Node node = new Node(key, value, this.head, null);
        if (this.head != null) {
            this.head.prev = node;
        }
        this.head = node;

        if (this.min == null) {
            this.min = node;
        } else if (key < this.min.key) {
            this.min = node;
        }

        return node;
    }

    public void decreaseKey(Node node, double key) {
        node.key = key;
        if (this.min != null && node.key < this.min.key) {
            this.min = node;
        }
    }

    public int pop() {
        int res = this.min.value;

        if (this.min.prev != null && this.min.next != null) {
            this.min.prev.next = this.min.next;
            this.min.next.prev = this.min.prev;
        } else if (this.min.prev != null) {
            this.min.prev.next = null;
        } else if (this.min.next != null) {
            this.min.next.prev = null;
        }

        if (this.min == this.head) {
            if (this.min.next != null) {
                this.head = this.min.next;
            } else {
                this.head = null;
            }
        }

        Node tmp = this.head;
        double minKey = Integer.MAX_VALUE;
        this.min = null;

        while (tmp != null) {
            if (tmp.key < minKey) {
                this.min = tmp;
                minKey = tmp.key;
            }
            tmp = tmp.next;
        }

        return res;
    }

    public String toString() {
        Node tmp = this.head;
        if (tmp == null) {
            return "Empty queue...";
        }

        String res = "";
        if (tmp.prev != null) {
            res += "error, there is something before the head: ";
            res += tmp.prev;
        }
        while (tmp != null) {
            res += String.format("(%f, %d) <-> ", tmp.key, tmp.value);
            tmp = tmp.next;
        }
        res += "\b\b\b\b    \n";

        res += String.format("min key: %f, min value: %d\n", this.min.key, this.min.value);
        return res;
    }
}
