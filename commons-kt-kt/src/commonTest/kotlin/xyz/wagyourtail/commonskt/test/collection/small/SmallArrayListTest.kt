package xyz.wagyourtail.commonskt.test.collection.small

import xyz.wagyourtail.commonskt.collection.small.SmallArrayList
import kotlin.test.*

class SmallArrayListTest {

    @Test
    fun `test add to empty list`() {
        val list = SmallArrayList<String>()
        list.add(0, "Test")
        assertEquals(1, list.size)
        assertEquals("Test", list[0])
    }

    @Test
    fun `test add at specific index`() {
        val list = SmallArrayList<String>()
        list.add(0, "First")
        list.add(1, "Second")
        list.add(1, "Inserted")
        assertEquals(3, list.size)
        assertEquals("First", list[0])
        assertEquals("Inserted", list[1])
        assertEquals("Second", list[2])
    }

    @Test
    fun `test add out of bounds throws exception`() {
        val list = SmallArrayList<String>()
        assertFailsWith<IndexOutOfBoundsException> {
            list.add(1, "OutOfBounds")
        }
    }

    @Test
    fun `test set element at index`() {
        val list = SmallArrayList<String>()
        list.add(0, "Original")
        val replacedElement = list.set(0, "Updated")
        assertEquals("Updated", list[0])
        assertEquals("Updated", replacedElement)
    }

    @Test
    fun `test set out of bounds throws exception`() {
        val list = SmallArrayList<String>()
        assertFailsWith<IndexOutOfBoundsException> {
            list.set(0, "OutOfBounds")
        }
    }

    @Test
    fun `test remove at index`() {
        val list = SmallArrayList<String>()
        list.add(0, "ToRemove")
        list.add(1, "ToKeep")
        val removedElement = list.removeAt(0)
        assertTrue(list.size == 1)
        assertEquals("ToRemove", removedElement)
        assertEquals("ToKeep", list[0])
    }

    @Test
    fun `test remove out of bounds throws exception`() {
        val list = SmallArrayList<String>()
        assertFailsWith<IndexOutOfBoundsException> {
            list.removeAt(0)
        }
    }

    @Test
    fun `test get element at index`() {
        val list = SmallArrayList<String>()
        list.add(0, "Element")
        val retrievedElement = list[0]
        assertEquals("Element", retrievedElement)
    }

    @Test
    fun `test get out of bounds throws exception`() {
        val list = SmallArrayList<String>()
        assertFailsWith<IndexOutOfBoundsException> {
            list[0]
        }
    }

    @Test
    fun `test resizing on adding beyond initial capacity`() {
        val list = SmallArrayList<Int>(initialSize = 2, resizeAmount = 2)
        list.add(0, 1)
        list.add(1, 2)
        list.add(2, 3) // Trigger resizing
        assertEquals(3, list.size)
        assertEquals(1, list[0])
        assertEquals(2, list[1])
        assertEquals(3, list[2])
    }

    @Test
    fun `test list initialization with non-default resize amount`() {
        val list = SmallArrayList<Int>(resizeAmount = 3)
        assertNotNull(list)
        assertEquals(0, list.size)
    }
}

fun <E> smallArrayListOf(
    vararg elements: E,
    initialSize: Int = elements.size,
    resizeAmount: Int = 1
): SmallArrayList<E> {
    val list = SmallArrayList<E>()
    for (element in elements) {
        list.add(element)
    }
    return list
}
