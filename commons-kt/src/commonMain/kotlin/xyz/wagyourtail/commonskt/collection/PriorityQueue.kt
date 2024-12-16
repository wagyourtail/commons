package xyz.wagyourtail.commonskt.collection

class PriorityQueue<T>(val fifo: Boolean = false, private val comparator: Comparator<T>) {
    private val heap = mutableListOf<T>()
    private val insertionOrder = mutableListOf<Int>()
    private var orderCounter = 0

    companion object {
        operator fun <T: Comparable<T>> invoke(fifo: Boolean = false) = PriorityQueue(fifo, Comparable<T>::compareTo)
    }

    fun add(element: T) {
        heap.add(element)
        insertionOrder.add(orderCounter++)
        heapifyUp(heap.size - 1)
    }

    fun addAll(elements: Collection<T>) {
        elements.forEach { add(it) }
    }

    fun poll(): T? {
        if (heap.isEmpty()) return null
        val removed = heap[0]
        heap[0] = heap.last()
        insertionOrder[0] = insertionOrder.last()
        heap.removeAt(heap.size - 1)
        insertionOrder.removeAt(insertionOrder.size - 1)
        heapifyDown(0)
        return removed
    }

    fun peek(): T? = heap.firstOrNull()

    private fun heapifyUp(index: Int) {
        var currentIndex = index
        while (currentIndex > 0) {
            val parentIndex = (currentIndex - 1) / 2
            val value = comparator.compare(heap[currentIndex], heap[parentIndex])
            val isLess = value < 0 || (value == 0 && fifo && insertionOrder[currentIndex] < insertionOrder[parentIndex])
            if (isLess) {
                swap(currentIndex, parentIndex)
                currentIndex = parentIndex
            } else {
                break
            }
        }
    }

    private fun heapifyDown(index: Int) {
        var currentIndex = index
        while (currentIndex < heap.size) {
            val leftChildIndex = 2 * currentIndex + 1
            val rightChildIndex = 2 * currentIndex + 2
            var smallest = currentIndex

            if (leftChildIndex < heap.size) {
                val value = comparator.compare(heap[leftChildIndex], heap[smallest])
                val isLess = value < 0 || (value == 0 && fifo && insertionOrder[leftChildIndex] < insertionOrder[smallest])
                if (isLess) {
                    smallest = leftChildIndex
                }
            }

            if (rightChildIndex < heap.size) {
                val value = comparator.compare(heap[rightChildIndex], heap[smallest])
                val isLess = value < 0 || (value == 0 && fifo && insertionOrder[rightChildIndex] < insertionOrder[smallest])
                if (isLess) {
                    smallest = rightChildIndex
                }
            }

            if (smallest != currentIndex) {
                swap(currentIndex, smallest)
                currentIndex = smallest
            } else {
                break
            }
        }
    }

    private fun swap(i: Int, j: Int) {
        val tempElement = heap[i]
        heap[i] = heap[j]
        heap[j] = tempElement

        val tempOrder = insertionOrder[i]
        insertionOrder[i] = insertionOrder[j]
        insertionOrder[j] = tempOrder
    }
}

fun <T> priorityQueueOf(fifo: Boolean = false, comparator: Comparator<T>, vararg elements: T) = PriorityQueue(fifo, comparator).also {
    for (element in elements) {
        it.add(element)
    }
}

fun <T: Comparable<T>> priorityQueueOf(fifo: Boolean = false, vararg elements: T) = PriorityQueue<T>(fifo).also {
    for (element in elements) {
        it.add(element)
    }
}