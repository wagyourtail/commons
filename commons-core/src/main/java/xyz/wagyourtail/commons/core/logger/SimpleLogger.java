package xyz.wagyourtail.commons.core.logger;

import xyz.wagyourtail.commons.core.AnsiColor;

import java.io.PrintStream;

public class SimpleLogger extends Logger {
    private final String prefix;
    private final Level level;
    private final boolean useAnsiColors;
    private final PrintStream out;

    public SimpleLogger(String prefix, Level level, boolean useAnsiColors, PrintStream out) {
        this.prefix = prefix;
        this.level = level;
        this.useAnsiColors = useAnsiColors;
        this.out = out;
    }

    public SimpleLogger(Class<?> clazz, Level level, boolean useAnsiColors, PrintStream out) {
        this(clazz.getSimpleName(), level, useAnsiColors, out);
    }

    @Override
    public Logger subLogger(Class<?> targetClass) {
        return new SimpleLogger(prefix + "/" + targetClass.getSimpleName(), level, useAnsiColors, out);
    }

    protected AnsiColor getColor(Level level) {
        switch (level) {
            case ALL:
                return AnsiColor.PURPLE;
            case TRACE:
                return AnsiColor.DARK_GRAY;
            case DEBUG:
                return AnsiColor.LIGHT_GRAY;
            case INFO:
            case LIFECYCLE:
                return AnsiColor.WHITE;
            case WARNING:
                return AnsiColor.YELLOW;
            case ERROR:
                return AnsiColor.RED;
            case OFF:
                return AnsiColor.LIGHT_RED;
        }
        throw new IllegalArgumentException("Unknown level: " + level);
    }

    @Override
    public boolean isLevel(Level level) {
        return level.getLevel() >= this.level.getLevel();
    }

    @Override
    public void log(Level level, String message) {
        if (isLevel(level)) {
            String content = "[" + prefix + "] " + level + ": " + message;
            if (useAnsiColors) {
                out.println(getColor(level).wrap(content));
            } else {
                out.println(content);
            }
        }
    }

    @Override
    public void log(Level level, final String message, final Throwable throwable) {
        wrapPrintStream(level, new StreamWrap() {
            @Override
            public void writeTo(PrintStream ps) {
                ps.println(message);
                throwable.printStackTrace(ps);
            }
        });
    }

}