package xyz.wagyourtail.commons.core;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class UnsafeHelper {

    public static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new UnsupportedOperationException("Unable to get Unsafe instance", e);
        }
    }

    public static MethodHandles.Lookup getImplLookup() {
        try {
            // ensure lookup is initialized
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            // get the impl_lookup field
            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Unsafe unsafe = getUnsafe();
            MethodHandles.Lookup IMPL_LOOKUP;
            IMPL_LOOKUP = (MethodHandles.Lookup) unsafe.getObject(MethodHandles.Lookup.class, unsafe.staticFieldOffset(implLookupField));
            if (IMPL_LOOKUP != null) return IMPL_LOOKUP;
            throw new NullPointerException();
        } catch (Throwable e) {
            try {
                // try to create a new lookup
                Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);
                return constructor.newInstance(Object.class, -1);
            } catch (Throwable e2) {
                e.addSuppressed(e2);
            }
            throw new UnsupportedOperationException("Unable to get MethodHandles.Lookup.IMPL_LOOKUP", e);
        }
    }

}
