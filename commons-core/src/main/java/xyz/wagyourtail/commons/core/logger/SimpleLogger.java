package xyz.wagyourtail.commons.core.logger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import xyz.wagyourtail.commons.core.AnsiColor;
import xyz.wagyourtail.commons.core.logger.prefix.DefaultLoggingPrefix;
import xyz.wagyourtail.commons.core.logger.prefix.LoggingPrefix;

import java.io.PrintStream;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleLogger extends Logger {
    @Builder.Default
    private final LoggingPrefix prefix = new DefaultLoggingPrefix();
    @Builder.Default
    private final Level level = Level.INFO;
    @Builder.Default
    private final boolean useAnsiColors = true;
    @Builder.Default
    private final PrintStream out = System.out;

    public static Logger forCaller() {
        return forClass(Objects.requireNonNull(DefaultLoggingPrefix.getCallingClass()));
    }

    public static Logger forClass(Class<?> clazz) {
        return builder()
                .prefix(DefaultLoggingPrefix.builder()
                        .loggerName(clazz.getSimpleName())
                        .build()
                )
                .build();
    }

    @Override
    public Logger subLogger(final String subloggerName) {
        return new SimpleLogger(prefix.subSupplier(subloggerName), level, useAnsiColors, out);
    }

    public Logger subLogger(final Class<?> clazz) {
        return subLogger(clazz.getSimpleName());
    }

    public Logger subLogger() {
        return subLogger(Objects.requireNonNull(DefaultLoggingPrefix.getCallingClass()).getSimpleName());
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
            String content = prefix.getPrefix(level) + message;
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
