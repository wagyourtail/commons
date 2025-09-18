package xyz.wagyourtail.commons.java8.utils;

import xyz.wagyourtail.commons.core.IteratorUtils;

import java.util.Iterator;
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
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                E next = iterator.next();
                while (!predicate.test(next) && iterator.hasNext()) {
                    next = iterator.next();
                }
                return next;
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

}
