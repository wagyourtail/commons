package xyz.wagyourtail.commonskt.test.collection

import xyz.wagyourtail.commonskt.collection.PriorityQueue
import kotlin.test.Test
import kotlin.test.assertEquals

class PriorityQueueTest {

    @Test
    fun testPriorityQueue() {
        val queue = PriorityQueue<Int>()
        queue.add(3)
        queue.add(1)
        queue.add(2)
        queue.add(4)
        queue.add(5)
        assertEquals(1, queue.poll())
        assertEquals(2, queue.peek())
        assertEquals(2, queue.poll())
        assertEquals(3, queue.peek())
        assertEquals(3, queue.poll())
        assertEquals(4, queue.peek())
        assertEquals(4, queue.poll())
        assertEquals(5, queue.peek())
        assertEquals(5, queue.poll())
        assertEquals(null, queue.poll())

        queue.add(10)
        queue.add(7)
        queue.add(9)
        assertEquals(7, queue.peek())
        assertEquals(7, queue.poll())
        assertEquals(9, queue.peek())
        assertEquals(9, queue.poll())
        assertEquals(10, queue.peek())
        assertEquals(10, queue.poll())
    }


    @Test
    fun testPriorityQueueDescending() {
        val queue = PriorityQueue<Int> { a, b -> b.compareTo(a) }
        queue.add(3)
        queue.add(1)
        queue.add(2)
        queue.add(4)
        queue.add(5)
        assertEquals(5, queue.poll())
        assertEquals(4, queue.peek())
        assertEquals(4, queue.poll())
        assertEquals(3, queue.peek())
        assertEquals(3, queue.poll())
        assertEquals(2, queue.peek())
        assertEquals(2, queue.poll())
        assertEquals(1, queue.peek())
        assertEquals(1, queue.poll())
        assertEquals(null, queue.poll())
    }

    @Test
    fun testPriorityQueueCustomObjects() {
        data class Task(val priority: Int, val name: String) : Comparable<Task> {
            override fun compareTo(other: Task): Int = this.priority.compareTo(other.priority)
        }

        val queue = PriorityQueue<Task>()
        queue.add(Task(3, "Low priority"))
        queue.add(Task(1, "High priority"))
        queue.add(Task(2, "Medium priority"))

        assertEquals(Task(1, "High priority"), queue.poll())
        assertEquals(Task(2, "Medium priority"), queue.peek())
        assertEquals(Task(2, "Medium priority"), queue.poll())
        assertEquals(Task(3, "Low priority"), queue.peek())
        assertEquals(Task(3, "Low priority"), queue.poll())
        assertEquals(null, queue.poll())


        val queue2 = PriorityQueue<Task>()
        queue2.add(Task(3, "Low priority1"))
        queue2.add(Task(1, "High priority1"))
        queue2.add(Task(2, "Medium priority1"))
        queue2.add(Task(2, "Medium priority2"))
        queue2.add(Task(3, "Low priority2"))

        assertEquals(Task(1, "High priority1"), queue2.poll())
        assertEquals(Task(2, "Medium priority2"), queue2.peek())
        assertEquals(Task(2, "Medium priority2"), queue2.poll())
        assertEquals(Task(2, "Medium priority1"), queue2.peek())
        assertEquals(Task(2, "Medium priority1"), queue2.poll())
        assertEquals(Task(3, "Low priority1"), queue2.peek())
        assertEquals(Task(3, "Low priority1"), queue2.poll())
        assertEquals(Task(3, "Low priority2"), queue2.peek())
        assertEquals(Task(3, "Low priority2"), queue2.poll())
        assertEquals(null, queue2.poll())


        val queue3 = PriorityQueue<Task>(true)
        queue3.add(Task(3, "Low priority1"))
        queue3.add(Task(1, "High priority1"))
        queue3.add(Task(2, "Medium priority1"))
        queue3.add(Task(2, "Medium priority2"))
        queue3.add(Task(3, "Low priority2"))

        assertEquals(Task(1, "High priority1"), queue3.poll())
        assertEquals(Task(2, "Medium priority1"), queue3.peek())
        assertEquals(Task(2, "Medium priority1"), queue3.poll())
        assertEquals(Task(2, "Medium priority2"), queue3.peek())
        assertEquals(Task(2, "Medium priority2"), queue3.poll())
        assertEquals(Task(3, "Low priority1"), queue3.peek())
        assertEquals(Task(3, "Low priority1"), queue3.poll())
        assertEquals(Task(3, "Low priority2"), queue3.peek())
        assertEquals(Task(3, "Low priority2"), queue3.poll())
        assertEquals(null, queue3.poll())
    }

}