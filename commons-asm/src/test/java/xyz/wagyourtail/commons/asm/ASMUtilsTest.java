package xyz.wagyourtail.commons.asm;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import xyz.wagyourtail.commons.core.io.SeekableInMemoryByteChannel;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ASMUtilsTest {

    byte[] currentClass = ASMUtilsTest.class.getClassLoader().getResourceAsStream("xyz/wagyourtail/commons/asm/ASMUtilsTest.class").readAllBytes();

    ASMUtilsTest() throws IOException {
    }

    @Test
    void isClass() throws IOException {
        assertTrue(ASMUtils.isClass(currentClass));
        assertTrue(ASMUtils.isClass(new ByteArrayInputStream(currentClass)));
        assertTrue(ASMUtils.isClass(new SeekableInMemoryByteChannel(currentClass)));
    }

    @Test
    void classVersion() {
        assertEquals(ASMUtils.classVersion(currentClass), new ClassReader(currentClass).readShort(6));
    }

    @Test
    void enumValueOf() throws ClassNotFoundException {
        assertEquals(TestEnum.A, ASMUtils.enumValueOf("Lxyz/wagyourtail/commons/asm/ASMUtilsTest$TestEnum;", "A"));
    }

    @Test
    void testGetClass() throws ClassNotFoundException {
        assertEquals(ASMUtilsTest.class, ASMUtils.getClass(Type.getType(ASMUtilsTest.class), ASMUtilsTest.class.getClassLoader()));
    }

    @Test
    void testEquals() {
        assertTrue(ASMUtils.equals(Type.getType(ASMUtilsTest.class), ASMUtilsTest.class));
    }

    @Test
    void copy() {
        ClassNode classNode = ASMUtils.bytesToClassNode(currentClass);
        ClassNode copy = ASMUtils.copy(classNode);
        byte[] first = ASMUtils.classNodeToBytes(classNode, 0, null);
        byte[] second = ASMUtils.classNodeToBytes(copy, 0, null);
        assertArrayEquals(first, second);
    }

    enum TestEnum {
        A, B, C
    }

}