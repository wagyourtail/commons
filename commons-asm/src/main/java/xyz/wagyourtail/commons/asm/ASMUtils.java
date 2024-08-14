package xyz.wagyourtail.commons.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
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

    public static byte[] classNodeToBytes(ClassNode node, int flags, IOFunction<String, ASMClassWriter.ClassInfo> infoRetriever) {
        ASMClassWriter writer = new ASMClassWriter(flags, infoRetriever);
        node.accept(writer);
        return writer.toByteArray();
    }

}
