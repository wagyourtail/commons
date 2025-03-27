package xyz.wagyourtail.commons.core.logger;

public class NullLogger extends Logger {
    public static final NullLogger INSTANCE = new NullLogger();

    private NullLogger() {}

    @Override
    public Logger subLogger(Class<?> targetClass) {
        return this;
    }

    @Override
    public boolean isLevel(Level level) {
        return false;
    }

    @Override
    public void log(Level level, String message) {
        // no-op
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        // no-op
    }

}
