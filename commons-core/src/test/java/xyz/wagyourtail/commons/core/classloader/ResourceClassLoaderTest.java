package xyz.wagyourtail.commons.core.classloader;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import xyz.wagyourtail.commons.core.classloader.provider.PackedResourceProvider;
import xyz.wagyourtail.commons.core.io.SeekableInMemoryByteChannel;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceClassLoaderTest {

    public PackedResourceProvider generateTestJar() throws Exception {
        val os = new ByteArrayOutputStream();
        val packed = new PackedResourceProvider.PackedResourceOutputStream(os);
        packed.putNextEntry("META-INF/MANIFEST.MF");
        // write with implementation-version and such
        packed.write("Manifest-Version: 1.0\n".getBytes(StandardCharsets.UTF_8));
        packed.write("Implementation-Version: 1.0\n".getBytes(StandardCharsets.UTF_8));
        packed.write("Implementation-Vendor: wagyourtail.xyz\n".getBytes(StandardCharsets.UTF_8));
        packed.write("Specification-Version: 1.1\n".getBytes(StandardCharsets.UTF_8));
        packed.write("Specification-Vendor: wagyourtail.xyz\n".getBytes(StandardCharsets.UTF_8));
        packed.write("Specification-Title: wagyourtail.commons.core.classloader\n".getBytes(StandardCharsets.UTF_8));
        packed.write("Implementation-Title: wagyourtail.commons.core.classloader\n".getBytes(StandardCharsets.UTF_8));
        packed.closeEntry();
        packed.putNextEntry("xyz/wagyourtail/test/PackedTest.class");
        ClassNode cn = new ClassNode();
        cn.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "xyz/wagyourtail/test/PackedTest", null, "java/lang/Object", null);
        var md = cn.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        md.visitCode();
        md.visitVarInsn(Opcodes.ALOAD, 0);
        md.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        md.visitInsn(Opcodes.RETURN);
        md.visitMaxs(1, 1);
        md.visitEnd();
        cn.visitEnd();

        md = cn.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getImplementationVersion", "()Ljava/lang/String;", null, null);
        md.visitCode();
        md.visitLdcInsn(Type.getType("Lxyz/wagyourtail/test/PackedTest;")); // class
        md.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getPackage", "()Ljava/lang/Package;", false);
        md.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Package", "getImplementationVersion", "()Ljava/lang/String;", false);
        md.visitInsn(Opcodes.ARETURN);
        md.visitMaxs(1, 1);
        md.visitEnd();

        md = cn.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getSpecificationVersion", "()Ljava/lang/String;", null, null);
        md.visitCode();
        md.visitLdcInsn(Type.getType("Lxyz/wagyourtail/test/PackedTest;")); // class
        md.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getPackage", "()Ljava/lang/Package;", false);
        md.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Package", "getSpecificationVersion", "()Ljava/lang/String;", false);
        md.visitInsn(Opcodes.ARETURN);
        md.visitMaxs(1, 1);
        md.visitEnd();

        val writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cn.accept(writer);
        packed.write(writer.toByteArray());

        packed.closeEntry();
        packed.close();

        return new PackedResourceProvider(new SeekableInMemoryByteChannel(os.toByteArray()));
    }

    @Test
    public void testResourceClassLoader() throws Exception {
        val provider = generateTestJar();
        val loader = new ResourceClassLoader(List.of(provider), Thread.currentThread().getContextClassLoader());

        val clazz = loader.loadClass("xyz.wagyourtail.test.PackedTest");
        assertEquals("1.0", clazz.getMethod("getImplementationVersion").invoke(null));
        assertEquals("1.1", clazz.getMethod("getSpecificationVersion").invoke(null));
    }

}