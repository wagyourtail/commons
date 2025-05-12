package xyz.wagyourtail.commons.asm;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public @interface NestedAnnotation {

        TestAnnotation[] value();

    }

}
