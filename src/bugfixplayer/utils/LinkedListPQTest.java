package bugfixplayer.utils;

import ourplayer.utils.LinkedListPQ;

public class LinkedListPQTest {
    public static void assertEquals(int actual, int expected) {
        if (actual == expected) {
            System.out.println("passed");
        } else {
            System.out.printf("failed: expected: %d, actual: %d\n", expected, actual);
        }
    }

    public static void assertEquals(boolean actual, boolean expected) {
        if (actual == expected) {
            System.out.println("passed");
        } else {
            System.out.printf("failed: expected: %s, actual: %s\n", expected, actual);
        }
    }

    private static void testSimplePushPop() {
        LinkedListPQ pq = new LinkedListPQ();
        pq.push(0, 10);
        pq.push(1, 11);
        pq.push(2, 12);
        
        assertEquals(pq.pop(), 10);
        assertEquals(pq.pop(), 11);
        assertEquals(pq.pop(), 12);

        assertEquals(pq.isEmpty(), true);
    }

    private static void testComplexPushPop() {
        LinkedListPQ pq = new LinkedListPQ();
        pq.push(3, 13);
        pq.push(5, 15);
        pq.push(1, 11);
        pq.push(0, 10);

        assertEquals(pq.pop(), 10);
        assertEquals(pq.pop(), 11);
        assertEquals(pq.pop(), 13);
        assertEquals(pq.pop(), 15);

        assertEquals(pq.isEmpty(), true);
    }

    private static void testDecreaseKey() {
        LinkedListPQ pq = new LinkedListPQ();

        assertEquals(pq.isEmpty(), true);
        LinkedListPQ.Node toDecrease = pq.push(5, 15);
        pq.push(1, 11);
        pq.push(3, 13);
        assertEquals(pq.isEmpty(), false);
        pq.decreaseKey(toDecrease, 0);

        assertEquals(pq.pop(), 15);
        assertEquals(pq.pop(), 11);
        assertEquals(pq.pop(), 13);
        assertEquals(pq.isEmpty(), true);
    }

    private static void testAll() {
        LinkedListPQ pq = new LinkedListPQ();

        assertEquals(pq.isEmpty(), true);
        pq.push(10, 110);
        pq.push(11, 111);
        pq.push(4, 14);
        pq.push(1, 11);
        pq.push(2, 12);
        pq.push(3, 13);
        LinkedListPQ.Node toDecrease = pq.push(5, 15);

        assertEquals(pq.isEmpty(), false);

        assertEquals(pq.pop(), 11);
        assertEquals(pq.pop(), 12);
        pq.decreaseKey(toDecrease, 0);
        assertEquals(pq.pop(), 15);
        assertEquals(pq.isEmpty(), false);
        assertEquals(pq.pop(), 13);
        assertEquals(pq.pop(), 14);
        assertEquals(pq.pop(), 110);
        assertEquals(pq.pop(), 111);
        assertEquals(pq.isEmpty(), true);
    }

    public static void main(String[] args) {
        testSimplePushPop();
        testComplexPushPop();
        testDecreaseKey();
        testAll();
    }
}
