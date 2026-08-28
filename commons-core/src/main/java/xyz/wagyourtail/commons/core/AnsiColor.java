package xyz.wagyourtail.commons.core;

import xyz.wagyourtail.commons.core.console.ConsoleFormatting;

public enum AnsiColor {
    /**
     * @deprecated Use {@link ConsoleFormatting#RESET} instead
     */
    @Deprecated
    RESET(0),

    BLACK(30),
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    PURPLE(35),
    CYAN(36),
    LIGHT_GRAY(37),

    DARK_GRAY(90),
    LIGHT_RED(91),
    LIGHT_GREEN(92),
    LIGHT_YELLOW(93),
    LIGHT_BLUE(94),
    LIGHT_PURPLE(95),
    LIGHT_CYAN(96),
    WHITE(97);

    public final ConsoleFormatting foreground;
    public final ConsoleFormatting background;

    AnsiColor(int value) {
        foreground = new ConsoleFormatting(value);

        if (value == 0) {
            background = new ConsoleFormatting(value);
        } else {
            background = new ConsoleFormatting(value + 10);
        }
    }

    /**
     * @deprecated Use {@link AnsiColor#foreground}.{@link ConsoleFormatting#wrap(String)} instead
     */
    @Deprecated
    public String wrap(String message) {
        return foreground.wrap(message);
    }

}
