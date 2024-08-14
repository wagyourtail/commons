package xyz.wagyourtail.commons.java7.collection;

import xyz.wagyourtail.commons.java7.function.Function;

import java.util.Enumeration;

/**
 * basically {@code enumeration.stream().flatMap(mapper).toEnumeration()} except that doesn't exist.
 * @param <T>
 * @param <E>
 */
public class FlatEnumeration<T, E> implements Enumeration<E> {
    private final Enumeration<T> enumeration;
    private final Function<T, Enumeration<E>> mapper;

    private Enumeration<E> currentEnumeration = null;

    public FlatEnumeration(Enumeration<T> enumeration, Function<T, Enumeration<E>> mapper) {
        this.enumeration = enumeration;
        this.mapper = mapper;
    }

    @Override
    public boolean hasMoreElements() {
        while (currentEnumeration == null || !currentEnumeration.hasMoreElements()) {
            if (enumeration.hasMoreElements()) {
                currentEnumeration = mapper.apply(enumeration.nextElement());
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public E nextElement() {
        if (!hasMoreElements()) {
            return null;
        }
        return currentEnumeration.nextElement();
    }

}
