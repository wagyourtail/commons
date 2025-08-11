package xyz.wagyourtail.commons.core.logger;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JavaLogWrapper extends Logger {
    private static final java.util.logging.Level LIFECYCLE = new java.util.logging.Level("LIFECYCLE", 850) {
    };
    private final java.util.logging.Logger logger;

    @Override
    public Logger subLogger(String subloggerName) {
        return new JavaLogWrapper(java.util.logging.Logger.getLogger(logger.getName() + "/" + subloggerName));
    }

    public java.util.logging.Level mapLevel(Level level) {
        switch (level) {
            case ALL:
                return java.util.logging.Level.ALL;
            case TRACE:
                return java.util.logging.Level.FINEST;
            case DEBUG:
                return java.util.logging.Level.FINE;
            case INFO:
                return java.util.logging.Level.INFO;
            case LIFECYCLE:
                return LIFECYCLE;
            case WARNING:
                return java.util.logging.Level.WARNING;
            case ERROR:
                return java.util.logging.Level.SEVERE;
            case OFF:
                return java.util.logging.Level.OFF;
        }
        throw new IllegalArgumentException("Unknown level: " + level);
    }

    @Override
    public boolean isLevel(Level level) {
        return logger.isLoggable(mapLevel(level));
    }

    @Override
    public void log(Level level, String message) {
        logger.log(mapLevel(level), message);
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        logger.log(mapLevel(level), message, throwable);
    }

}
