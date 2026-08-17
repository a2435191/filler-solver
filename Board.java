final class Board {
    static final int HEIGHT = 7, WIDTH = 8,
                     TOTAL_SQUARES = HEIGHT * WIDTH,
                     SQUARES_TO_TIE = TOTAL_SQUARES / 2;

    static final String toString(Color[][] colors) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < HEIGHT; y++) {
            if (y != 0) sb.append('\n');
            for (int x = 0; x < WIDTH; x++) {
                Color c = colors[HEIGHT - 1 - y][x];
                if (c == null)
                    sb.append("? ");
                else
                    sb.append(c.toEmoji());
            }
        }
        return sb.toString();
    }

    final static Color[][] parse(String s) {
        Color[][] arr = s.lines().toList().reversed().stream()
            .map(line ->
                line.codePoints()
                    .mapToObj(Character::toString)
                    .map(Color::fromEmoji)
                    .toArray(Color[]::new))
            .toArray(Color[][]::new);
        return arr;
    }

    
}