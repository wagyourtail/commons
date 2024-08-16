package xyz.wagyourtail.commons.asm.writer;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.function.IOFunction;

import java.io.IOException;
import java.util.ArrayList;
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

}