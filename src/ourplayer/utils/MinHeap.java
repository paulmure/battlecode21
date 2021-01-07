package ourplayer.utils;

public class MinHeap {
    public static class Heap {
        private int[] arr;
        private int size = 0;

        public Heap(int cap) {
            arr = new int[cap];
        }

        public void add(int elem) {
            int idx = size;
            int parentIdx = (idx - 1) / 2;
            arr[size++] = elem;

            while (parentIdx >= 0 && arr[parentIdx] > elem) {
                int tmp = arr[parentIdx];
                arr[parentIdx] = arr[idx];
                arr[idx] = tmp;
                idx = parentIdx;
                parentIdx = (idx - 1) / 2;
            }
        }

        public int poll() {
            int res = arr[0];
            arr[0] = arr[--size];

            int idx = 0;
            int leftChildIdx = 2 * idx + 1;
            int rightChildIdx = 2 * idx + 2;

            // heapify
            // to squeeze performance, skip recursion
            while (true) {
                if (leftChildIdx < size && arr[leftChildIdx] < arr[idx]) {
                    System.out.println("left");
                    int tmp = arr[idx];
                    arr[idx] = arr[leftChildIdx];
                    arr[leftChildIdx] = tmp;

                    idx = leftChildIdx;
                } else if (rightChildIdx < size && arr[rightChildIdx] < arr[idx]) {
                    System.out.println("right");
                    int tmp = arr[idx];
                    arr[idx] = arr[rightChildIdx];
                    arr[rightChildIdx] = tmp;

                    idx = rightChildIdx;
                } else {
                    System.out.println("break");
                    break;
                }

                leftChildIdx = 2 * idx + 1;
                rightChildIdx = 2 * idx + 2;
            }
            return res;
        }

        public void print() {
            for (int i = 0; i < size; ++i) {
                System.out.println(arr[i]);
            }
        }
    }

    public static void main(String[] args) {
        Heap minHeap = new Heap(10);

        minHeap.add(10);
        minHeap.add(1);
        minHeap.add(2);
        minHeap.add(5);
        minHeap.print();

        System.out.println("----------------");

        System.out.println("poll = " + minHeap.poll());
        minHeap.print();
        System.out.println("poll = " + minHeap.poll());
        minHeap.print();
    }
}