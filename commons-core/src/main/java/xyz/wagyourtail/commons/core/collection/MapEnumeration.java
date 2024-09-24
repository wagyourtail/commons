package xyz.wagyourtail.commons.core.collection;

import java.util.Enumeration;

public abstract class MapEnumeration<T, E> implements Enumeration<E> {
    private final Enumeration<T> enumeration;

    public MapEnumeration(Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public boolean hasMoreElements() {
        return enumeration.hasMoreElements();
    }

    protected abstract E mapper(T element);

    @Override
    public E nextElement() {
        if (!hasMoreElements()) {
            return null;
        }
        return mapper(enumeration.nextElement());
    }

}
