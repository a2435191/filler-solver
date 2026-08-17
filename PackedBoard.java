
import java.util.Arrays;

/** Use a 1-D byte array to represent all the colors on the board. Uses eight bits per color. */
final class PackedBoard extends Board {
    private static final int LENGTH = TOTAL_SQUARES; // length in bytes

    private final byte[] arr;

    private PackedBoard(byte[] arr) {
        this.arr = arr;
    }

    PackedBoard(Color[][] colors) {
        this(new byte[LENGTH]);
        for (int i = 0; i < HEIGHT; i++)
            for (int j = 0; j < WIDTH; j++)
                set(i, j, colors[i][j]);
    }

    @Override
    Board copy() {
        return new PackedBoard(Arrays.copyOf(arr, LENGTH));
    }

    @Override
    Color get(int row, int col) {
        byte b = arr[row * WIDTH + col];
        return Color.fromOrdinal(b);
    }

    @Override
    void set(int row, int col, Color c) {
        byte b = (byte)c.ordinal();
        arr[row * WIDTH + col] = b;
    }
}
