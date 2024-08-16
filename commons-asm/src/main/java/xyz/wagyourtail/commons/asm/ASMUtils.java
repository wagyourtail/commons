package xyz.wagyourtail.commons.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import xyz.wagyourtail.commons.asm.writer.ASMClassWriter;
import xyz.wagyourtail.commons.asm.writer.ClassInfo;
import xyz.wagyourtail.commons.core.function.IOFunction;

public class ASMUtils {

    public static ClassNode bytesToClassNode(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    public static ClassNode bytesToClassNode(byte[] bytes, int flags) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, flags);
        return node;
    }

    public static byte[] classNodeToBytes(ClassNode node, int flags, IOFunction<String, ClassInfo> infoRetriever) {
        ASMClassWriter writer = new ASMClassWriter(flags, infoRetriever);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static Object enumValueOf(String desc, String value) throws ClassNotFoundException {
        return Enum.valueOf((Class<Enum>) Class.forName(Type.getType(desc).getClassName(), true, ASMUtils.class.getClassLoader()), value);
    }

    public static Object enumValueOf(String desc, String value, ClassLoader loader) throws ClassNotFoundException {
        return Enum.valueOf((Class<Enum>) Class.forName(Type.getType(desc).getClassName(), true, loader), value);
    }

    public static Class<?> getClass(Type type) throws ClassNotFoundException {
        return getClass(type, ASMUtils.class.getClassLoader());
    }

    public static Class<?> getClass(Type type, ClassLoader loader) throws ClassNotFoundException {
        switch (type.getSort()) {
            case Type.VOID:
                return void.class;
            case Type.BOOLEAN:
                return boolean.class;
            case Type.CHAR:
                return char.class;
            case Type.BYTE:
                return byte.class;
            case Type.SHORT:
                return short.class;
            case Type.INT:
                return int.class;
            case Type.FLOAT:
                return float.class;
            case Type.LONG:
                return long.class;
            case Type.DOUBLE:
                return double.class;
            default:
                return Class.forName(type.getClassName(), true, loader);
        }

    }

}
