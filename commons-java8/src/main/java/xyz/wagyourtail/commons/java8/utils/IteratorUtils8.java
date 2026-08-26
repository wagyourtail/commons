package xyz.wagyourtail.commons.java8.utils;

import lombok.val;
import xyz.wagyourtail.commons.core.IteratorUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

public class IteratorUtils8 extends IteratorUtils {

    public static <E, R> Iterator<R> map(final Iterator<E> iterator, final Function<E, R> mapper) {
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R next() {
                return mapper.apply(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    public static <E> Iterator<E> filter(Iterator<E> iterator, Predicate<E> predicate) {
        return new Iterator<E>() {
            private E next;

            @Override
            public boolean hasNext() {
                if (next != null) {
                    return true;
                }
                while (iterator.hasNext()) {
                    next = iterator.next();
                    if (predicate.test(next)) {
                        return true;
                    }
                }
                next = null;
                return false;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                val n = next;
                next = null;
                return n;
            }

            @Override
            public void remove() {
                if (next != null) {
                    throw new IllegalStateException("can't remove after hasNext()");

                }
                iterator.remove();
            }
        };
    }

}
