
import java.util.Arrays;

/** Use a 1-D byte array to represent all the colors on the board. Uses four bits per color. */
final class PackedBoard4 extends Board {
    private static final int LENGTH = TOTAL_SQUARES / 2; // length in bytes

    private final byte[] arr;

    private PackedBoard4(byte[] arr) {
        this.arr = arr;
    }

    PackedBoard4(Color[][] colors) {
        this(new byte[LENGTH]);
        for (int i = 0; i < HEIGHT; i++)
            for (int j = 0; j < WIDTH; j++)
                set(i, j, colors[i][j]);
    }

    @Override
    Board copy() {
        return new PackedBoard4(Arrays.copyOf(arr, LENGTH));
    }

    @Override
    Color get(int row, int col) {
        byte packed = arr[row * (WIDTH / 2) + col / 2];
        byte b;
        if (col % 2 == 0) // get high bits
            b = (byte)(packed >> 4);
        else              // get low bits
            b = (byte)(packed & 0x0f);
        return Color.fromOrdinal(b);
    }

    @Override
    void set(int row, int col, Color c) {
        byte b = (byte)c.ordinal();
        byte packed = arr[row * (WIDTH / 2) + col / 2];
        if (col % 2 == 0) // set high bits
            packed = (byte)((packed & 0x0f) | (b << 4));
        else              // set low bits
            packed = (byte)((packed & 0xf0) | b);
        arr[row * (WIDTH / 2) + col / 2] = packed;
    }
}
