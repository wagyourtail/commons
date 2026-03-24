package xyz.wagyourtail.commonskt.test.collection.sorted

import xyz.wagyourtail.commonskt.collection.sorted.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import java.util.TreeSet

class SortedSetTest {

    @Test
    fun testBasicOperations() {
        val ourSet = sortedSetOf(5, 2, 8, 1, 9)
        val jvmSet = TreeSet<Int>().apply { addAll(listOf(5, 2, 8, 1, 9)) }

        assertEquals(jvmSet.size, ourSet.size)
        assertEquals(jvmSet.toList(), ourSet.toList())
        
        assertEquals(jvmSet.first(), ourSet.first())
        assertEquals(jvmSet.last(), ourSet.last())
    }

    @Test
    fun testWithCustomComparator() {
        val ourSet = sortedSetOf("banana", "apple", "cherry", comparator = compareBy { it.length })
        val jvmSet = TreeSet<String>(compareBy { it.length }).apply { 
            addAll(listOf("banana", "apple", "cherry")) 
        }

        assertEquals(jvmSet.size, ourSet.size)
        assertEquals(jvmSet.toList(), ourSet.toList())
    }

    @Test
    fun testHeadSet() {
        val ourSet = sortedSetOf(1, 3, 5, 7, 9)
        val jvmSet = TreeSet<Int>().apply { addAll(listOf(1, 3, 5, 7, 9)) }

        val ourHeadSet = ourSet.headSet(7)
        val jvmHeadSet = jvmSet.headSet(7)

        assertEquals(jvmHeadSet.toList(), ourHeadSet.toList())
        assertEquals(jvmHeadSet.size, ourHeadSet.size)
    }

    @Test
    fun testTailSet() {
        val ourSet = sortedSetOf(1, 3, 5, 7, 9)
        val jvmSet = TreeSet<Int>().apply { addAll(listOf(1, 3, 5, 7, 9)) }

        val ourTailSet = ourSet.tailSet(5)
        val jvmTailSet = jvmSet.tailSet(5)

        assertEquals(jvmTailSet.toList(), ourTailSet.toList())
        assertEquals(jvmTailSet.size, ourTailSet.size)
    }

    @Test
    fun testSubSet() {
        val ourSet = sortedSetOf(1, 3, 5, 7, 9)
        val jvmSet = TreeSet<Int>().apply { addAll(listOf(1, 3, 5, 7, 9)) }

        val ourSubSet = ourSet.subSet(3, 9)
        val jvmSubSet = jvmSet.subSet(3, 9)

        assertEquals(jvmSubSet.toList(), ourSubSet.toList())
        assertEquals(jvmSubSet.size, ourSubSet.size)
    }

    @Test
    fun testMutableOperations() {
        val ourSet = MutableSortedSetImpl<Int>(naturalOrder())
        val jvmSet = TreeSet<Int>()

        // Test add
        assertEquals(jvmSet.add(5), ourSet.add(5))
        assertEquals(jvmSet.add(3), ourSet.add(3))
        assertEquals(jvmSet.add(7), ourSet.add(7))
        
        // Test duplicate add
        assertEquals(jvmSet.add(5), ourSet.add(5))
        
        assertEquals(jvmSet.toList(), ourSet.toList())
        assertEquals(jvmSet.size, ourSet.size)

        // Test remove
        assertEquals(jvmSet.remove(3), ourSet.remove(3))
        assertEquals(jvmSet.remove(10), ourSet.remove(10))
        
        assertEquals(jvmSet.toList(), ourSet.toList())
        assertEquals(jvmSet.size, ourSet.size)

        // Test contains
        assertEquals(jvmSet.contains(5), ourSet.contains(5))
        assertEquals(jvmSet.contains(3), ourSet.contains(3))

        // Test containsAll
        assertEquals(jvmSet.containsAll(listOf(5, 7)), ourSet.containsAll(listOf(5, 7)))
        assertEquals(jvmSet.containsAll(listOf(5, 3)), ourSet.containsAll(listOf(5, 3)))
    }

    @Test
    fun testBulkOperations() {
        val ourSet = MutableSortedSetImpl<Int>(naturalOrder())
        val jvmSet = TreeSet<Int>()

        // Test addAll
        assertEquals(jvmSet.addAll(listOf(5, 3, 7, 1)), ourSet.addAll(listOf(5, 3, 7, 1)))
        assertEquals(jvmSet.addAll(listOf(5, 9)), ourSet.addAll(listOf(5, 9)))
        
        assertEquals(jvmSet.toList(), ourSet.toList())
        assertEquals(jvmSet.size, ourSet.size)

        // Test removeAll
        assertEquals(jvmSet.removeAll(listOf(3, 9)), ourSet.removeAll(listOf(3, 9)))
        assertEquals(jvmSet.removeAll(listOf(10, 11)), ourSet.removeAll(listOf(10, 11)))
        
        assertEquals(jvmSet.toList(), ourSet.toList())
        assertEquals(jvmSet.size, ourSet.size)

        // Test retainAll
        assertEquals(jvmSet.retainAll(listOf(1, 5, 7)), ourSet.retainAll(listOf(1, 5, 7)))
        
        assertEquals(jvmSet.toList(), ourSet.toList())
        assertEquals(jvmSet.size, ourSet.size)
    }

    @Test
    fun testIterator() {
        val ourSet = sortedSetOf(1, 3, 5, 7, 9)
        val jvmSet = TreeSet<Int>().apply { addAll(listOf(1, 3, 5, 7, 9)) }

        val ourIterator = ourSet.iterator()
        val jvmIterator = jvmSet.iterator()

        while (jvmIterator.hasNext()) {
            assertTrue(ourIterator.hasNext())
            assertEquals(jvmIterator.next(), ourIterator.next())
        }
        assertFalse(ourIterator.hasNext())
    }

    @Test
    fun testMutableIterator() {
        val ourSet = MutableSortedSetImpl<Int>(naturalOrder())
        val jvmSet = TreeSet<Int>()

        listOf(1, 3, 5, 7, 9).forEach {
            ourSet.add(it)
            jvmSet.add(it)
        }

        val ourIterator = ourSet.iterator()
        val jvmIterator = jvmSet.iterator()

        // Test removal during iteration
        while (jvmIterator.hasNext()) {
            assertTrue(ourIterator.hasNext())
            val ourNext = ourIterator.next()
            val jvmNext = jvmIterator.next()
            assertEquals(jvmNext, ourNext)
            
            if (ourNext == 3) {
                ourIterator.remove()
                jvmIterator.remove()
            }
        }

        assertEquals(jvmSet.toList(), ourSet.toList())
        assertEquals(jvmSet.size, ourSet.size)
    }

    @Test
    fun testClear() {
        val ourSet = MutableSortedSetImpl<Int>(naturalOrder())
        val jvmSet = TreeSet<Int>()

        listOf(1, 3, 5, 7, 9).forEach {
            ourSet.add(it)
            jvmSet.add(it)
        }

        ourSet.clear()
        jvmSet.clear()

        assertEquals(jvmSet.toList(), ourSet.toList())
        assertEquals(jvmSet.size, ourSet.size)
        assertTrue(ourSet.isEmpty())
    }

    @Test
    fun testEmptySet() {
        val ourSet = sortedSetOf<Int>()
        val jvmSet = TreeSet<Int>()

        assertEquals(jvmSet.size, ourSet.size)
        assertEquals(jvmSet.isEmpty(), ourSet.isEmpty())
        
        assertFailsWith<IndexOutOfBoundsException> { ourSet.first() }
        assertFailsWith<IndexOutOfBoundsException> { ourSet.last() }
    }

    @Test
    fun testSingleElement() {
        val ourSet = sortedSetOf(5)
        val jvmSet = TreeSet<Int>().apply { add(5) }

        assertEquals(jvmSet.size, ourSet.size)
        assertEquals(jvmSet.first(), ourSet.first())
        assertEquals(jvmSet.last(), ourSet.last())
        assertEquals(jvmSet.toList(), ourSet.toList())
    }

    @Test
    fun testSubSetOperations() {
        val ourSet = sortedSetOf(1, 3, 5, 7, 9)
        val jvmSet = TreeSet<Int>().apply { addAll(listOf(1, 3, 5, 7, 9)) }

        val ourSubSet = ourSet.subSet(3, 9) as MutableSortedSet<Int>
        val jvmSubSet = jvmSet.subSet(3, 9) as TreeSet<Int>

        // Test that subSet operations work correctly
        assertEquals(jvmSubSet.size, ourSubSet.size)
        assertEquals(jvmSubSet.toList(), ourSubSet.toList())
        
        // Test first/last on subset
        assertEquals(jvmSubSet.first(), ourSubSet.first())
        assertEquals(jvmSubSet.last(), ourSubSet.last())
    }
}