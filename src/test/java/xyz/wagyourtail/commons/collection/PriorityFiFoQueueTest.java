package xyz.wagyourtail.commons.collection;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PriorityFiFoQueueTest {

    @Test
    void testPriorityQueue() {
        PriorityFiFoQueue<Integer> queue = new PriorityFiFoQueue<>();
        queue.add(3);
        queue.add(1);
        queue.add(2);
        queue.add(4);
        queue.add(5);
        assertEquals(1, queue.poll());
        assertEquals(2, queue.peek());
        assertEquals(2, queue.poll());
        assertEquals(3, queue.peek());
        assertEquals(3, queue.poll());
        assertEquals(4, queue.peek());
        assertEquals(4, queue.poll());
        assertEquals(5, queue.peek());
        assertEquals(5, queue.poll());
        assertNull(queue.poll());

        queue.add(10);
        queue.add(7);
        queue.add(9);
        assertEquals(7, queue.peek());
        assertEquals(7, queue.poll());
        assertEquals(9, queue.peek());
        assertEquals(9, queue.poll());
        assertEquals(10, queue.peek());
        assertEquals(10, queue.poll());
    }

    @Test
    void testPriorityQueueDescending() {
        PriorityFiFoQueue<Integer> queue = new PriorityFiFoQueue<>(Comparator.reverseOrder());
        queue.add(3);
        queue.add(1);
        queue.add(2);
        queue.add(4);
        queue.add(5);
        assertEquals(5, queue.poll());
        assertEquals(4, queue.peek());
        assertEquals(4, queue.poll());
        assertEquals(3, queue.peek());
        assertEquals(3, queue.poll());
        assertEquals(2, queue.peek());
        assertEquals(2, queue.poll());
        assertEquals(1, queue.peek());
        assertEquals(1, queue.poll());
        assertNull(queue.poll());
    }

    @Test
    void testPriorityQueueCustomObjects() {
        class Task implements Comparable<Task> {
            int priority;
            String name;

            Task(int priority, String name) {
                this.priority = priority;
                this.name = name;
            }

            @Override
            public int compareTo(Task other) {
                return Integer.compare(this.priority, other.priority);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Task task = (Task) o;
                return priority == task.priority && name.equals(task.name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(priority, name);
            }

            @Override
            public String toString() {
                return "Task{" +
                        "priority=" + priority +
                        ", name='" + name + '\'' +
                        '}';
            }

        }

        PriorityFiFoQueue<Task> queue = new PriorityFiFoQueue<>();
        queue.add(new Task(3, "Low priority"));
        queue.add(new Task(1, "High priority"));
        queue.add(new Task(2, "Medium priority"));

        assertEquals(new Task(1, "High priority"), queue.poll());
        assertEquals(new Task(2, "Medium priority"), queue.peek());
        assertEquals(new Task(2, "Medium priority"), queue.poll());
        assertEquals(new Task(3, "Low priority"), queue.peek());
        assertEquals(new Task(3, "Low priority"), queue.poll());
        assertNull(queue.poll());

        PriorityFiFoQueue<Task> queue2 = new PriorityFiFoQueue<>();
        queue2.add(new Task(3, "Low priority1"));
        queue2.add(new Task(2, "Medium priority1"));
        queue2.add(new Task(1, "High priority1"));
        queue2.add(new Task(2, "Medium priority2"));
        queue2.add(new Task(3, "Low priority2"));
        queue2.add(new Task(2, "Medium priority3"));

        assertEquals(new Task(1, "High priority1"), queue2.poll());
        assertEquals(new Task(2, "Medium priority1"), queue2.peek());
        assertEquals(new Task(2, "Medium priority1"), queue2.poll());
        assertEquals(new Task(2, "Medium priority2"), queue2.peek());
        assertEquals(new Task(2, "Medium priority2"), queue2.poll());
        assertEquals(new Task(2, "Medium priority3"), queue2.peek());
        assertEquals(new Task(2, "Medium priority3"), queue2.poll());
        assertEquals(new Task(3, "Low priority1"), queue2.peek());
        assertEquals(new Task(3, "Low priority1"), queue2.poll());
        assertEquals(new Task(3, "Low priority2"), queue2.peek());
        assertEquals(new Task(3, "Low priority2"), queue2.poll());
        assertNull(queue2.poll());
    }


}