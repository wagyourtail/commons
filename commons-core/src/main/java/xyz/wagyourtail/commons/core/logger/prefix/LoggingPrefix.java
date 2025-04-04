package xyz.wagyourtail.commons.core.logger.prefix;

import xyz.wagyourtail.commons.core.logger.Logger;

public abstract class LoggingPrefix {
    public abstract String getPrefix(Logger.Level level);

    public abstract LoggingPrefix subSupplier(String targetClass);

    public static DefaultLoggingPrefix.DefaultLoggingPrefixBuilder builder() {
        return DefaultLoggingPrefix.builder();
    }

}
