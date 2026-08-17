

/** Just implement `Board` in the naive way */
class SimpleBoard extends Board {
    private final Color[][] arr;

    SimpleBoard(Color[][] arr) {
        this.arr = arr;
    }

    @Override
    Board copy() {
        Color[][] next = new Color[HEIGHT][WIDTH];
        for (int i = 0; i < HEIGHT; i++)
            System.arraycopy(arr[i], 0, next[i], 0, WIDTH);
        return new SimpleBoard(next);
    }

    @Override
    Color get(int row, int col) {
        return arr[row][col];
    }

    @Override
    void set(int row, int col, Color c) {
        arr[row][col] = c;
    }
    
}
