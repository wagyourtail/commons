package xyz.wagyourtail.commons.core;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionUtils {

    private CollectionUtils() {
    }

    public static <T> int count(Iterable<T> iterable, T value) {
        int count = 0;
        for (T t : iterable) {
            if (t.equals(value)) {
                count++;
            }
        }
        return count;
    }

    public static List<String> resolveArgs(Map<String, String> properties, List<String> args, boolean hasDollar) {
        List<String> newArgs = new ArrayList<>();
        Pattern pattern = Pattern.compile((hasDollar ? "\\$" : "") + "\\{([^}]+)}");

        for (String arg : args) {
            Matcher m = pattern.matcher(arg);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String key = m.group(1);
                if (!properties.containsKey(key)) throw new IllegalArgumentException("Property " + key + " not found");
                String value = properties.get(key);
                m.appendReplacement(sb, value);
            }
            m.appendTail(sb);

            newArgs.add(sb.toString());
        }

        return newArgs;
    }

    @SafeVarargs
    public static <T> Iterable<T> concat(final Iterable<? extends T>... iterables) {
        return new Iterable<T>() {

            @NotNull
            @Override
            public Iterator<T> iterator() {
                val iterators = new Iterator[iterables.length];
                for (int i = 0; i < iterables.length; i++) {
                    iterators[i] = iterables[i].iterator();
                }
                return concat(iterators);
            }

        };
    }

    @SafeVarargs
    public static <T> Iterator<T> concat(final Iterator<? extends T>... iterators) {
        return new Iterator<T>() {
            final ArrayDeque<Iterator<? extends T>> queue = new ArrayDeque<>(Arrays.asList(iterators));
            Iterator<? extends T> current = queue.removeFirst();
            Iterator<? extends T> previous = null;

            @Override
            public boolean hasNext() {
                if (current.hasNext()) return true;
                if (queue.isEmpty()) return false;
                current = queue.removeFirst();
                return hasNext();
            }

            @Override
            public T next() {
                previous = current;
                return current.next();
            }

            @Override
            public void remove() {
                previous.remove();
            }

        };
    }

}
