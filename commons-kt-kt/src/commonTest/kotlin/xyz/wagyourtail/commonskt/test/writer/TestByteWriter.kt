package xyz.wagyourtail.commonskt.test.writer

import xyz.wagyourtail.commonskt.reader.ByteArrayByteReader
import xyz.wagyourtail.commonskt.writer.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals


class TestByteWriter {

    @Test
    fun testNumericPuts() {
        val writer = ByteArrayByteWriter()
        writer.putU1(0x12)
        writer.putU2(0x1234)
        writer.putU4(0x12345678u)
        writer.putU8(0x1122334455667788uL)
        assertContentEquals(
            byteArrayOf(
                0x12,
                0x12, 0x34,
                0x12, 0x34, 0x56, 0x78,
                0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88.toByte()
            ),
            writer.toByteArray()
        )
    }

    @Test
    fun testNumericPutsLE() {
        val writer = ByteArrayByteWriter()
        writer.putU2LE(0x1234)
        writer.putU4LE(0x12345678u)
        assertContentEquals(
            byteArrayOf(0x34, 0x12, 0x78, 0x56, 0x34, 0x12),
            writer.toByteArray()
        )
    }

    @Test
    fun testRoundTrip() {
        val writer = ByteArrayByteWriter()
        writer.putI4(-0x1234)
        writer.putI8(-0x1122334455L)
        writer.putFloat(1.5f)
        writer.putDouble(-2.25)
        writer.putModifiedUtf8("java/lang/Objecté")

        val reader = ByteArrayByteReader(writer.toByteArray())
        assertEquals(-0x1234, reader.takeI4())
        assertEquals(-0x1122334455L, reader.takeI8())
        assertEquals(1.5f, reader.takeFloat())
        assertEquals(-2.25, reader.takeDouble())
        assertEquals("java/lang/Objecté", reader.takeModifiedUtf8(21))
        reader.expectEOS()
    }

    @Test
    fun testModifiedUtf8Nul() {
        val writer = ByteArrayByteWriter()
        writer.putModifiedUtf8("a\u0000b")
        assertContentEquals(
            byteArrayOf(0x61, 0xC0.toByte(), 0x80.toByte(), 0x62),
            writer.toByteArray()
        )
        assertEquals("a\u0000b", ByteArrayByteReader(writer.toByteArray()).takeModifiedUtf8(4))
    }

    @Test
    fun testBackpatching() {
        // write a u4 length placeholder, write content, backpatch the length —
        // the write-side analog of copy(limit) on read
        val writer = ByteArrayByteWriter()
        val lenPos = writer.position
        writer.putI4(0)
        val start = writer.position
        writer.putU1(1)
        writer.putU1(2)
        writer.putU1(3)
        val len = writer.position - start
        writer.overwrite(lenPos, byteArrayOf(0, 0, 0, len.toByte()))
        assertContentEquals(byteArrayOf(0, 0, 0, 3, 1, 2, 3), writer.toByteArray())
    }

    @Test
    fun testPosition() {
        val writer = ByteArrayByteWriter()
        assertEquals(0, writer.position)
        writer.putU1(1)
        assertEquals(1, writer.position)
        writer.putU2(1)
        assertEquals(3, writer.position)
        writer.put(byteArrayOf(1, 2, 3))
        assertEquals(6, writer.position)
    }

}
