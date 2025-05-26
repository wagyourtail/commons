package xyz.wagyourtail.commonskt.test.collection

import xyz.wagyourtail.commonskt.collection.ArrayMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArrayMapTest {

    @Test
    fun testBasicOperations() {
        val map = ArrayMap<String, String>()

        // Test empty map
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())

        // Test put and get
        assertNull(map.put("key1", "value1"))
        assertEquals("value1", map["key1"])
        assertEquals(1, map.size)
        assertFalse(map.isEmpty())

        // Test updating existing key
        assertEquals("value1", map.put("key1", "newValue1"))
        assertEquals("newValue1", map["key1"])
        assertEquals(1, map.size)

        // Test multiple entries
        map["key2"] = "value2"
        map["key3"] = "value3"
        assertEquals(3, map.size)
        assertEquals("newValue1", map["key1"])
        assertEquals("value2", map["key2"])
        assertEquals("value3", map["key3"])

        // Test containsKey
        assertTrue(map.containsKey("key1"))
        assertTrue(map.containsKey("key2"))
        assertFalse(map.containsKey("nonExistentKey"))

        // Test remove
        assertEquals("value2", map.remove("key2"))
        assertEquals(2, map.size)
        assertNull(map["key2"])
        assertNull(map.remove("nonExistentKey"))
    }

    @Test
    fun testCollectionViews() {
        val map = ArrayMap<String, Int>()
        map["one"] = 1
        map["two"] = 2
        map["three"] = 3

        // Test keys
        val keys = map.keys
        assertEquals(3, keys.size)
        assertTrue(keys.contains("one"))
        assertTrue(keys.contains("two"))
        assertTrue(keys.contains("three"))

        // Test values
        val values = map.values
        assertEquals(3, values.size)
        assertTrue(values.contains(1))
        assertTrue(values.contains(2))
        assertTrue(values.contains(3))

        // Test entries
        val entries = map.entries
        assertEquals(3, entries.size)
        assertTrue(entries.any { it.key == "one" && it.value == 1 })
        assertTrue(entries.any { it.key == "two" && it.value == 2 })
        assertTrue(entries.any { it.key == "three" && it.value == 3 })
    }

    @Test
    fun testBulkOperations() {
        val map = ArrayMap<String, Int>()
        map["one"] = 1

        // Test putAll
        val otherMap = mapOf("two" to 2, "three" to 3)
        map.putAll(otherMap)
        assertEquals(3, map.size)
        assertEquals(1, map["one"])
        assertEquals(2, map["two"])
        assertEquals(3, map["three"])

        // Test clear
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
        assertNull(map["one"])
    }

    @Test
    fun testNullValues() {
        val map = ArrayMap<String, String?>()

        // Test null values
        map["nullKey"] = null
        assertTrue(map.containsKey("nullKey"))
        assertNull(map["nullKey"])

        // Test replacing null with non-null
        assertNull(map.put("nullKey", "nonNullValue"))
        assertEquals("nonNullValue", map["nullKey"])

        // Test replacing non-null with null
        assertEquals("nonNullValue", map.put("nullKey", null))
        assertNull(map["nullKey"])
    }

    @Test
    fun testIterator() {
        val map = ArrayMap<String, Int>()
        map["one"] = 1
        map["two"] = 2
        map["three"] = 3

        // Test iterator
        val iterator = map.entries.iterator()
        assertTrue(iterator.hasNext())

        var entry = iterator.next()
        assertEquals("one", entry.key)
        assertEquals(1, entry.value)

        // Test modifying value through entry
        entry.setValue(10)
        assertEquals(10, map["one"])

        // Test removing through iterator
        iterator.remove()
        assertEquals(2, map.size)
        assertFalse(map.containsKey("one"))

        // Continue iteration
        assertTrue(iterator.hasNext())
        entry = iterator.next()
        assertEquals("two", entry.key)
        assertEquals(2, entry.value)

        assertTrue(iterator.hasNext())
        entry = iterator.next()
        assertEquals("three", entry.key)
        assertEquals(3, entry.value)

        assertFalse(iterator.hasNext())
        assertFails {
            iterator.next()
        }
    }

    @Test
    fun testOrderPreservation() {
        val map = ArrayMap<String, Int>()

        // Add entries
        map["three"] = 3
        map["one"] = 1
        map["two"] = 2

        // Verify order is preserved
        val keys = map.keys.toList()
        assertEquals("three", keys[0])
        assertEquals("one", keys[1])
        assertEquals("two", keys[2])

        // Update existing key and verify order is maintained
        map["one"] = 10
        val updatedKeys = map.keys.toList()
        assertEquals("three", updatedKeys[0])
        assertEquals("one", updatedKeys[1])
        assertEquals("two", updatedKeys[2])

        // Remove and add again - should be at the end
        map.remove("one")
        map["one"] = 100
        val finalKeys = map.keys.toList()
        assertEquals("three", finalKeys[0])
        assertEquals("two", finalKeys[1])
        assertEquals("one", finalKeys[2])
    }
}
