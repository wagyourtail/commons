package xyz.wagyourtail.commonskt.test.utils

import xyz.wagyourtail.commonskt.utils.toHex
import kotlin.test.Test
import kotlin.test.assertEquals

class TestNumberUtils {

    @Test
    fun testToHex() {
        assertEquals("", byteArrayOf().toHex())
        assertEquals("00ff10", byteArrayOf(0x00, 0xff.toByte(), 0x10).toHex())
        assertEquals(
            "0123456789abcdef",
            byteArrayOf(0x01, 0x23, 0x45, 0x67, 0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte()).toHex()
        )
        assertEquals("00FF10", byteArrayOf(0x00, 0xff.toByte(), 0x10).toHex(uppercase = true))
    }

}
