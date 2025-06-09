package xyz.wagyourtail.commons.core;

import java.io.ObjectStreamClass;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ReflectionUtils {

    private static final MethodHandles.Lookup IMPL_LOOKUP = UnsafeHelper.getImplLookup();
    private static final MethodHandle HAS_STATIC_INITIALIZER = findStaticOrNull(IMPL_LOOKUP, ObjectStreamClass.class, "hasStaticInitializer", MethodType.methodType(boolean.class, Class.class));

    public static MethodHandle findStaticOrNull(MethodHandles.Lookup lookup, Class<?> clazz, String methodName, MethodType type) {
        try {
            return lookup.findStatic(clazz, methodName, type);
        } catch (Exception ignored) {}
        return null;
    }

    public static MethodHandle findVirtualOrNull(MethodHandles.Lookup lookup, Class<?> clazz, String methodName, MethodType type) {
        try {
            return lookup.findVirtual(clazz, methodName, type);
        } catch (Exception ignored) {}
        return null;
    }

    public static MethodHandle findSpecialOrNull(MethodHandles.Lookup lookup, Class<?> clazz, String methodName, MethodType type, Class<?> caller) {
        try {
            return lookup.findSpecial(clazz, methodName, type, caller);
        } catch (Exception ignored) {}
        return null;
    }

    public static MethodHandle findStaticGetterOrNull(MethodHandles.Lookup lookup, Class<?> clazz, String fieldName, Class<?> type) {
        try {
            return lookup.findStaticGetter(clazz, fieldName, type);
        } catch (Exception ignored) {}
        return null;
    }

    public static MethodHandle findGetterOrNull(MethodHandles.Lookup lookup, Class<?> clazz, String fieldName, Class<?> type) {
        try {
            return lookup.findGetter(clazz, fieldName, type);
        } catch (Exception ignored) {}
        return null;
    }

    public static MethodHandle findStaticSetterOrNull(MethodHandles.Lookup lookup, Class<?> clazz, String fieldName, Class<?> type) {
        try {
            return lookup.findStaticSetter(clazz, fieldName, type);
        } catch (Exception ignored) {}
        return null;
    }

    public static MethodHandle findSetterOrNull(MethodHandles.Lookup lookup, Class<?> clazz, String fieldName, Class<?> type) {
        try {
            return lookup.findSetter(clazz, fieldName, type);
        } catch (Exception ignored) {}
        return null;
    }

    public static MethodHandle findConstructorOrNull(MethodHandles.Lookup lookup, Class<?> clazz, MethodType type) {
        try {
            return lookup.findConstructor(clazz, type);
        } catch (Exception ignored) {}
        return null;
    }

    public static boolean hasStaticInitializer(Class<?> clazz) {
        try {
            if (HAS_STATIC_INITIALIZER == null) {
                throw new UnsupportedOperationException("Unable to get HAS_STATIC_INITIALIZER");
            }
            return (boolean) HAS_STATIC_INITIALIZER.invokeExact(clazz);
        } catch (Throwable ignored) {}
        return false;
    }



}
