package xyz.wagyourtail.commons.core.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClassTree<K, V> {
    final List<ClassTree<? super K, ? super V>> subTypes = new ArrayList<>();
    final Class<K> type;
    V value;

    public ClassTree(Class<K> baseType, V value) {
        type = baseType;
        this.value = value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends K, U extends V> void registerType(Class<T> type, U value) {
        if (type.equals(this.type)) {
            this.value = value;
            return;
        }
        for (ClassTree<? super K, ? super V> wrap : subTypes) {
            if (wrap.type.equals(type)) {
                wrap.value = value;
                return;
            }
            if (wrap.type.isAssignableFrom(type)) {
                wrap.registerType(type, value);
                return;
            }
        }
        ClassTree<T, U> newWrapper = new ClassTree<>(type, value);
        Iterator<ClassTree<? super K, ? super V>> iter = subTypes.iterator();
        while (iter.hasNext()) {
            ClassTree<? super K, ? super V> wrap = iter.next();
            if (type.isAssignableFrom(wrap.type)) {
                newWrapper.subTypes.add(wrap);
                iter.remove();
            }
        }
        this.subTypes.add((ClassTree) newWrapper);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends K> V get(T inputType) {
        Class<T> inputClass = (Class) inputType.getClass();
        return getSubtype(inputClass).value;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends K, U extends V> ClassTree<T, U> getSubtype(Class<T> type) {
        for (ClassTree<? super K, ? super V> wrap : subTypes) {
            if (wrap.type.isAssignableFrom(type)) {
                return wrap.getSubtype(type);
            }
        }
        return (ClassTree<T, U>) this;
    }

}
