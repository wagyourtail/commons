package xyz.wagyourtail.commons.core.logger;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

public abstract class Logger {

    public abstract Logger subLogger(Class<?> targetClass);

    public abstract boolean isLevel(Level level);

    public abstract void log(Level level, String message);

    public void log(Level level, Object message) {
        log(level, message.toString());
    }

    public void log(Level level, MessageSupplier messageSupplier) {
        if (isLevel(level)) {
            log(level, messageSupplier.getMessage());
        }
    }

    public void log(Level level, String message, Object... args) {
        if (isLevel(level)) {
            log(level, MessageFormat.format(message, args));
        }
    }

    public abstract void log(Level level, String message, Throwable throwable);

    public void trace(String message, Object... args) {
        log(Level.TRACE, message, args);
    }

    public void debug(String message, Object... args) {
        log(Level.DEBUG, message, args);
    }

    public void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public void lifecycle(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public void warning(String message, Object... args) {
        log(Level.WARNING, message, args);
    }

    public void warning(String message, Throwable throwable) {
        log(Level.WARNING, message, throwable);
    }

    public void error(String message, Object... args) {
        log(Level.ERROR, message, args);
    }

    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    @SneakyThrows
    public void wrapPrintStream(Level level, StreamWrap ps) {
        if (isLevel(level)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream ps2 = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
                ps.writeTo(ps2);
            }
            String str = baos.toString(StandardCharsets.UTF_8.name());
            log(level, str);
        }
    }

    @Getter
    public enum Level {
        ALL(Integer.MIN_VALUE),
        TRACE(400),
        DEBUG(500),
        INFO(800),
        LIFECYCLE(850),
        WARNING(900),
        ERROR(1000),
        OFF(Integer.MAX_VALUE);

        private final int level;

        Level(int level) {
            this.level = level;
        }

    }

    public static MessageSupplier messageSupplierOf(final String message) {
        return new MessageSupplier() {
            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    public interface MessageSupplier {
        String getMessage();
    }

    public interface StreamWrap {
        void writeTo(PrintStream ps);

    }

}
