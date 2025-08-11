package xyz.wagyourtail.commons.core.logger;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

public abstract class Logger {

    public abstract Logger subLogger(String subloggerName);

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

    public void trace(MessageSupplier messageSupplier) {
        log(Level.TRACE, messageSupplier);
    }

    public void trace(String message) {
        log(Level.TRACE, message);
    }

    public void trace(String message, Object... args) {
        log(Level.TRACE, message, args);
    }

    public void debug(MessageSupplier messageSupplier) {
        log(Level.DEBUG, messageSupplier);
    }

    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    public void debug(String message, Object... args) {
        log(Level.DEBUG, message, args);
    }

    public void info(MessageSupplier messageSupplier) {
        log(Level.INFO, messageSupplier);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public void lifecycle(MessageSupplier messageSupplier) {
        log(Level.LIFECYCLE, messageSupplier);
    }

    public void lifecycle(String message) {
        log(Level.LIFECYCLE, message);
    }

    public void lifecycle(String message, Object... args) {
        log(Level.LIFECYCLE, message, args);
    }

    public void warning(MessageSupplier messageSupplier) {
        log(Level.WARNING, messageSupplier);
    }

    public void warning(String message) {
        log(Level.WARNING, message);
    }

    public void warning(String message, Object... args) {
        log(Level.WARNING, message, args);
    }

    public void warning(String message, Throwable throwable) {
        log(Level.WARNING, message, throwable);
    }

    public void error(MessageSupplier messageSupplier) {
        log(Level.ERROR, messageSupplier);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public void error(String message, Object... args) {
        log(Level.ERROR, message, args);
    }

    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    @SneakyThrows
    public void wrapPrintStream(final Level level, final StreamWrap ps) {
        if (isLevel(level)) {
            ps.writeTo(new PrintStream(new OutputStream() {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();

                @Override
                public void write(int b) throws UnsupportedEncodingException {
                    if (b == '\n' || b == -1) {
                        log(level, baos.toString(StandardCharsets.UTF_8.name()));
                        baos.reset();
                    } else {
                        baos.write(b);
                    }
                }

                @Override
                public void close() throws IOException {
                    super.close();
                    if (baos.size() > 0) {
                        log(level, baos.toString(StandardCharsets.UTF_8.name()));
                    }
                }

            }, true, StandardCharsets.UTF_8.name()));


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

    public interface StreamWrap {
        void writeTo(PrintStream ps);
    }

    public interface MessageSupplier {
        String getMessage();
    }

}
