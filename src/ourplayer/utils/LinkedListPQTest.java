package ourplayer.utils;

import ourplayer.utils.LinkedListPQ;

public class LinkedListPQTest {
    public static void main(String[] args) {
        LinkedListPQ pq = new LinkedListPQ();
        pq.push(0, 5);
        pq.print();
        LinkedListPQ.Node node = pq.push(10, 3);
        pq.push(5, 6);
        pq.print();
        pq.decreaseKey(node, 3);
        pq.push(8, 123);
        pq.push(12, 123);
        pq.print();
        System.out.println("popped: " + pq.pop());
        pq.print();
    }
}
