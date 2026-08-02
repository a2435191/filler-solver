
import java.util.ArrayList;
import java.util.List;

final class Filler {
    static enum Color {
        RED, GREEN, YELLOW, BLUE, PURPLE, BLACK;

        String toEmoji() {
            return switch (this) {
                case RED    -> "🟥";
                case GREEN  -> "🟩";
                case YELLOW -> "🟨";
                case BLUE   -> "🟦";
                case PURPLE -> "🟪";
                case BLACK  -> "⬛";
            };
        }

        static Color fromEmoji(String s) {
            return switch (s) {
                case "🟥" -> RED   ;
                case "🟩" -> GREEN ;
                case "🟨" -> YELLOW;
                case "🟦" -> BLUE  ;
                case "🟪" -> PURPLE;
                case "⬛" -> BLACK ;
                default -> throw new IllegalArgumentException(s);
            };
        }
    }

    static final int HEIGHT = 7, WIDTH = 8,
                     TOTAL_SQUARES = HEIGHT * WIDTH,
                     SQUARES_TO_TIE = TOTAL_SQUARES / 2;

    /** 
     * - board: indexed from *bottom* row up, then right
     */
    record GameState(Color[][] board, int lowerLeftSquares, int upperRightSquares) {
        GameState(Color[][] board) {
            this(board, countConnectedTiles(board, 0, 0), countConnectedTiles(board, HEIGHT - 1, WIDTH - 1));
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
            return new GameState(board);
        }

        /** Fresh copy */
        static Color[][] copyBoard(Color[][] other) {
            Color[][] out = new Color[HEIGHT][WIDTH];
            for (int y = 0; y < HEIGHT; y++)
                System.arraycopy(other[y], 0, out[y], 0, WIDTH);
            return out;
        }

        /** Everything connected to square [y, x] gets its color set to `c` */
        GameState makeMove(Color c, int y, int x) {
            Color[][] tmp = copyBoard(this.board);
            fillAndCount(c, y, x, tmp, new boolean[HEIGHT][WIDTH]);
            return new GameState(tmp);
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
            return countConnectedTiles(board, y, x, new boolean[HEIGHT][WIDTH]);
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
                return Double.POSITIVE_INFINITY;
            if (upperRightSquares > SQUARES_TO_TIE)
                return Double.NEGATIVE_INFINITY;

            return (lowerLeftSquares - upperRightSquares) * (lowerLeftSquares + upperRightSquares);
            // if (lowerLeftSquares == SQUARES_TO_TIE && upperRightSquares == SQUARES_TO_TIE)
            //     return 0.0;
        }
    }

    record Result(double score, List<Color> bestMoves){}

    private static <T> List<T> addToEnd(List<T> l, T t) {
        List<T> out = new ArrayList<>(l);
        out.add(t);
        return out;
    }

    /** Minimax algorithm
      * - fuel: we stop once this is 0 so we don't go too deep
      * - maximize: true iff it's our turn and we want to maximize the score, otherwise (false) means it's our opponent's
      *   turn, and he/she wants to minimize the score
      * Returns the best moves in _reverse_ order */
    static Result minimax(GameState state, int fuel, boolean maximize) {
        // Game's over
        if (state.lowerLeftSquares() > SQUARES_TO_TIE 
                || state.upperRightSquares() > SQUARES_TO_TIE 
                || state.lowerLeftSquares() == SQUARES_TO_TIE && state.upperRightSquares() == SQUARES_TO_TIE)
            return new Result(state.score(), List.of());

        if (fuel == 0)
            return new Result(state.score(), List.of()); // heuristic
            

        final Color currentColor1 = state.board()[0][0];
        final Color currentColor2 = state.board()[HEIGHT - 1][WIDTH - 1];

        if (maximize) {
            Color bestMoveForMe = null;
            Result resultFromBestMove = new Result(Double.NEGATIVE_INFINITY, List.of());

            for (Color c : Color.values()) {
                // Can't do the color in either of the corners
                if (c == currentColor1 || c == currentColor2)
                    continue;
                GameState next = state.makeMove(c, 0, 0);
                
                Result afterMove = minimax(next, fuel - 1, false);
                if (bestMoveForMe == null || afterMove.score() > resultFromBestMove.score()) {
                    bestMoveForMe = c;
                    resultFromBestMove = afterMove;
                }
            }
            
            return new Result(resultFromBestMove.score(), addToEnd(resultFromBestMove.bestMoves(), bestMoveForMe));

        } else {
            Color bestMoveForOpponent = null;
            Result resultFromBestMove = new Result(Double.POSITIVE_INFINITY, List.of());

            for (Color c : Color.values()) {
                // Can't do the color in either of the corners
                if (c == currentColor1 || c == currentColor2)
                    continue;
                GameState next = state.makeMove(c, HEIGHT - 1, WIDTH - 1);

                Result afterMove = minimax(next, fuel - 1, true);
                if (bestMoveForOpponent == null || afterMove.score() < resultFromBestMove.score()) {
                    bestMoveForOpponent = c;
                    resultFromBestMove = afterMove;
                }
            }
            
            return new Result(resultFromBestMove.score(), addToEnd(resultFromBestMove.bestMoves(), bestMoveForOpponent));
        }
    }

    static final String EXAMPLE = """
        🟩🟥🟦🟪⬛🟦🟦🟦
        🟪🟦🟨🟩🟥🟪🟦⬛
        ⬛🟥🟩🟨🟦🟩⬛🟩
        🟥⬛🟦🟥🟩🟪🟨🟪
        🟩🟦⬛🟨🟪🟦🟩⬛
        🟦🟪🟩🟥🟨🟥🟦🟪
        ⬛⬛🟨🟩🟪🟩🟥⬛""";

    public static void main(String[] args) {
        GameState initial = GameState.parse(EXAMPLE);
        System.out.println(initial + "\n");

        // GameState curr = initial;
        // curr = curr.makeMove(Color.BLACK, 0, 0);

        // System.out.println(curr + "\n");

        // System.out.println(curr.countConnectedTiles(0, 0));

        Result r = minimax(initial, 14, true);
        System.out.println(r.score());
        
        boolean me = true;
        int nMoves = r.bestMoves().size();
        for (int i = 0; i < nMoves; i++) {
            Color move = r.bestMoves().get(nMoves - 1 - i);
            System.out.printf("%s move %s\n", me ? "I" : "U", move.toEmoji());

            me = !me;
        }
    }
}