package xyz.wagyourtail.commons.core;

public class CollectionUtils {

    public static <T> int count(Iterable<T> iterable, T value) {
        int count = 0;
        for (T t : iterable) {
            if (t.equals(value)) {
                count++;
            }
        }
        return count;
    }

}
