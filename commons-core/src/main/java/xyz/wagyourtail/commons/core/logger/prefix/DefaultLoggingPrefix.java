package xyz.wagyourtail.commons.core.logger.prefix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import xyz.wagyourtail.commons.core.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DefaultLoggingPrefix extends LoggingPrefix {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Builder.Default
    private final String loggerName = getCallingClass().getSimpleName();
    @Builder.Default
    private final boolean includeTime = true;
    @Builder.Default
    private final boolean includeLevel = true;
    @Builder.Default
    private final boolean includeClassName = false;
    @Builder.Default
    private final boolean includeThreadName = true;

    @Override
    public String getPrefix(Logger.Level level) {
        Date date = new Date();
        StringBuilder sb = new StringBuilder();
        if (includeTime) {
            sb.append("[")
            .append(DATE_FORMAT.format(date))
            .append("] ");
        }
        if (!loggerName.isEmpty()) {
            sb.append("[")
            .append(loggerName)
            .append("] ");
        }
        if (includeLevel) {
            sb.append("[")
            .append(level.name())
            .append("] ");
        }
        if (includeClassName) {
            sb.append("[")
            .append(getCallingClassName())
            .append("] ");
        }
        if (includeThreadName) {
            sb.append("[")
            .append(Thread.currentThread().getName())
            .append("] ");
        }
        return sb.toString();
    }

    private static boolean isInternalClass(Class<?> clazz) {
        if (LoggingPrefix.class.isAssignableFrom(clazz)) {
            return true;
        }
        if (Logger.class.isAssignableFrom(clazz)) {
            return true;
        }
        if (SecurityManagerAdapter.class.isAssignableFrom(clazz)) {
            return true;
        }
        Class<?> enclosing = clazz.getEnclosingClass();
        return enclosing != null && isInternalClass(enclosing);
    }

    public static Class<?> getCallingClass() {
        Class<?>[] stackTrace = SecurityManagerAdapter.INSTANCE.getClassContext();
        for (Class<?> element : stackTrace) {
            if (isInternalClass(element)) {
                continue;
            }
            return element;
        }
        return null;
    }

    protected String getCallingClassName() {
        Class<?> element = getCallingClass();
        if (element != null) {
            String[] nameParts = element.getName().split("\\.");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < nameParts.length; j++) {
                if (j == nameParts.length - 1) {
                    sb.append(nameParts[j]);
                } else {
                    sb.append(Character.toChars(nameParts[j].codePointAt(0)))
                    .append(".");
                }
            }
            return sb.toString();
        }
        return "Unknown";
    }

    @Override
    public LoggingPrefix subSupplier(String subloggerName) {
        return toBuilder()
            .loggerName(loggerName + "/" + subloggerName)
            .build();
    }


    static class SecurityManagerAdapter extends SecurityManager {
        static final SecurityManagerAdapter INSTANCE = new SecurityManagerAdapter();

        @Override
        public Class<?>[] getClassContext() {
            return super.getClassContext();
        }

    }

}
