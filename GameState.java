import java.util.Arrays;

/** 
 * - board: indexed from *bottom* row up, then right
 * - ply: number of single-player turns
 */
record GameState(Board board, int ply, int lowerLeftSquares, int upperRightSquares) {
    static final double WIN_SCORE = 1e9;

    GameState(Board board, int ply) {
        this(board, ply, countConnectedTiles(board, 0, 0), countConnectedTiles(board, Board.HEIGHT - 1, Board.WIDTH - 1));
    }


    private static boolean[][] VISITED_SCRATCH = new boolean[Board.HEIGHT][Board.WIDTH];
    private static void clearScratch() {
        for (boolean[] row : VISITED_SCRATCH)
            Arrays.fill(row, false);
    }

    /** Everything connected to square [y, x] gets its color set to `c` */
    GameState makeMove(Color c, int y, int x) {
        Board tmp = board.copy();

        clearScratch();
        fillAndCount(c, y, x, tmp, VISITED_SCRATCH);

        return new GameState(tmp, this.ply + 1);
    }

    // TODO: compute count here as well
    private void fillAndCount(Color c, int y, int x, Board next, boolean[][] visited) {
        next.set(y, x, c);
        visited[y][x] = true;

        if (y + 1 < Board.HEIGHT && !visited[y + 1][x] && board.get(y + 1, x) == board.get(y, x))
            fillAndCount(c, y + 1, x, next, visited);
        if (y - 1 >= 0 && !visited[y - 1][x] && board.get(y - 1, x) == board.get(y, x))
            fillAndCount(c, y - 1, x, next, visited);
        if (x + 1 < Board.WIDTH && !visited[y][x + 1] && board.get(y, x + 1) == board.get(y, x))
            fillAndCount(c, y, x + 1, next, visited);
        if (x - 1 >= 0 && !visited[y][x - 1] && board.get(y, x - 1) == board.get(y, x))
            fillAndCount(c, y, x - 1, next, visited);
    }

    static int countConnectedTiles(Board board, int y, int x) {
        clearScratch();
        return countConnectedTiles(board, y, x, VISITED_SCRATCH);
    }

    private static int countConnectedTiles(Board board, int y, int x, boolean[][] visited) {
        int sum = 1; 
        visited[y][x] = true;

        if (y + 1 < Board.HEIGHT && !visited[y + 1][x] && board.get(y + 1, x) == board.get(y, x))
            sum += countConnectedTiles(board, y + 1, x, visited);
        if (y - 1 >= 0 && !visited[y - 1][x] && board.get(y - 1, x) == board.get(y, x))
            sum += countConnectedTiles(board, y - 1, x, visited);
        if (x + 1 < Board.WIDTH && !visited[y][x + 1] && board.get(y, x + 1) == board.get(y, x))
            sum += countConnectedTiles(board, y, x + 1, visited);
        if (x - 1 >= 0 && !visited[y][x - 1] && board.get(y, x - 1) == board.get(y, x))
            sum += countConnectedTiles(board, y, x - 1, visited);

        return sum;
    }

    // Heuristic: we want to reward us controlling more squares, punish our opponent controlling more squares,
    // and TODO
    double score() {
        if (lowerLeftSquares > Board.SQUARES_TO_TIE)
            return WIN_SCORE - this.ply; // incentivize winning early
        if (upperRightSquares > Board.SQUARES_TO_TIE)
            return -(WIN_SCORE - this.ply); // or losing late, i.e. dragging out lost games I suppose

        return lowerLeftSquares - upperRightSquares;
        // if (lowerLeftSquares == SQUARES_TO_TIE && upperRightSquares == SQUARES_TO_TIE)
        //     return 0.0;
    }
}