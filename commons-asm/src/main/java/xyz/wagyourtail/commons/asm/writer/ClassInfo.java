package xyz.wagyourtail.commons.asm.writer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import xyz.wagyourtail.commons.core.function.IOFunction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassInfo {
    final boolean isInterface;
    final String name;
    final String superName;
    final List<String> interfaces;

    public ClassInfo(boolean isInterface, String name, String superName, List<String> interfaces) {
        this.isInterface = isInterface;
        this.name = name;
        this.superName = superName;
        this.interfaces = interfaces;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClassInfo && ((ClassInfo) obj).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static ClassInfo fromClass(Class<?> clazz) {
        Class<?> superClass = clazz.getSuperclass();
        // should only occur for java/lang/Object and it's handled specially
        if (superClass == null) {
            superClass = clazz;
        }
        List<String> interfaces = new ArrayList<>();
        for (Class<?> i : clazz.getInterfaces()) {
            interfaces.add(Type.getInternalName(i));
        }
        return new ClassInfo(
            clazz.isInterface(),
            Type.getInternalName(clazz),
            Type.getInternalName(superClass),
            interfaces
        );
    }

    public static ClassInfo fromClassNode(ClassNode cn) {
        return new ClassInfo(
            (cn.access & Opcodes.ACC_INTERFACE) != 0,
            cn.name,
            cn.superName,
            cn.interfaces
        );
    }

    public static ClassInfo fromReader(ClassReader reader) {
        return new ClassInfo(
            (reader.getAccess() & Opcodes.ACC_INTERFACE) != 0,
            reader.getClassName(),
            reader.getSuperName(),
            Arrays.asList(reader.getInterfaces())
        );
    }

    public static IOFunction<String, ClassInfo> infoWithFallback(final IOFunction<String, ClassInfo> infoRetriever, final IOFunction<String, ClassInfo> fallback) {
        return new IOFunction<String, ClassInfo>() {
            @Override
            public ClassInfo apply(String name) throws IOException {
                ClassInfo info = null;
                IOException ioe = null;
                try {
                    info = infoRetriever.apply(name);
                } catch (IOException e) {
                    ioe = e;
                }
                if (info == null) {
                    try {
                        return fallback.apply(name);
                    } catch (IOException e) {
                        if (ioe != null) e.addSuppressed(ioe);
                        throw e;
                    }
                }
                return info;
            }
        };
    }

    public static IOFunction<String, ClassInfo> infoFromClassloader(final ClassLoader loader) {
        return new IOFunction<String, ClassInfo>() {
            @Override
            public ClassInfo apply(String name) {
                try {
                    Type t = Type.getObjectType(name);
                    Class<?> c = Class.forName(t.getClassName(), false, loader);
                    return ClassInfo.fromClass(c);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
        };
    }

    public static IOFunction<String, ClassInfo> infoFromClassloaderWithoutLoading(final ClassLoader loader) {
        return new IOFunction<String, ClassInfo>() {
            @Override
            public ClassInfo apply(String name) throws IOException {
                try (InputStream is = loader.getResourceAsStream(name + ".class")) {
                    if (is == null) return null;
                    ClassReader cr = new ClassReader(is);
                    return ClassInfo.fromReader(cr);
                }
            }
        };
    }
}
