package xyz.wagyourtail.commons.asm.info;

import lombok.Getter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import xyz.wagyourtail.commons.core.ReflectionUtils;
import xyz.wagyourtail.commons.core.function.IOFunction;
import xyz.wagyourtail.commons.core.lazy.Lazy;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassInfo {
    @Getter
    private final String name;
    @Getter
    private final String superName;
    @Getter
    private final boolean isInterface;
    @Getter
    private final List<String> interfaces;
    private final Lazy<List<MethodInfo>> methods;
    private final Lazy<List<FieldInfo>> fields;

    public ClassInfo(boolean isInterface, String name, String superName, List<String> interfaces, Lazy<List<MethodInfo>> methods, Lazy<List<FieldInfo>> fields) {
        this.isInterface = isInterface;
        this.name = name;
        this.superName = superName;
        this.interfaces = new ArrayList<>(interfaces);
        this.methods = methods;
        this.fields = fields;
    }

    public static ClassInfo of(final Class<?> clazz) {
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
                interfaces,
                new Lazy<List<MethodInfo>>() {
                    @Override
                    protected List<MethodInfo> supplier() {
                        List<MethodInfo> methods = new ArrayList<>();

                        for (Method m : clazz.getDeclaredMethods()) {
                            methods.add(MethodInfo.of(m));
                        }
                        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                            methods.add(MethodInfo.of(c));
                        }

                        if (ReflectionUtils.hasStaticInitializer(clazz)) {
                            methods.add(MethodInfo.createClinit());
                        }

                        return methods;
                    }
                },
                new Lazy<List<FieldInfo>>() {
                    @Override
                    protected List<FieldInfo> supplier() {
                        List<FieldInfo> fields = new ArrayList<>();
                        for (Field f : clazz.getDeclaredFields()) {
                            fields.add(FieldInfo.of(f));
                        }
                        return fields;
                    }
                }
        );
    }

    public static ClassInfo of(final ClassNode cn) {
        return new ClassInfo(
                (cn.access & Opcodes.ACC_INTERFACE) != 0,
                cn.name,
                cn.superName,
                cn.interfaces,
                new Lazy<List<MethodInfo>>() {
                    @Override
                    protected List<MethodInfo> supplier() {
                        List<MethodInfo> methods = new ArrayList<>();
                        for (MethodNode mn : cn.methods) {
                            methods.add(MethodInfo.of(mn));
                        }
                        return methods;
                    }
                },
                new Lazy<List<FieldInfo>>() {
                    @Override
                    protected List<FieldInfo> supplier() {
                        List<FieldInfo> fields = new ArrayList<>();
                        for (FieldNode fd : cn.fields) {
                            fields.add(FieldInfo.of(fd));
                        }
                        return fields;
                    }
                }
        );
    }

    public static ClassInfo of(final ClassReader reader) {
        final Lazy<ClassNode> cn = new Lazy<ClassNode>() {
            @Override
            protected ClassNode supplier() {
                ClassNode cn = new ClassNode();
                reader.accept(cn, ClassReader.SKIP_CODE);
                return cn;
            }
        };
        return new ClassInfo(
                (reader.getAccess() & Opcodes.ACC_INTERFACE) != 0,
                reader.getClassName(),
                reader.getSuperName(),
                Arrays.asList(reader.getInterfaces()),
                new Lazy<List<MethodInfo>>() {
                    @Override
                    protected List<MethodInfo> supplier() {
                        List<MethodInfo> methods = new ArrayList<>();
                        for (MethodNode mn : cn.get().methods) {
                            methods.add(MethodInfo.of(mn));
                        }
                        return methods;
                    }
                },
                new Lazy<List<FieldInfo>>() {
                    @Override
                    protected List<FieldInfo> supplier() {
                        List<FieldInfo> fields = new ArrayList<>();
                        for (FieldNode fd : cn.get().fields) {
                            fields.add(FieldInfo.of(fd));
                        }
                        return fields;
                    }
                }
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

    @SafeVarargs
    public static IOFunction<String, ClassInfo> infoWithFallbacks(final IOFunction<String, ClassInfo>... infoRetrievers) {
        return new IOFunction<String, ClassInfo>() {
            @Override
            public ClassInfo apply(String name) throws IOException {
                IOException ioe = null;
                for (IOFunction<String, ClassInfo> infoRetriever : infoRetrievers) {
                    try {
                        ClassInfo info = infoRetriever.apply(name);
                        if (info != null) {
                            return info;
                        }
                    } catch (IOException e) {
                        if (ioe != null) e.addSuppressed(ioe);
                        ioe = e;
                    }
                }
                if (ioe != null) throw ioe;
                return null;
            }
        };
    }

    public static IOFunction<String, ClassInfo> infoFromByteSupplier(final IOFunction<String, byte[]> infoRetriever) {
        return new IOFunction<String, ClassInfo>() {
            @Override
            public ClassInfo apply(String s) throws IOException {
                byte[] bytes = infoRetriever.apply(s);
                if (bytes != null) {
                    ClassReader reader = new ClassReader(bytes);
                    return of(reader);
                }
                return null;
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
                    return of(c);
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
                    return of(cr);
                }
            }
        };
    }

    public static IOFunction<String, ClassInfo> infoFromCurrent() {
        return infoFromClassloader(ClassInfo.class.getClassLoader());
    }

    public List<MethodInfo> getMethods() {
        return methods.get();
    }

    public List<FieldInfo> getFields() {
        return fields.get();
    }

}
