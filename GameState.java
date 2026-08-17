import java.util.Arrays;

/** 
 * - present: for each of six colors, the bit at index row * 8 + col represents whether (row, col) has that color. 
 *  i.e. each byte (of the lower seven bytes) in the long represents a row
 * - ply: number of single-player turns
 */
record GameState(long[] present, int ply, Color lowerLeftColor, Color upperRightColor, long lowerLeftRegion, long upperRightRegion) {
    static final double WIN_SCORE = 1e9;

    GameState(long[] present, int ply, Color lowerLeftColor, Color upperRightColor, long lowerLeftRegion, long upperRightRegion) {
        assert present.length == Color.N_VALUES;
        this.present = present;
        this.ply = ply;
        this.lowerLeftColor = lowerLeftColor;
        this.upperRightColor = upperRightColor;
        this.lowerLeftRegion = lowerLeftRegion;
        this.upperRightRegion = upperRightRegion;
    }

    static long[] toBits(Color[][] colors) {
        long[] out = new long[Color.N_VALUES];
        for (int y = 0; y < Board.HEIGHT; y++)
            for (int x = 0; x < Board.WIDTH; x++) {
                int i = y * Board.WIDTH + x;
                byte color = (byte)(colors[y][x].ordinal());
                out[color] |= (1L << i);  // set ith bit
            }
        return out;
    }

    static Color[][] ofBits(long[] bits) {
        Color[][] out = new Color[Board.HEIGHT][Board.WIDTH];
        for (int y = 0; y < Board.HEIGHT; y++)
            for (int x = 0; x < Board.WIDTH; x++) {
                int i = y * Board.WIDTH + x;
                for (byte c = 0; c < Color.N_VALUES; c++) {
                    if ((bits[c] & (1L << i)) != 0) {
                        assert out[y][x] == null;
                        out[y][x] = Color.fromOrdinal(c);
                    }
                }
                assert out[y][x] != null;
            }
        return out;
    }

    static String bitsToString(long bits) {
        StringBuilder sb = new StringBuilder();
        for (int y = Board.HEIGHT - 1; y >= 0; y--) {
            if (y != Board.HEIGHT - 1)
                sb.append("\n");
            for (int x = 0; x < Board.WIDTH; x++) {
                int i = y * Board.WIDTH + x;
                if ((bits & (1L << i)) == 0)
                    sb.append(". ");
                else
                    sb.append("X ");
            }
        }
        return sb.toString();
    }

    private static final int LL_CORNER_IDX = 0, UR_CORNER_IDX = Board.WIDTH * Board.HEIGHT - 1;

    static GameState computeFields(Color[][] colors, int ply) {
        long[] bits = toBits(colors);
        Color llColor = colors[0][0];
        Color urColor = colors[Board.HEIGHT - 1][Board.WIDTH - 1];
        long lowerLeftRegion = connectedMask(bits[llColor.ordinal()], LL_CORNER_IDX);
        long upperRightRegion = connectedMask(bits[urColor.ordinal()], UR_CORNER_IDX);
        return new GameState(bits, ply, llColor, urColor, lowerLeftRegion, upperRightRegion);
    }

    static {
        assert Long.BYTES * 8 >= Board.TOTAL_SQUARES;
    }

    private static final long  LEFT_COLUMN = 0x01_01_01_01_01_01_01L,
                              RIGHT_COLUMN = 0x80_80_80_80_80_80_80L;

    static long connectedMask(long l, int i) {
        long connected = l & (1L << i);
        while (true) { 
            // left neighbors are at lower indices, so from their perspective we are at higher indices,
            // meaning we need to right-shift ourselves back down. We're going to mask out any bits representing
            // the rightmost column though— those of course cannot be left neighbors
            long leftNeighbors = (connected >>> 1L) & ~RIGHT_COLUMN;

            long rightNeighbors = (connected << 1L) & ~LEFT_COLUMN;
            long upNeighbors = connected >>> 8L;
            long downNeighbors = connected << 8L;

            long next = connected | ((leftNeighbors | rightNeighbors | upNeighbors | downNeighbors) & l);
            if (next == connected)
                return connected;
            connected = next;
        }
    }

    /** Everything connected (via the relation "has the same color as adjacent") to square [y, x] gets its color set to `c` */
    GameState makeMove(Color c, boolean whichCorner /* true iff lower-left, otherwise upper-right */) {
        byte newColor = (byte)c.ordinal();
        byte oldColor = (byte)(whichCorner ? lowerLeftColor : upperRightColor).ordinal();

        // The corner region whose color we change
        long cornerRegion = whichCorner ? lowerLeftRegion : upperRightRegion;
        
        long[] next = Arrays.copyOf(present, Color.N_VALUES);
        next[oldColor] &= ~cornerRegion;
        next[newColor] |= cornerRegion;

        // Need to compute the next corner region (same corner as before, since we can't modify our opponent's corner region ever)
        long nextCornerRegion = connectedMask(next[newColor], whichCorner ? LL_CORNER_IDX : UR_CORNER_IDX); // TODO start this from the existing cornerRegion
        
        return new GameState(next, ply + 1, 
            whichCorner ? c : lowerLeftColor, 
            whichCorner ? upperRightColor : c,
            whichCorner ? nextCornerRegion : lowerLeftRegion,
            whichCorner ? upperRightRegion : nextCornerRegion
        );
    }

    int lowerLeftSquares() {
        return Long.bitCount(lowerLeftRegion);
    }

    int upperRightSquares() {
        return Long.bitCount(upperRightRegion);
    }

    // Heuristic: we want to reward us controlling more squares, punish our opponent controlling more squares,
    // and TODO
    double score() {
        if (lowerLeftSquares() > Board.SQUARES_TO_TIE)
            return WIN_SCORE - this.ply; // incentivize winning early
        if (upperRightSquares() > Board.SQUARES_TO_TIE)
            return -(WIN_SCORE - this.ply); // or losing late, i.e. dragging out lost games I suppose

        return lowerLeftSquares() - upperRightSquares();
        // if (lowerLeftSquares == SQUARES_TO_TIE && upperRightSquares == SQUARES_TO_TIE)
        //     return 0.0;
    }
}