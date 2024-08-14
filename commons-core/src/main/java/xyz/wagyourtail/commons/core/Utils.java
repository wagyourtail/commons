package xyz.wagyourtail.commons.core;

public class Utils {

    public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

}
