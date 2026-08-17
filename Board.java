abstract class Board {
    static final int HEIGHT = 7, WIDTH = 8,
                     TOTAL_SQUARES = HEIGHT * WIDTH,
                     SQUARES_TO_TIE = TOTAL_SQUARES / 2;
    
    abstract Board copy();
    abstract void set(int row, int col, Color c);
    abstract Color get(int row, int col);

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < HEIGHT; y++) {
            if (y != 0) sb.append('\n');
            for (int x = 0; x < WIDTH; x++) {
                Color c = get(HEIGHT - 1 - y, x);
                if (c == null)
                    sb.append("? ");
                else
                    sb.append(c.toEmoji());
            }
        }
        return sb.toString();
    }

    final static Board parse(String s) {
        Color[][] arr = s.lines().toList().reversed().stream()
            .map(line ->
                line.codePoints()
                    .mapToObj(Character::toString)
                    .map(Color::fromEmoji)
                    .toArray(Color[]::new))
            .toArray(Color[][]::new);
        return new SimpleBoard(arr);
    }

    
}