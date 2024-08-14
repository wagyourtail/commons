package xyz.wagyourtail.commons.asm;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.function.IOFunction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ASMClassWriter extends ClassWriter {
    private final IOFunction<String, ClassInfo> infoRetriever;

    public ASMClassWriter(int flags, IOFunction<String, ClassInfo> infoRetriever) {
        super(flags);
        this.infoRetriever = infoRetriever;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (type1.equals(type2)) {
            return type1;
        }

        ClassInfo t1 = getClassInfo(type1);
        ClassInfo t2 = getClassInfo(type2);

        if (t1 == null || t2 == null) {
            return "java/lang/Object";
        }
        if (t2.isInterface) {
            ClassInfo temp = t1;
            t1 = t2;
            t2 = temp;
        }

        if (t1.isInterface) {
            if (t2.isInterface) {
                if (getInterfaces(t1).contains(t2.name)) {
                    return t2.name;
                }
                if (getInterfaces(t2).contains(t1.name)) {
                    return t1.name;
                }
            }
            if (collectInterfaces(t2).contains(t1.name)) {
                return t1.name;
            }
            return "java/lang/Object";
        } else {
            List<ClassInfo> l1 = getSuperTypes(getClassInfo(type1));
            List<ClassInfo> l2 = getSuperTypes(getClassInfo(type2));
            // get intersection
            l1.retainAll(l2);
            // get first element
            if (l1.isEmpty()) {
                return "java/lang/Object";
            }
            return l1.get(0).name;
        }
    }

    @Nullable
    public ClassInfo getClassInfo(String name) {
        try {
            ClassInfo ci = infoRetriever.apply(name);
            if (ci == null) {
                throw new IllegalArgumentException("Class " + name + " not found");
            }
            return ci;
        } catch (IOException e) {
            Utils.<RuntimeException>sneakyThrow(e);
            return null;
        }
    }

    public List<ClassInfo> getSuperTypes(ClassInfo type) {
        List<ClassInfo> l = new ArrayList<>();
        ClassInfo current = type;
        while (current != null && !current.name.equals("java/lang/Object")) {
            l.add(current);
            if (current.superName.equals("java/lang/Object")) {
                break;
            }
            current = getClassInfo(current.superName);
        }
        return l;
    }

    public Set<String> collectInterfaces(ClassInfo type) {
        List<ClassInfo> superTypes = getSuperTypes(type);
        Set<String> interfaces = new HashSet<>();
        for (ClassInfo info : superTypes) {
            interfaces.addAll(getInterfaces(info));
        }
        return interfaces;
    }

    public Set<String> getInterfaces(ClassInfo type) {
        Set<String> l = new HashSet<>();
        for (String i : type.interfaces) {
            l.add(i);
            ClassInfo ci = getClassInfo(i);
            if (ci != null && !ci.name.equals("java/lang/Object")) {
                l.addAll(getInterfaces(ci));
            }
        }
        return l;
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

    public static class ClassInfo {
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
    }
}
