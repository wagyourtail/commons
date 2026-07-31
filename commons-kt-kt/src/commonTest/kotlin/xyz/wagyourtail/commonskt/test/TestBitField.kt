package xyz.wagyourtail.commonskt.test

import xyz.wagyourtail.commonskt.BitField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestBitField {

    class BitFieldA : BitField() {

        var flag1 by entry(0b1)
        var flag2 by entry(0b10, true)
        var flag3 by entry(0b100)

    }

    @Test
    fun test() {
        val bitField = BitFieldA()

        assertFalse(bitField.flag1)
        assertTrue(bitField.flag2)
        assertFalse(bitField.flag3)

        bitField.flag1 = true
        assertTrue(bitField.flag1)

        assertEquals(bitField.field, 3)
    }

}