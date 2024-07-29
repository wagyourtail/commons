package xyz.wagyourtail.commons.collection;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class SynchronizedWeakHashSet<E> extends AbstractSet<E> implements Serializable {

    private final Map<E, Boolean> map;

    public SynchronizedWeakHashSet() {
        map = new WeakHashMap<>();
    }

    @Override
    public synchronized int size() {
        return map.size();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public synchronized boolean add(E o) {
        return map.put(o, Boolean.TRUE) == null;
    }

    @Override
    public synchronized boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public synchronized void clear() {
        map.clear();
    }

    @Override
    public synchronized Iterator<E> iterator() {
        return Collections.unmodifiableSet(map.keySet()).iterator();
    }

}
