package xyz.wagyourtail.commonskt.test.collection.sorted

import xyz.wagyourtail.commonskt.collection.sorted.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import java.util.TreeMap

class SortedMapTest {

    @Test
    fun testBasicOperations() {
        val ourMap = mutableSortedMapOf(5 to "five", 2 to "two", 8 to "eight", 1 to "one", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(5 to "five", 2 to "two", 8 to "eight", 1 to "one", 9 to "nine"))
        }

        assertEquals(jvmMap.size, ourMap.size)
        assertEquals(jvmMap.keys.toList(), ourMap.keys.toList())
        assertEquals(jvmMap.values.toList(), ourMap.values.toList())
        assertEquals(jvmMap.entries.map { it.key to it.value }, ourMap.entries.map { it.key to it.value })
        
        assertEquals(jvmMap.firstKey(), ourMap.firstKey())
        assertEquals(jvmMap.lastKey(), ourMap.lastKey())
    }

    @Test
    fun testWithCustomComparator() {
        val ourMap = mutableSortedMapOf(
            "banana" to 1,
            "apple" to 2, 
            "cherry" to 3,
            comparator = compareBy { it.length }
        )
        val jvmMap = TreeMap<String, Int>(compareBy { it.length }).apply { 
            putAll(listOf("banana" to 1, "apple" to 2, "cherry" to 3))
        }

        assertEquals(jvmMap.size, ourMap.size)
        assertEquals(jvmMap.keys.toList(), ourMap.keys.toList())
        assertEquals(jvmMap.firstKey(), ourMap.firstKey())
        assertEquals(jvmMap.lastKey(), ourMap.lastKey())
    }

    @Test
    fun testHeadMap() {
        val ourMap = mutableSortedMapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine"))
        }

        val ourHeadMap = ourMap.headMap(7)
        val jvmHeadMap = jvmMap.headMap(7)

        assertEquals(jvmHeadMap.size, ourHeadMap.size)
        assertEquals(jvmHeadMap.keys.toList(), ourHeadMap.keys.toList())
        assertEquals(jvmHeadMap.values.toList(), ourHeadMap.values.toList())
    }

    @Test
    fun testTailMap() {
        val ourMap = mutableSortedMapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine"))
        }

        val ourTailMap = ourMap.tailMap(5)
        val jvmTailMap = jvmMap.tailMap(5)

        assertEquals(jvmTailMap.size, ourTailMap.size)
        assertEquals(jvmTailMap.keys.toList(), ourTailMap.keys.toList())
        assertEquals(jvmTailMap.values.toList(), ourTailMap.values.toList())
    }

    @Test
    fun testSubMap() {
        val ourMap = mutableSortedMapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine"))
        }

        val ourSubMap = ourMap.subMap(3, 9)
        val jvmSubMap = jvmMap.subMap(3, 9)

        assertEquals(jvmSubMap.size, ourSubMap.size)
        assertEquals(jvmSubMap.keys.toList(), ourSubMap.keys.toList())
        assertEquals(jvmSubMap.values.toList(), ourSubMap.values.toList())
    }

    @Test
    fun testPutAndGet() {
        val ourMap = MutableSortedMapImpl<Int, String>(naturalOrder())
        val jvmMap = TreeMap<Int, String>()

        // Test put
        assertEquals(jvmMap.put(5, "five"), ourMap.put(5, "five"))
        assertEquals(jvmMap.put(3, "three"), ourMap.put(3, "three"))
        assertEquals(jvmMap.put(7, "seven"), ourMap.put(7, "seven"))
        
        // Test put with existing key
        assertEquals(jvmMap.put(5, "FIVE"), ourMap.put(5, "FIVE"))
        
        assertEquals(jvmMap.size, ourMap.size)
        assertEquals(jvmMap.keys.toList(), ourMap.keys.toList())
        assertEquals(jvmMap.values.toList(), ourMap.values.toList())

        // Test get
        assertEquals(jvmMap[5], ourMap[5])
        assertEquals(jvmMap[3], ourMap[3])
        assertEquals(jvmMap[10], ourMap[10])
    }

    @Test
    fun testRemove() {
        val ourMap = MutableSortedMapImpl<Int, String>(naturalOrder())
        val jvmMap = TreeMap<Int, String>()

        mapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine").forEach { (k, v) ->
            ourMap.put(k, v)
            jvmMap.put(k, v)
        }

        // Test remove existing key
        assertEquals(jvmMap.remove(3), ourMap.remove(3))
        
        // Test remove non-existing key
        assertEquals(jvmMap.remove(10), ourMap.remove(10))
        
        assertEquals(jvmMap.size, ourMap.size)
        assertEquals(jvmMap.keys.toList(), ourMap.keys.toList())
        assertEquals(jvmMap.values.toList(), ourMap.values.toList())
    }

    @Test
    fun testContains() {
        val ourMap = mutableSortedMapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine"))
        }

        // Test containsKey
        assertEquals(jvmMap.containsKey(5), ourMap.containsKey(5))
        assertEquals(jvmMap.containsKey(10), ourMap.containsKey(10))

        // Test containsValue
        assertEquals(jvmMap.containsValue("five"), ourMap.containsValue("five"))
        assertEquals(jvmMap.containsValue("ten"), ourMap.containsValue("ten"))
    }

    @Test
    fun testPutAll() {
        val ourMap = MutableSortedMapImpl<Int, String>(naturalOrder())
        val jvmMap = TreeMap<Int, String>()

        ourMap.putAll(mapOf(5 to "five", 3 to "three", 7 to "seven"))
        jvmMap.putAll(mapOf(5 to "five", 3 to "three", 7 to "seven"))

        assertEquals(jvmMap.size, ourMap.size)
        assertEquals(jvmMap.keys.toList(), ourMap.keys.toList())
        assertEquals(jvmMap.values.toList(), ourMap.values.toList())
    }

    @Test
    fun testClear() {
        val ourMap = MutableSortedMapImpl<Int, String>(naturalOrder())
        val jvmMap = TreeMap<Int, String>()

        mapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine").forEach { (k, v) ->
            ourMap.put(k, v)
            jvmMap.put(k, v)
        }

        ourMap.clear()
        jvmMap.clear()

        assertEquals(jvmMap.size, ourMap.size)
        assertEquals(jvmMap.isEmpty(), ourMap.isEmpty())
        assertTrue(ourMap.isEmpty())
    }

    @Test
    fun testEmptyMap() {
        val ourMap = mutableSortedMapOf<Int, String>()
        val jvmMap = TreeMap<Int, String>()

        assertEquals(jvmMap.size, ourMap.size)
        assertEquals(jvmMap.isEmpty(), ourMap.isEmpty())
        
        assertFailsWith<IndexOutOfBoundsException> { ourMap.firstKey() }
        assertFailsWith<IndexOutOfBoundsException> { ourMap.lastKey() }
    }

    @Test
    fun testSingleElement() {
        val ourMap = mutableSortedMapOf(5 to "five")
        val jvmMap = TreeMap<Int, String>().apply { put(5, "five") }

        assertEquals(jvmMap.size, ourMap.size)
        assertEquals(jvmMap.firstKey(), ourMap.firstKey())
        assertEquals(jvmMap.lastKey(), ourMap.lastKey())
        assertEquals(jvmMap[5], ourMap[5])
    }

    @Test
    fun testKeysSet() {
        val ourMap = mutableSortedMapOf(5 to "five", 2 to "two", 8 to "eight", 1 to "one", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(5 to "five", 2 to "two", 8 to "eight", 1 to "one", 9 to "nine"))
        }

        val ourKeys = ourMap.keys
        val jvmKeys = jvmMap.keys

        assertEquals(jvmKeys.size, ourKeys.size)
        assertEquals(jvmKeys.toList(), ourKeys.toList())
        assertEquals(jvmKeys.first(), ourKeys.first())
        assertEquals(jvmKeys.last(), ourKeys.last())

        // Test contains on keys
        assertEquals(jvmKeys.contains(5), ourKeys.contains(5))
        assertEquals(jvmKeys.contains(10), ourKeys.contains(10))
    }

    @Test
    fun testValuesCollection() {
        val ourMap = mutableSortedMapOf(5 to "five", 2 to "two", 8 to "eight", 1 to "one", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(5 to "five", 2 to "two", 8 to "eight", 1 to "one", 9 to "nine"))
        }

        val ourValues = ourMap.values
        val jvmValues = jvmMap.values

        assertEquals(jvmValues.size, ourValues.size)
        assertEquals(jvmValues.toList().sorted(), ourValues.toList().sorted())

        // Test contains on values
        assertEquals(jvmValues.contains("five"), ourValues.contains("five"))
        assertEquals(jvmValues.contains("ten"), ourValues.contains("ten"))

        // Test containsAll on values
        assertEquals(jvmValues.containsAll(listOf("five", "two")), ourValues.containsAll(listOf("five", "two")))
        assertEquals(jvmValues.containsAll(listOf("five", "ten")), ourValues.containsAll(listOf("five", "ten")))
    }

    @Test
    fun testEntriesSet() {
        val ourMap = mutableSortedMapOf(5 to "five", 2 to "two", 8 to "eight", 1 to "one", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(5 to "five", 2 to "two", 8 to "eight", 1 to "one", 9 to "nine"))
        }

        val ourEntries = ourMap.entries
        val jvmEntries = jvmMap.entries

        assertEquals(jvmEntries.size, ourEntries.size)
        assertEquals(jvmEntries.map { it.key to it.value }, ourEntries.map { it.key to it.value })
        assertEquals(jvmEntries.first().key, ourEntries.first().key)
        assertEquals(jvmEntries.last().key, ourEntries.last().key)

        // Test contains on entries
        val ourEntry = ourEntries.find { it.key == 5 }
        val jvmEntry = jvmEntries.find { it.key == 5 }
        assertNotNull(ourEntry)
        assertNotNull(jvmEntry)
        assertEquals(jvmEntry.value, ourEntry.value)
    }

    @Test
    fun testIterator() {
        val ourMap = mutableSortedMapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine"))
        }

        val ourIterator = ourMap.iterator()
        val jvmIterator = jvmMap.iterator()

        while (jvmIterator.hasNext()) {
            assertTrue(ourIterator.hasNext())
            val ourNext = ourIterator.next()
            val jvmNext = jvmIterator.next()
            assertEquals(jvmNext.key, ourNext.key)
            assertEquals(jvmNext.value, ourNext.value)
        }
        assertFalse(ourIterator.hasNext())
    }

    @Test
    fun testMutableIterator() {
        val ourMap = MutableSortedMapImpl<Int, String>(naturalOrder())
        val jvmMap = TreeMap<Int, String>()

        mapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine").forEach { (k, v) ->
            ourMap.put(k, v)
            jvmMap.put(k, v)
        }

        val ourIterator = ourMap.entries.iterator()
        val jvmIterator = jvmMap.entries.iterator()

        // Test removal during iteration
        while (jvmIterator.hasNext()) {
            assertTrue(ourIterator.hasNext())
            val ourNext = ourIterator.next()
            val jvmNext = jvmIterator.next()
            assertEquals(jvmNext.key, ourNext.key)
            assertEquals(jvmNext.value, ourNext.value)
            
            if (ourNext.key == 3) {
                ourIterator.remove()
                jvmIterator.remove()
            }
        }

        assertEquals(jvmMap.size, ourMap.size)
        assertEquals(jvmMap.keys.toList(), ourMap.keys.toList())
        assertEquals(jvmMap.values.toList(), ourMap.values.toList())
    }

    @Test
    fun testSubMapOperations() {
        val ourMap = mutableSortedMapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine")
        val jvmMap = TreeMap<Int, String>().apply { 
            putAll(mapOf(1 to "one", 3 to "three", 5 to "five", 7 to "seven", 9 to "nine"))
        }

        val ourSubMap = ourMap.subMap(3, 9)
        val jvmSubMap = jvmMap.subMap(3, 9)

        // Test that subMap operations work correctly
        assertEquals(jvmSubMap.size, ourSubMap.size)
        assertEquals(jvmSubMap.keys.toList(), ourSubMap.keys.toList())
        assertEquals(jvmSubMap.values.toList(), ourSubMap.values.toList())
        
        // Test firstKey/lastKey on submap
        assertEquals(jvmSubMap.firstKey(), ourSubMap.firstKey())
        assertEquals(jvmSubMap.lastKey(), ourSubMap.lastKey())
    }
}