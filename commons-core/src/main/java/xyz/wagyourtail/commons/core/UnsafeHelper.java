package xyz.wagyourtail.commons.core;

import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class UnsafeHelper {

    private UnsafeHelper() {
    }

    private static Unsafe cachedUnsafe;

    public static Unsafe getUnsafe() {
        if (cachedUnsafe != null) {
            return cachedUnsafe;
        }
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return cachedUnsafe = (Unsafe) f.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new UnsupportedOperationException("Unable to get Unsafe instance", e);
        }
    }

    private static MethodHandles.Lookup cachedImplLookup;

    public static MethodHandles.Lookup getImplLookup() {
        if (cachedImplLookup != null) {
            return cachedImplLookup;
        }

        // ensure lookup is initialized
        MethodHandles.lookup();

        // try to get lookup
        MethodHandles.Lookup lookup = getImplLookupWithUnsafe();
        if (lookup == null) {
            lookup = getImplLookupWithSerialization();
        }
        if (lookup == null) {
            lookup = constructImplLookupWithReflection();
        }
        if (lookup == null) {
            lookup = constructImplLookupWithSerialization();
        }
        if (lookup == null) {
            throw new UnsupportedOperationException("Unable to get or construct IMPL_LOOKUP");
        }
        return cachedImplLookup = lookup;
    }

    private static MethodHandles.Lookup getImplLookupWithUnsafe() {
        try {
            // get the impl_lookup field
            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Unsafe unsafe = getUnsafe();
            MethodHandles.Lookup IMPL_LOOKUP;
            IMPL_LOOKUP = (MethodHandles.Lookup) unsafe.getObject(MethodHandles.Lookup.class, unsafe.staticFieldOffset(implLookupField));
            return IMPL_LOOKUP;
        } catch (Throwable ignored) {}
        return null;
    }

    private static MethodHandles.Lookup constructImplLookupWithReflection() {
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            return constructor.newInstance(Object.class, -1);
        } catch (Throwable ignored) {}
        return null;
    }

    private static MethodHandles.Lookup getImplLookupWithSerialization() {
        ReflectionFactory factory = ReflectionFactory.getReflectionFactory();
        try {
            // create constructor for lookup
            Constructor<MethodHandles.Lookup> constructor = (Constructor) factory.newConstructorForSerialization(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class));
            // get private lookup for lookup class
            MethodHandles.Lookup lookup = constructor.newInstance(MethodHandles.Lookup.class);
            // use private lookup to access IMPL_LOOKUP field
            MethodHandle getter = lookup.findStaticGetter(MethodHandles.Lookup.class, "IMPL_LOOKUP", MethodHandles.Lookup.class);
            return (MethodHandles.Lookup) getter.invokeExact();
        } catch (Throwable ignored) {}
        return null;
    }

    private static MethodHandles.Lookup constructImplLookupWithSerialization() {
        ReflectionFactory factory = ReflectionFactory.getReflectionFactory();
        try {
            // create constructor for lookup
            Constructor<MethodHandles.Lookup> constructor = (Constructor) factory.newConstructorForSerialization(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class));
            return constructor.newInstance(Object.class, -1);
        } catch (Throwable ignored) {}
        return null;
    }

}
