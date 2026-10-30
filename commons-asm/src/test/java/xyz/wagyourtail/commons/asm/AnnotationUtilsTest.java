package xyz.wagyourtail.commons.asm;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class AnnotationUtilsTest {

    @Test
    public void testAnnotationUtils() throws ClassNotFoundException {
        AnnotationNode testAnnotation = new AnnotationNode(Opcodes.ASM9, Type.getDescriptor(TestAnnotation.class));
        testAnnotation.visit("key", "keyValue");

        TestAnnotation ann = AnnotationUtils.createAnnotation(testAnnotation, TestAnnotation.class);
        assertEquals("keyValue", ann.key());
        assertEquals("defaultValue", ann.value());

        AnnotationNode testAnnotation2 = new AnnotationNode(Opcodes.ASM9, Type.getDescriptor(TestAnnotation.class));
        testAnnotation2.visit("value", "valueValue");

        TestAnnotation ann2 = AnnotationUtils.createAnnotation(testAnnotation2);
        assertEquals("valueValue", ann2.value());
        assertEquals("defaultKey", ann2.key());

        AnnotationNode nestedAnnotation = new AnnotationNode(Opcodes.ASM9, Type.getDescriptor(NestedAnnotation.class));
        AnnotationVisitor arr = nestedAnnotation.visitArray("value");
        arr.visit(null, testAnnotation);
        AnnotationVisitor arrVal = arr.visitAnnotation(null, Type.getDescriptor(TestAnnotation.class));
        testAnnotation2.accept(arrVal);
        arr.visitEnd();

        NestedAnnotation nestedAnn = AnnotationUtils.createAnnotation(nestedAnnotation);
        assertEquals(2, nestedAnn.value().length);
        assertEquals("defaultValue", nestedAnn.value()[0].value());
        assertEquals("keyValue", nestedAnn.value()[0].key());
        assertEquals("valueValue", nestedAnn.value()[1].value());
        assertEquals("defaultKey", nestedAnn.value()[1].key());

    }

    public @interface TestAnnotation {

        String value() default "defaultValue";

        String key() default "defaultKey";

    }

    public @interface ArrayAnnotation {

        int[] ints() default {};

        String name() default "n";

    }

    @Test
    public void testObjectMethods() throws ClassNotFoundException {
        AnnotationNode node = new AnnotationNode(Opcodes.ASM9, Type.getDescriptor(ArrayAnnotation.class));
        node.visit("ints", Arrays.asList(1, 2, 3));
        node.visit("name", "x");

        ArrayAnnotation a = AnnotationUtils.createAnnotation(node, ArrayAnnotation.class);

        //annotationType — previously NPE
        assertEquals(ArrayAnnotation.class, a.annotationType());

        //primitive array materializes correctly + cached
        assertArrayEquals(new int[]{1, 2, 3}, a.ints());
        assertSame(a.ints(), a.ints(), "cached, not re-allocated");

        //equals/hashCode/toString — previously NPE
        assertTrue(a.equals(a));
        assertFalse(a.equals("x"));
        assertEquals(a.hashCode(), a.hashCode());
        assertTrue(a.toString().startsWith("@" + ArrayAnnotation.class.getName()));
    }

    public @interface NestedAnnotation {

        TestAnnotation[] value();

    }

}
