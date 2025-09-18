package xyz.wagyourtail.commons.core;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class IteratorUtils {

    @SafeVarargs
    public static <T> Iterable<T> concat(final Iterable<? extends T>... iterables) {
        return new Iterable<T>() {

            @NotNull
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    final Deque<Iterable<? extends T>> queue = new ArrayDeque<>(Arrays.asList(iterables));
                    Iterable<? extends T> current = queue.removeFirst();
                    Iterator<? extends T> previous = null;

                    @Override
                    public boolean hasNext() {
                        if (current.iterator().hasNext()) return true;
                        if (queue.isEmpty()) return false;
                        current = queue.removeFirst();
                        return hasNext();
                    }

                    @Override
                    public T next() {
                        previous = current.iterator();
                        return previous.next();
                    }

                    @Override
                    public void remove() {
                        previous.remove();
                    }
                };
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

    @SafeVarargs
    public static <T> Iterator<T> zip(final Iterator<? extends T>... iterators) {
        return zip(false, iterators);
    }

    @SafeVarargs
    public static <T> Iterator<T> zip(final boolean stopWhenFirstEmpty, final Iterator<? extends T>... iterators) {
        return new Iterator<T>() {
            final ArrayDeque<Iterator<? extends T>> queue = new ArrayDeque<>(Arrays.asList(iterators));
            Iterator<? extends T> current = queue.removeFirst();
            Iterator<? extends T> previous = null;

            @Override
            public boolean hasNext() {
                if (current.hasNext()) return true;
                if (stopWhenFirstEmpty) {
                    return false;
                }
                if (queue.isEmpty()) return false;
                current = queue.removeFirst();
                return hasNext();
            }

            @Override
            public T next() {
                previous = current;
                queue.addLast(current);
                current = queue.removeFirst();
                return previous.next();
            }

            @Override
            public void remove() {
                previous.remove();
            }

        };
    }

    public static <T> Iterator<T> withDelimiter(final Iterator<T> iterator, final T delimiter) {
        return new Iterator<T>() {
            boolean nextDelimiter = false;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                if (nextDelimiter) {
                    nextDelimiter = false;
                    return delimiter;
                }
                nextDelimiter = true;
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> repeat(int count, Iterable<T> iterator) {
        Iterable<T>[] iterables = new Iterable[count];
        Arrays.fill(iterables, iterator);
        return concat(iterables);
    }

    public static <T> List<T> toList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public static <T> Set<T> toSet(Iterator<T> iterator) {
        Set<T> set = new HashSet<>();
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }

    public static <K, V> Map<K, V> toMap(Iterator<Map.Entry<K, V>> iterator) {
        Map<K, V> map = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<K, V> entry = iterator.next();
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

}
