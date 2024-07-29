package xyz.wagyourtail.commonskt.test.collection

import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultMapTest {

    @Test
    fun testDefaultMap() {
        val map = defaultedMapOf<String, String> { it + "value" }

        assertEquals("keyvalue", map["key"])
        map["key"] = "newvalue"

        assertEquals("newvalue", map["key"])
        assertEquals("key2value", map["key2"])
    }

}