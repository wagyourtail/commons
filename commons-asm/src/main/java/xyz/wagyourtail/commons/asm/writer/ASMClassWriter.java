package xyz.wagyourtail.commons.asm.writer;

import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import xyz.wagyourtail.commons.asm.info.ClassInfo;
import xyz.wagyourtail.commons.core.function.IOFunction;

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
        if (t2.isInterface()) {
            ClassInfo temp = t1;
            t1 = t2;
            t2 = temp;
        }

        if (t1.isInterface()) {
            if (t2.isInterface()) {
                if (getInterfaces(t1).contains(t2.getName())) {
                    return t2.getName();
                }
                if (getInterfaces(t2).contains(t1.getName())) {
                    return t1.getName();
                }
            }
            if (collectInterfaces(t2).contains(t1.getName())) {
                return t1.getName();
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
            return l1.get(0).getName();
        }
    }

    @Nullable
    @SneakyThrows
    public ClassInfo getClassInfo(String name) {
        ClassInfo ci = infoRetriever.apply(name);
        if (ci == null) {
            throw new IllegalArgumentException("Class " + name + " not found");
        }
        return ci;
    }

    public List<ClassInfo> getSuperTypes(ClassInfo type) {
        List<ClassInfo> l = new ArrayList<>();
        ClassInfo current = type;
        while (current != null && !current.getName().equals("java/lang/Object")) {
            l.add(current);
            if (current.getSuperName().equals("java/lang/Object")) {
                break;
            }
            current = getClassInfo(current.getSuperName());
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
        for (String i : type.getInterfaces()) {
            l.add(i);
            ClassInfo ci = getClassInfo(i);
            if (ci != null && !ci.getName().equals("java/lang/Object")) {
                l.addAll(getInterfaces(ci));
            }
        }
        return l;
    }

}
