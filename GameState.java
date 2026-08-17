import java.util.Arrays;

/** 
 * - board: indexed from *bottom* row up, then right
 * - ply: number of single-player turns
 */
record GameState(Color[][] board, int ply, int lowerLeftSquares, int upperRightSquares) {
    static final int HEIGHT = 7, WIDTH = 8,
                     TOTAL_SQUARES = HEIGHT * WIDTH,
                     SQUARES_TO_TIE = TOTAL_SQUARES / 2;
    static final double WIN_SCORE = 1e9;

    GameState(Color[][] board, int ply) {
        this(board, ply, countConnectedTiles(board, 0, 0), countConnectedTiles(board, HEIGHT - 1, WIDTH - 1));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < HEIGHT; y++) {
            if (y != 0) sb.append('\n');
            Color[] row = board[HEIGHT - 1 - y];
            for (int x = 0; x < WIDTH; x++) {
                if (row[x] == null)
                    sb.append("? ");
                else
                    sb.append(row[x].toEmoji());
            }
        }
        return sb.toString();
    }

    static GameState parse(String s) {
        Color[][] board = s.lines().toList().reversed().stream()
            .map(line ->
                line.codePoints()
                    .mapToObj(Character::toString)
                    .map(Color::fromEmoji)
                    .toArray(Color[]::new))
            .toArray(Color[][]::new);
        return new GameState(board, 0);
    }

    /** Fresh copy */
    static Color[][] copyBoard(Color[][] other) {
        Color[][] out = new Color[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++)
            System.arraycopy(other[y], 0, out[y], 0, WIDTH);
        return out;
    }

    private static boolean[][] VISITED_SCRATCH = new boolean[HEIGHT][WIDTH];
    private static void clearScratch() {
        for (boolean[] row : VISITED_SCRATCH)
            Arrays.fill(row, false);
    }

    /** Everything connected to square [y, x] gets its color set to `c` */
    GameState makeMove(Color c, int y, int x) {
        Color[][] tmp = copyBoard(this.board);

        clearScratch();
        fillAndCount(c, y, x, tmp, VISITED_SCRATCH);
        
        return new GameState(tmp, this.ply + 1);
    }

    // TODO: compute count here as well
    private void fillAndCount(Color c, int y, int x, Color[][] next, boolean[][] visited) {
        next[y][x] = c;
        visited[y][x] = true;

        if (y + 1 < HEIGHT && !visited[y + 1][x] && board[y + 1][x] == board[y][x])
            fillAndCount(c, y + 1, x, next, visited);
        if (y - 1 >= 0 && !visited[y - 1][x] && board[y - 1][x] == board[y][x])
            fillAndCount(c, y - 1, x, next, visited);
        if (x + 1 < WIDTH && !visited[y][x + 1] && board[y][x + 1] == board[y][x])
            fillAndCount(c, y, x + 1, next, visited);
        if (x - 1 >= 0 && !visited[y][x - 1] && board[y][x - 1] == board[y][x])
            fillAndCount(c, y, x - 1, next, visited);
    }

    static int countConnectedTiles(Color[][] board, int y, int x) {
        clearScratch();
        return countConnectedTiles(board, y, x, VISITED_SCRATCH);
    }

    private static int countConnectedTiles(Color[][] board, int y, int x, boolean[][] visited) {
        int sum = 1; 
        visited[y][x] = true;

        if (y + 1 < HEIGHT && !visited[y + 1][x] && board[y + 1][x] == board[y][x])
            sum += countConnectedTiles(board, y + 1, x, visited);
        if (y - 1 >= 0 && !visited[y - 1][x] && board[y - 1][x] == board[y][x])
            sum += countConnectedTiles(board, y - 1, x, visited);
        if (x + 1 < WIDTH && !visited[y][x + 1] && board[y][x + 1] == board[y][x])
            sum += countConnectedTiles(board, y, x + 1, visited);
        if (x - 1 >= 0 && !visited[y][x - 1] && board[y][x - 1] == board[y][x])
            sum += countConnectedTiles(board, y, x - 1, visited);

        return sum;
    }

    // Heuristic: we want to reward us controlling more squares, punish our opponent controlling more squares,
    // and TODO
    double score() {
        if (lowerLeftSquares > SQUARES_TO_TIE)
            return WIN_SCORE - this.ply; // incentivize winning early
        if (upperRightSquares > SQUARES_TO_TIE)
            return -(WIN_SCORE - this.ply); // or losing late, i.e. dragging out lost games I suppose

        return lowerLeftSquares - upperRightSquares;
        // if (lowerLeftSquares == SQUARES_TO_TIE && upperRightSquares == SQUARES_TO_TIE)
        //     return 0.0;
    }
}