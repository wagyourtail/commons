package xyz.wagyourtail.commons.asm;

import lombok.val;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import xyz.wagyourtail.commons.asm.info.ClassInfo;
import xyz.wagyourtail.commons.asm.writer.ASMClassWriter;
import xyz.wagyourtail.commons.core.SeekableByteChannelUtils;
import xyz.wagyourtail.commons.core.function.IOFunction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;

public class ASMUtils {

    public static final int ASM_LATEST = Opcodes.ASM9;

    private ASMUtils() {
    }

    /**
     * bit-bashed is class, for speed
     */
    public static boolean isClass(byte[] bytes) {
        return bytes.length > 4 &&
                bytes[0] == (byte) 0xCA &&
                bytes[1] == (byte) 0xFE &&
                bytes[2] == (byte) 0xBA &&
                bytes[3] == (byte) 0xBE;
    }

    /**
     * bit-bashed class version, for speed
     */
    public static int classVersion(byte[] bytes) {
        if (bytes.length < 8) throw new IllegalArgumentException("Invalid bytes");
        return ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);
    }

    public static boolean isClass(InputStream stream) throws IOException {
        byte[] bytes = new byte[4];
        return stream.read(bytes) == 4 &&
                bytes[0] == (byte) 0xCA &&
                bytes[1] == (byte) 0xFE &&
                bytes[2] == (byte) 0xBA &&
                bytes[3] == (byte) 0xBE;
    }

    public static int classVersion(InputStream stream) throws IOException {
        byte[] bytes = new byte[8];
        if (stream.read(bytes) == 8) {
            return classVersion(bytes);
        }
        throw new IOException("Invalid bytes");
    }

    public static boolean isClass(SeekableByteChannel channel) throws IOException {
        channel.position(0);
        val bytes = SeekableByteChannelUtils.readBytes(channel, 4);
        return bytes.length == 4 &&
                bytes[0] == (byte) 0xCA &&
                bytes[1] == (byte) 0xFE &&
                bytes[2] == (byte) 0xBA &&
                bytes[3] == (byte) 0xBE;

    }

    public static int classVersion(SeekableByteChannel channel) throws IOException {
        channel.position(6);
        val bytes = SeekableByteChannelUtils.readBytes(channel, 2);
        return ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
    }

    /**
     * helper function to directly get to asm-tree form
     */
    public static ClassNode bytesToClassNode(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    /**
     * helper function to directly get to asm-tree form
     */
    public static ClassNode bytesToClassNode(byte[] bytes, int flags) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, flags);
        return node;
    }

    /**
     * helper function to write ClassNode, with argument for frame context
     */
    public static byte[] classNodeToBytes(ClassNode node, int flags, IOFunction<String, ClassInfo> infoRetriever) {
        ASMClassWriter writer = new ASMClassWriter(flags, infoRetriever);
        node.accept(writer);
        return writer.toByteArray();
    }

    /**
     * helper function to get enum value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> T enumValueOf(Type type, String value) throws ClassNotFoundException {
        return (T) Enum.valueOf((Class<Enum>) Class.forName(type.getClassName(), true, ASMUtils.class.getClassLoader()), value);
    }

    /**
     * helper function to get enum value
     */
    public static <T extends Enum<T>> T enumValueOf(String desc, String value) throws ClassNotFoundException {
        return enumValueOf(Type.getType(desc), value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> T enumValueOf(Type desc, String value, ClassLoader loader) throws ClassNotFoundException {
        return (T) Enum.valueOf((Class<Enum>) Class.forName(desc.getClassName(), true, loader), value);
    }

    /**
     * helper function to get enum value
     */
    public static <T extends Enum<T>> T enumValueOf(String desc, String value, ClassLoader loader) throws ClassNotFoundException {
        return enumValueOf(Type.getType(desc), value, loader);
    }

    /**
     * helper function to get class form type
     */
    public static Class<?> getClass(Type type) throws ClassNotFoundException {
        return getClass(type, ASMUtils.class.getClassLoader());
    }

    /**
     * helper function to get class form type
     */
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

    public static boolean equals(Type type, Class<?> clazz) {
        return type.getClassName().equals(clazz.getName());
    }

    public static ClassNode copy(ClassNode node) {
        ClassNode copy = new ClassNode();
        node.accept(new ClassVisitor(Opcodes.ASM9, copy) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

                    @Override
                    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments.clone());
                    }
                };
            }
        });
        return copy;
    }

}
