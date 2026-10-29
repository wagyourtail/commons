package xyz.wagyourtail.commonskt.test.reader

import xyz.wagyourtail.commonskt.reader.ByteArrayByteReader
import xyz.wagyourtail.commonskt.reader.ByteReader
import xyz.wagyourtail.commonskt.reader.ParseException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue


class TestByteReader {

    @Test
    fun test() {
        val reader = ByteArrayByteReader(byteArrayOf(0x74, 0x65, 0x73, 0x74))
        assertEquals(0x74, reader.peek())
        assertEquals(0x74, reader.take())
        assertEquals(0x65, reader.peek())
        assertEquals(0x65, reader.take())
        assertEquals(0x73, reader.take())
        assertEquals(0x74, reader.take())
        assertEquals(null, reader.peek())
        assertEquals(null, reader.take())
    }

    @Test
    fun testPositionAndSeek() {
        val reader = ByteArrayByteReader(byteArrayOf(0, 1, 2, 3, 4, 5))
        assertEquals(0, reader.position)
        reader.take()
        reader.take()
        assertEquals(2, reader.position)
        reader.seek(5)
        assertEquals(5, reader.take())
        assertEquals(6, reader.position)
        assertNull(reader.take())
        reader.seek(0)
        assertEquals(0, reader.take())
    }

    @Test
    fun testTake() {
        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3, 4, 5))
        assertContentEquals(byteArrayOf(1, 2, 3), reader.take(3))
        assertContentEquals(byteArrayOf(4, 5), reader.take(10))
        assertContentEquals(ByteArray(0), reader.take(3))
    }

    @Test
    fun testNumericTakes() {
        val reader = ByteArrayByteReader(
            byteArrayOf(
                0x12,
                0x12, 0x34,
                0x12, 0x34, 0x56, 0x78,
                0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88.toByte()
            )
        )
        assertEquals(0x12, reader.takeU1())
        assertEquals(0x1234, reader.takeU2())
        assertEquals(0x12345678u, reader.takeU4())
        assertEquals(0x1122334455667788uL, reader.takeU8())
        assertNull(reader.take())
    }

    @Test
    fun testNumericTakesLE() {
        val reader = ByteArrayByteReader(
            byteArrayOf(
                0x34, 0x12,
                0x78, 0x56, 0x34, 0x12
            )
        )
        assertEquals(0x1234, reader.takeU2LE())
        assertEquals(0x12345678u, reader.takeU4LE())
    }

    @Test
    fun testSignedTakes() {
        val reader =
            ByteArrayByteReader(byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()))
        assertEquals(-1, reader.takeI1())
        assertEquals(-1, reader.takeI2())
        assertEquals(0xFF, reader.takeU1())
    }

    @Test
    fun testTakeUntil() {
        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3, 0, 4, 5))
        assertContentEquals(byteArrayOf(1, 2, 3), reader.takeUntil(0))
        assertEquals(0, reader.take())
        assertContentEquals(byteArrayOf(4, 5), reader.takeRemaining())
    }

    @Test
    fun testMarkReset() {
        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3, 4))
        reader.mark()
        assertEquals(1, reader.take())
        reader.reset()
        assertEquals(1, reader.take())
        reader.mark()
        reader.take(2)
        reader.reset()
        assertEquals(2, reader.take())
    }

    @Test
    fun testCopyWithLimit() {
        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12))
        assertEquals(1, reader.take())
        val copy = reader.copy(5)
        assertContentEquals(byteArrayOf(2, 3, 4, 5, 6), copy.takeRemaining())
        copy.reset()
        assertContentEquals(byteArrayOf(2, 3, 4), copy.takeUntil(5))
        assertContentEquals(byteArrayOf(5, 6), copy.takeUntil(8))
        // copies are independent — the parent's position never moved
        assertContentEquals(byteArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), reader.takeRemaining())
        assertEquals(null, copy.take())
    }

    @Test
    fun testExpect() {
        val reader = ByteArrayByteReader(byteArrayOf(0xCA.toByte(), 0xFE.toByte()))
        reader.expect(0xCA)
        reader.expect(0xFE)
        reader.expectEOS()
    }

    @Test
    fun testExpectFailsWithOffset() {
        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3))
        reader.take(2)
        val e = assertFailsWith<ParseException> { reader.expect(0xFF) }
        // position is reported post-take, same as CharReader's line:col
        assertEquals(3, e.byteOffset)
        assertTrue(e.message!!.contains("0x3"))
    }

    @Test
    fun testParse() {
        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3, 4))
        assertEquals(0x0102, reader.parse { takeU2() })
        assertEquals(0x0304, reader.takeU2())
        assertNull(reader.take())

        reader.reset()
        assertEquals(
            1, reader.parse(
                { expect(0xFF) },
                { expect(1) }
            )
        )
    }

    @Test
    fun testParseOrNull() {
        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3, 4))
        assertNull(
            reader.parseOrNull(
                { expect(0xFF) },
                { expect(0xFE) }
            )
        )
        assertEquals(1, reader.take())
    }

    @Test
    fun testBufferedByteReader() {
        val backing = ByteArrayByteReader(byteArrayOf(1, 2, 3, 4, 5))
        val reader = ByteReader.BufferedByteReader(backing, Int.MAX_VALUE)
        reader.mark()
        assertEquals(1, reader.take())
        reader.reset()
        assertEquals(1, reader.take())
        val copy = reader.copy(2)
        assertContentEquals(byteArrayOf(2, 3), copy.takeRemaining())
        assertNull(copy.take())
        assertEquals(2, reader.take())
        // parse over the buffered reader — success commits position
        assertEquals(0x0304, reader.parse { takeU2() })
        assertEquals(5, reader.take())
        reader.expectEOS()
    }

    @Test
    fun testModifiedUtf8() {
        // "java/lang/Object" straight ascii
        val ascii = "java/lang/Object"
        val reader = ByteArrayByteReader(ascii.encodeToByteArray())
        assertEquals(ascii, reader.takeModifiedUtf8(ascii.length))

        // U+0000 stored as C0 80
        val withNul = byteArrayOf(0xC0.toByte(), 0x80.toByte())
        assertEquals("\u0000", ByteArrayByteReader(withNul).takeModifiedUtf8(2))

        // U+00E9 (é) as 2-byte C3 A9
        assertEquals("é", ByteArrayByteReader(byteArrayOf(0xC3.toByte(), 0xA9.toByte())).takeModifiedUtf8(2))

        // U+FFFF-ish 3-byte: U+20AC (€) = E2 82 AC
        assertEquals("€", ByteArrayByteReader(byteArrayOf(0xE2.toByte(), 0x82.toByte(), 0xAC.toByte())).takeModifiedUtf8(3))
    }

    // --- concurrent copies / EOS / mark-transparency ---

    @Test
    fun testMultipleConcurrentCopies() {
        val base = ByteReader.BufferedByteReader(
            ByteArrayByteReader(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)),
            Int.MAX_VALUE
        )
        val a = base.copy()
        assertEquals(0, a.take())
        assertEquals(1, a.take())
        assertEquals(2, a.take())
        val b = base.copy()
        assertEquals(0, b.take())
        assertEquals(3, a.take())
        assertEquals(1, b.take())
        assertEquals(0, base.take())
        // drain a — EOS must be sticky
        while (a.take() != null) {
        }
        assertNull(a.take())
        assertNull(a.take())
        // b continues independently mid-buffer
        assertEquals(2, b.take())
        assertEquals(3, b.take())
        assertEquals(4, b.take())
        // parent untouched beyond its own take
        assertEquals(1, base.take())
    }

    @Test
    fun testTakeAtEOSIsStable() {
        val reader = ByteReader.BufferedByteReader(
            ByteArrayByteReader(byteArrayOf(0, 1)),
            Int.MAX_VALUE
        )
        val copy = reader.copy()
        assertContentEquals(byteArrayOf(0, 1), copy.take(2))
        assertNull(copy.take())
        assertNull(copy.take())
        val c2 = reader.copy()
        assertContentEquals(byteArrayOf(0, 1), c2.take(2))
        assertNull(c2.take())
        assertNull(c2.take())
        assertContentEquals(byteArrayOf(0, 1), reader.take(2))
        assertNull(reader.take())
        assertNull(reader.take())
    }

    @Test
    fun testCopyLimitRespected() {
        val reader = ByteReader.BufferedByteReader(
            ByteArrayByteReader(byteArrayOf(0, 1, 2, 3)),
            Int.MAX_VALUE
        )
        val copy = reader.copy(2)
        assertEquals(0, copy.take())
        assertEquals(1, copy.take())
        assertNull(copy.take())
        assertNull(copy.take())
        assertNull(copy.peek())
        assertContentEquals(byteArrayOf(0, 1, 2, 3), reader.take(4))
    }

    @Test
    fun testNestedCopies() {
        val reader = ByteReader.BufferedByteReader(
            ByteArrayByteReader(byteArrayOf(10, 11, 12, 13, 14, 15)),
            Int.MAX_VALUE
        )
        val outer = reader.copy(4)
        assertEquals(10, outer.take())
        val inner = outer.copy(2)
        assertContentEquals(byteArrayOf(11, 12), inner.take(2))
        assertNull(inner.take())
        assertContentEquals(byteArrayOf(11, 12, 13), outer.take(3))
        assertNull(outer.take())
        assertContentEquals(byteArrayOf(10, 11, 12, 13, 14, 15), reader.take(6))
    }

    @Test
    fun testMarkSurvivesParse() {
        val reader = ByteArrayByteReader(byteArrayOf(0, 1, 2, 3, 4))
        reader.take(1)
        reader.mark()
        reader.take(2)
        val r = reader.parse {
            expect(3)
            "ok"
        }
        assertEquals("ok", r)
        reader.reset()
        assertContentEquals(byteArrayOf(1, 2, 3, 4), reader.take(4))
    }
}
