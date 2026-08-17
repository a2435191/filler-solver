
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class Filler {

    record Result(double score, List<Color> bestMoves){}

    private static <T> List<T> addToEnd(List<T> l, T t) {
        List<T> out = new ArrayList<>(l);
        out.add(t);
        return out;
    }

    static Result minimax(GameState initial, int maxDepth) {
        return minimax(initial, maxDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
    }

    record Child(Color c, GameState state) {}

    /** Minimax algorithm with alpha-beta pruning
      * - fuel: we stop once this is 0 so we don't go too deep
      * - alpha: we're able to pick a state with score at least this good
      * - beta: our opponent forces us to pick a state with score at least this bad
      * - maximize: true iff it's our turn and we want to maximize the score, otherwise (false) means it's our opponent's
      *   turn, and he/she wants to minimize the score
      * Returns the best moves in _reverse_ order */
    static Result minimax(GameState state, int fuel, double alpha, double beta, boolean maximize) {
        // Game's over
        if (state.lowerLeftSquares() > Board.SQUARES_TO_TIE 
                || state.upperRightSquares() > Board.SQUARES_TO_TIE 
                || state.lowerLeftSquares() == Board.SQUARES_TO_TIE && state.upperRightSquares() == Board.SQUARES_TO_TIE)
            return new Result(state.score(), List.of());

        if (fuel == 0)
            return new Result(state.score(), List.of()); // heuristic
            

        final Color currentColor1 = state.lowerLeftColor();
        final Color currentColor2 = state.upperRightColor();

        if (maximize) {
            List<Child> children = new ArrayList<>();
            for (Color c : Color.ALL) {
                // Can't do the color in either of the corners
                if (c == currentColor1 || c == currentColor2)
                    continue;
                GameState next = state.makeMove(c, true);
                children.add(new Child(c, next));
            }
            // Sort children by the heuristic (TODO or another heuristic?)
            children.sort(Comparator.comparingDouble(child -> ((Child)child).state().score()).reversed()); // best score first

            Color bestMoveForMe = null;
            Result resultFromBestMove = new Result(Double.NEGATIVE_INFINITY, List.of());
            for (Child child : children) {
                Result afterMove = minimax(child.state(), fuel - 1, alpha, beta, false);
                if (bestMoveForMe == null || afterMove.score() > resultFromBestMove.score()) {
                    bestMoveForMe = child.c();
                    resultFromBestMove = afterMove;
                }
                if (resultFromBestMove.score() >= beta) // this state is too good for us for our opponent to ever pick it
                    break;
                alpha = Math.max(alpha, resultFromBestMove.score());
            }
            
            return new Result(resultFromBestMove.score(), addToEnd(resultFromBestMove.bestMoves(), bestMoveForMe));

        } else {
            List<Child> children = new ArrayList<>();
            for (Color c : Color.ALL) {
                // Can't do the color in either of the corners
                if (c == currentColor1 || c == currentColor2)
                    continue;
                GameState next = state.makeMove(c, false);
                children.add(new Child(c, next));
            }
            // Sort children by the heuristic (TODO or another heuristic?)
            children.sort(Comparator.comparingDouble(child -> ((Child)child).state().score())); // worst score first
            
            Color bestMoveForOpponent = null;
            Result resultFromBestMove = new Result(Double.POSITIVE_INFINITY, List.of());
            for (Child child : children) {
                Result afterMove = minimax(child.state(), fuel - 1, alpha, beta, true);
                if (bestMoveForOpponent == null || afterMove.score() < resultFromBestMove.score()) {
                    bestMoveForOpponent = child.c();
                    resultFromBestMove = afterMove;
                }
                if (resultFromBestMove.score() <= alpha) // this state is too bad for us for us to ever pick it
                    break;
                beta = Math.min(beta, resultFromBestMove.score());
            }
            
            return new Result(resultFromBestMove.score(), addToEnd(resultFromBestMove.bestMoves(), bestMoveForOpponent));
        }
    }

    static final String EXAMPLE = """
        YGBRGKPP
        GPYBPGPP
        PKPGKPYK
        YGRYPYBY
        GKYKGRGK
        KYGPYKPY
        YYKRPRGB""";

    public static void main(String[] args) {
        Color[][] initialBoard = Board.parse(EXAMPLE);
        GameState initial = GameState.computeFields(initialBoard, 0);
        System.out.println(Board.toString(initialBoard) + "\n");

        // GameState curr = initial;
        // curr = curr.makeMove(Color.BLACK, 0, 0);

        // System.out.println(curr + "\n");

        // System.out.println(curr.countConnectedTiles(0, 0));

        Result r = minimax(initial, 30);
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