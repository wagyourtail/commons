package xyz.wagyourtail.commons.reader.test;

import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.core.reader.ByteArrayByteReader;
import xyz.wagyourtail.commons.core.writer.ByteArrayByteWriter;

import static org.junit.jupiter.api.Assertions.*;

class TestByteWriter {

    @Test
    public void testNumericPuts() {
        var writer = new ByteArrayByteWriter();
        writer.putU1(0x12);
        writer.putU2(0x1234);
        writer.putU4(0x12345678L);
        writer.putU8(0x1122334455667788L);
        assertArrayEquals(
                new byte[]{
                        0x12,
                        0x12, 0x34,
                        0x12, 0x34, 0x56, 0x78,
                        0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88
                },
                writer.toByteArray()
        );
    }

    @Test
    public void testNumericPutsLE() {
        var writer = new ByteArrayByteWriter();
        writer.putU2LE(0x1234);
        writer.putU4LE(0x12345678L);
        assertArrayEquals(
                new byte[]{0x34, 0x12, 0x78, 0x56, 0x34, 0x12},
                writer.toByteArray()
        );
    }

    @Test
    public void testRoundTrip() {
        var writer = new ByteArrayByteWriter();
        writer.putI4(-0x1234);
        writer.putI8(-0x1122334455L);
        writer.putFloat(1.5f);
        writer.putDouble(-2.25);
        writer.putModifiedUtf8("java/lang/Objecté");

        var reader = new ByteArrayByteReader(writer.toByteArray());
        assertEquals(-0x1234, reader.takeI4());
        assertEquals(-0x1122334455L, reader.takeI8());
        assertEquals(1.5f, reader.takeFloat());
        assertEquals(-2.25, reader.takeDouble());
        assertEquals("java/lang/Objecté", reader.takeModifiedUtf8(21));
        reader.expectEOS();
    }

    @Test
    public void testModifiedUtf8Nul() {
        var writer = new ByteArrayByteWriter();
        writer.putModifiedUtf8("a\u0000b");
        assertArrayEquals(
                new byte[]{0x61, (byte) 0xC0, (byte) 0x80, 0x62},
                writer.toByteArray()
        );
        assertEquals("a\u0000b", new ByteArrayByteReader(writer.toByteArray()).takeModifiedUtf8(4));
    }

    @Test
    public void testBackpatching() {
        // write a u4 length placeholder, write content, backpatch the length —
        // the write-side analog of copy(limit) on read
        var writer = new ByteArrayByteWriter();
        int lenPos = writer.getPosition();
        writer.putI4(0);
        int start = writer.getPosition();
        writer.putU1(1);
        writer.putU1(2);
        writer.putU1(3);
        int len = writer.getPosition() - start;
        writer.overwrite(lenPos, new byte[]{0, 0, 0, (byte) len});
        assertArrayEquals(new byte[]{0, 0, 0, 3, 1, 2, 3}, writer.toByteArray());
    }

}
