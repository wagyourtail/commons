package xyz.wagyourtail.commonskt.test.reader

import xyz.wagyourtail.commonskt.reader.ByteArrayByteReader
import xyz.wagyourtail.commonskt.reader.ByteReader
import xyz.wagyourtail.commonskt.reader.InputStreamByteReader
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue
import kotlin.test.fail


/**
 * Capability proof for [ByteReader]: walk an entire real `.class` file —
 * header, full constant pool, members, and every attribute — using only the
 * reader primitives. Not a class-file parser; attributes are navigated with
 * `copy(attribute_length)` sub-readers the way a real parser would.
 */
class TestByteReaderJvm {

    @Test
    fun testWalkEntireClassFile() {
        val bytes = javaClass.getResourceAsStream("TestByteReaderJvm.class")!!.readBytes()
        val reader = ByteArrayByteReader(bytes)
        walkClassFile(reader)
        // sanity: parsed version is a plausible javac output
        assertEquals(bytes.size.toLong(), reader.position.toLong())
    }

    companion object {

        fun walkClassFile(reader: ByteReader<*>) {
            reader.expect(0xCA)
            reader.expect(0xFE)
            reader.expect(0xBA)
            reader.expect(0xBE)
            val minor = reader.takeU2()
            val major = reader.takeU2()
            assertTrue(major in 45..80, "implausible major version $major")

            val constantPoolCount = reader.takeU2()
            var cpCount = 0
            var i = 1
            while (i < constantPoolCount) {
                cpCount++
                when (val tag = reader.takeU1()) {
                    1 -> { // Utf8
                        val len = reader.takeU2()
                        reader.takeBytes(len)
                    }
                    3, 4 -> reader.takeI4() // Integer, Float
                    5, 6 -> { // Long, Double — take up two entries
                        reader.takeI8()
                        i++
                    }
                    7, 8, 16, 19, 20 -> reader.takeU2() // Class, String, MethodType, Module, Package
                    9, 10, 11, 12, 17, 18 -> { // refs, NameAndType, Dynamic, InvokeDynamic
                        reader.takeU2()
                        reader.takeU2()
                    }
                    15 -> { // MethodHandle
                        reader.takeU1()
                        reader.takeU2()
                    }
                    else -> throw AssertionError("unknown constant pool tag $tag")
                }
                i++
            }
            assertTrue(cpCount > 0)

            reader.takeU2() // access_flags
            reader.takeU2() // this_class
            reader.takeU2() // super_class

            val interfacesCount = reader.takeU2()
            repeat(interfacesCount) { reader.takeU2() }

            val fieldsCount = reader.takeU2()
            repeat(fieldsCount) { walkMember(reader) }

            val methodsCount = reader.takeU2()
            assertTrue(methodsCount > 0, "expected at least one method")
            repeat(methodsCount) { walkMember(reader) }

            val attributesCount = reader.takeU2()
            repeat(attributesCount) { walkAttribute(reader) }

            reader.expectEOS()
        }

        private fun walkMember(reader: ByteReader<*>) {
            reader.takeU2() // access_flags
            reader.takeU2() // name_index
            reader.takeU2() // descriptor_index
            val attributesCount = reader.takeU2()
            repeat(attributesCount) { walkAttribute(reader) }
        }

        private fun walkAttribute(reader: ByteReader<*>) {
            reader.takeU2() // attribute_name_index
            val length = reader.takeU4()
            assertTrue(length <= Int.MAX_VALUE.toUInt(), "attribute too large")
            // length-delimited window — the primitive for nested structures;
            // reading the window does not move the parent, skip() commits it
            val attr = reader.copy(length.toInt())
            attr.takeRemaining()
            attr.expectEOS()
            reader.skip(length.toInt())
        }

    }


    @Test
    fun testStreamMarkReset() {
        // bounded replay mark/reset on a raw stream reader
        val reader = InputStreamByteReader(ByteArrayInputStream(byteArrayOf(0, 1, 2, 3, 4, 5)))
        assertEquals(0, reader.take())
        assertEquals(1, reader.take())
        reader.mark(3)
        assertEquals(2, reader.take())
        assertEquals(3, reader.take())
        reader.reset()
        assertEquals(2, reader.take())
        assertEquals(3, reader.take())
        assertEquals(4, reader.take())
        // reset again — mark still active
        reader.reset()
        assertContentEquals(byteArrayOf(2, 3, 4), reader.take(3))
        // exceed the readlimit → mark invalidated
        reader.reset()
        reader.take(4) // 2,3,4,5 — recording '5' exceeds limit 3
        try {
            reader.reset()
            fail("expected IllegalStateException")
        } catch (_: IllegalStateException) {
        }
    }

    @Test
    fun testStreamBufferedCopies() {
        // buffered() gives stream readers full multi-copy support
        val reader = InputStreamByteReader(ByteArrayInputStream(byteArrayOf(0, 1, 2, 3, 4, 5)))
        val base = reader.buffered()
        val a = base.copy()
        val b = base.copy()
        assertEquals(0, a.take())
        assertEquals(0, b.take())
        assertEquals(1, a.take())
        assertEquals(1, b.take())
        assertEquals(0, base.take())
    }

}
