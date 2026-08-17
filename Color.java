enum Color {
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
            case "r", "R", "🟥" -> RED   ;
            case "g", "G", "🟩" -> GREEN ;
            case "y", "Y", "🟨" -> YELLOW;
            case "b", "B", "🟦" -> BLUE  ;
            case "p", "P", "🟪" -> PURPLE;
            case "k", "K", "⬛" -> BLACK ;
            default -> throw new IllegalArgumentException(s);
        };
    }

    // Avoid making a copy by calling Color.values() all the time. Obviously don't modify this
    static final Color[] ALL = Color.values();

    static final int N_VALUES = ALL.length;

    static Color fromOrdinal(byte i) {
        return ALL[i];
    }
}