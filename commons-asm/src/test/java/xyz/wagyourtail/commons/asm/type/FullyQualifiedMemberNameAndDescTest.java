package xyz.wagyourtail.commons.asm.type;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import static org.junit.jupiter.api.Assertions.*;

class FullyQualifiedMemberNameAndDescTest {

    @Test
    void of() {
        assertEquals(new FullyQualifiedMemberNameAndDesc(Type.getObjectType("com/example/Test"), null, null), FullyQualifiedMemberNameAndDesc.of("Lcom/example/Test;"));
        assertEquals(new FullyQualifiedMemberNameAndDesc(Type.getObjectType("com/example/Test"), "test", Type.getType("(Ljava/lang/String;)V")), FullyQualifiedMemberNameAndDesc.of("Lcom/example/Test;test;(Ljava/lang/String;)V"));
    }

}