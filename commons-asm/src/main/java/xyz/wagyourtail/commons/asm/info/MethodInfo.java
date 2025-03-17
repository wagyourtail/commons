package xyz.wagyourtail.commons.asm.info;


import lombok.AllArgsConstructor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@AllArgsConstructor
public class MethodInfo {
    public final int access;
    public final String name;
    public final Type desc;

    public static MethodInfo of(Method method) {
        return new MethodInfo(
            method.getModifiers(),
            method.getName(),
            Type.getType(method)
        );
    }

    public static MethodInfo of(Constructor<?> constructor) {
        return new MethodInfo(
            constructor.getModifiers(),
            "<init>",
            Type.getType(constructor)
        );
    }

    public static MethodInfo of(MethodNode methodNode) {
        return new MethodInfo(
            methodNode.access,
            methodNode.name,
            Type.getMethodType(methodNode.desc)
        );
    }

    public boolean isInheritable() {
        return (access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) == 0 && !name.startsWith("<");
    }

}
