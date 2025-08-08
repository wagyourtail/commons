package xyz.wagyourtail.commonskt.test.collection.small

import xyz.wagyourtail.commonskt.collection.small.SmallArraySet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class SmallArraySetTest {

    @Test
    fun `test adding unique elements`() {
        val set = SmallArraySet<String>()
        assertTrue(set.add("A"))
        assertTrue(set.add("B"))
        assertTrue(set.add("C"))
        assertEquals(3, set.size)
        assertTrue(set.contains("A"))
        assertTrue(set.contains("B"))
        assertTrue(set.contains("C"))
    }

    @Test
    fun `test adding duplicate elements`() {
        val set = SmallArraySet<String>()
        assertTrue(set.add("A"))
        assertFalse(set.add("A"))
        assertEquals(1, set.size)
        assertTrue(set.contains("A"))
    }

    @Test
    fun `test removing elements`() {
        val set = SmallArraySet<String>()
        set.add("A")
        set.add("B")
        set.add("C")
        val iterator = set.iterator()
        assertTrue(iterator.hasNext())
        iterator.next()
        iterator.remove()
        assertEquals(2, set.size)
        assertFalse(set.contains("A"))
        assertTrue(set.contains("B"))
        assertTrue(set.contains("C"))
    }

    @Test
    fun `test removing elements by iterator with invalid state`() {
        val set = SmallArraySet<String>()
        val iterator = set.iterator()
        assertFailsWith(NoSuchElementException::class) { iterator.next() }
        assertFailsWith(IllegalStateException::class) { iterator.remove() }
    }

    @Test
    fun `test resizing backing array`() {
        val set = SmallArraySet<Int>(initialSize = 2, resizeAmount = 2)
        set.add(1)
        set.add(2)
        set.add(3)
        assertEquals(3, set.size)
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test
    fun `test remove element from middle of set`() {
        val set = SmallArraySet<String>(initialSize = 3, resizeAmount = 1)
        set.add("A")
        set.add("B")
        set.add("C")
        set.remove("B")
        assertEquals(2, set.size)
        assertFalse(set.contains("B"))
        assertTrue(set.contains("A"))
        assertTrue(set.contains("C"))
    }

    @Test
    fun `test iteration over set`() {
        val set = SmallArraySet<Int>()
        set.add(1)
        set.add(2)
        set.add(3)
        val elements = mutableListOf<Int>()
        for (element in set) {
            elements.add(element)
        }
        assertEquals(listOf(1, 2, 3), elements)
    }

}