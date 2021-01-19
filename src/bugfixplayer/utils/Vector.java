package bugfixplayer.utils;

public class Vector {
    public int[] arr;
    public int size;
    public int cap;

    public Vector(int cap) {
        this.arr = new int[cap];
        this.cap = cap;
        this.size = 0;
    }

    public void append(int elem) {
        this.arr[size++] = elem;
    }

    public void remove(int i) {
        for (int j = i; j < size-1; ++j) {
            this.arr[j] = this.arr[j+1];
        }
        this.size--;
    }
}
