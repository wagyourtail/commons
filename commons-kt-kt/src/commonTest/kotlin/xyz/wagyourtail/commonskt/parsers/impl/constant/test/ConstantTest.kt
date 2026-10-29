package xyz.wagyourtail.commonskt.parsers.impl.constant.test

import xyz.wagyourtail.commonskt.data.BuildStringVisitor
import xyz.wagyourtail.commonskt.parsers.impl.Constant
import xyz.wagyourtail.commonskt.reader.ParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull


class ConstantTest {

    @Test
    fun basicTests() {
        Constant.checked("null")
        Constant.checked("false")
        Constant.checked("100")
        Constant.checked("10e+24")
        Constant.checked("100.23e-46")
        Constant.checked(".23e-46")
        Constant.checked("0xFFFF10")
        Constant.checked("-100")
        Constant.checked("-InfinityF")
        Constant.checked("-.2f")
        Constant.checked("\"string\"")
    }

    @Test
    fun testIllegal() {
        assertFailsWith(ParseException::class) { Constant.checked("09") }

        assertFailsWith(ParseException::class) { Constant.checked("0xg") }

        assertFailsWith(ParseException::class) { Constant.checked("0b") }

        assertFailsWith(ParseException::class) { Constant.checked("0b2") }

        assertFailsWith(ParseException::class) { Constant.checked("0x") }

        assertFailsWith(ParseException::class) { Constant.checked("0x1h") }
    }

    @Test
    fun testVisitor() {
        assertEquals("null", BuildStringVisitor.apply(Constant.checked("null")))
        assertEquals("true", BuildStringVisitor.apply(Constant.checked("true")))
        assertEquals("10e+24", BuildStringVisitor.apply(Constant.checked("10e+24")))
        assertEquals("-NaND", BuildStringVisitor.apply(Constant.checked("-NaND")))
        assertEquals("0.10e+24f", BuildStringVisitor.apply(Constant.checked("0.10e+24f")))
        assertEquals("0b1010", BuildStringVisitor.apply(Constant.checked("0b1010")))
        assertEquals("0x1010L", BuildStringVisitor.apply(Constant.checked("0x1010L")))
        assertEquals("0123l", BuildStringVisitor.apply(Constant.checked("0123l")))
        assertEquals("0", BuildStringVisitor.apply(Constant.checked("0")))
        assertEquals("1", BuildStringVisitor.apply(Constant.checked("1")))
    }

    @Test
    fun testValues() {
        assertNull(Constant.checked("null").value)
        assertEquals(true, Constant.checked("true").value)
        assertEquals(10e24, Constant.checked("10e+24").value)
        assertEquals(Double.Companion.NaN, Constant.checked("NaND").value)
        assertEquals(0.10e24f, Constant.checked("0.10e+24f").value)
        assertEquals(10, Constant.checked("0b1010").value)
        assertEquals(0x1010L, Constant.checked("0x1010L").value)
        assertEquals(83L, Constant.checked("0123l").value)
        assertEquals(0, Constant.checked("0").value)
        assertEquals(1, Constant.checked("1").value)
        assertEquals(-110, Constant.checked("-110").value)
        assertEquals(-0x109L, Constant.checked("-0x109L").value)
        assertEquals("Test String", Constant.checked("\"Test String\"").value)
    }
}
