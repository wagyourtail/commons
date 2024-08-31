package xyz.wagyourtail.commons.core.collection;

import java.util.Enumeration;

public abstract class FlatEnumeration<T, E> implements Enumeration<E> {
    private final Enumeration<T> enumeration;

    private Enumeration<E> currentEnumeration = null;

    public FlatEnumeration(Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public boolean hasMoreElements() {
        while (currentEnumeration == null || !currentEnumeration.hasMoreElements()) {
            if (enumeration.hasMoreElements()) {
                currentEnumeration = mapper(enumeration.nextElement());
            } else {
                return false;
            }
        }
        return true;
    }

    protected abstract Enumeration<E> mapper(T element);

    @Override
    public E nextElement() {
        if (!hasMoreElements()) {
            return null;
        }
        return currentEnumeration.nextElement();
    }

    public interface Mapper<T, E> {
        Enumeration<E> map(T t);
    }

}
