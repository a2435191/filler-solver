
import java.util.Arrays;

/** Use a 1-D byte array to represent all the colors on the board. Uses eight bits per color. */
final class PackedBoard8 extends Board {
    private static final int LENGTH = TOTAL_SQUARES / 2; // length in bytes

    private final byte[] arr;

    private PackedBoard8(byte[] arr) {
        this.arr = arr;
    }

    PackedBoard8(Color[][] colors) {
        this(new byte[LENGTH]);
        for (int i = 0; i < HEIGHT; i++)
            for (int j = 0; j < WIDTH; j++)
                set(i, j, colors[i][j]);
    }

    @Override
    Board copy() {
        return new PackedBoard8(Arrays.copyOf(arr, LENGTH));
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
