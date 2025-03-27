package xyz.wagyourtail.commons.collection;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class PriorityFiFoQueue<E> extends AbstractQueue<E> {
    private final PriorityQueue<Task> underlying = new PriorityQueue<>();
    /**
     * The comparator, or null if priority queue uses elements'
     * natural ordering.
     */
    private final Comparator<? super E> comparator;
    private long insertionOrder = 0;

    public PriorityFiFoQueue(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    public PriorityFiFoQueue() {
        comparator = (a, b) -> ((Comparable<E>) a).compareTo(b);
    }


    @Override
    public Iterator<E> iterator() {
        Iterator<Task> underlyingIter = underlying.iterator();
        return new Iterator<E>() {
            @Override
            public boolean hasNext() {
                return underlyingIter.hasNext();
            }

            @Override
            public E next() {
                return underlyingIter.next().element;
            }
        };
    }

    @Override
    public int size() {
        return underlying.size();
    }

    @Override
    public boolean add(E e) {
        return underlying.add(new Task(e, insertionOrder++));
    }

    @Override
    public boolean offer(E e) {
        return underlying.offer(new Task(e, insertionOrder++));
    }

    @Override
    public E poll() {
        Task t = underlying.poll();
        return t == null ? null : t.element;
    }

    @Override
    public E peek() {
        Task t = underlying.peek();
        return t == null ? null : t.element;
    }


    private class Task implements Comparable<Task> {
        private final E element;
        private final long insertionOrder;

        public Task(E element, long insertionOrder) {
            this.element = element;
            this.insertionOrder = insertionOrder;
        }

        @Override
        public int compareTo(@NotNull PriorityFiFoQueue<E>.Task o) {
            int compare = comparator.compare(element, o.element);
            if (compare == 0) {
                return Long.signum(insertionOrder - o.insertionOrder);
            }
            return compare;
        }

    }

}
