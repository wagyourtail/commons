package xyz.wagyourtail.commonskt.test.collection.iterator

import xyz.wagyourtail.commonskt.collection.iterator.mutableIterator
import kotlin.test.Test
import kotlin.test.assertTrue

class MutableIteratorTest {

    @Test
    fun testMutableIter() {
        val primes = mutableListOf<Int>()

        val iter = mutableIterator {
            for (i in 0..10) {
                if (!yield(i)) {
                    primes.add(i)
                }
            }
            val toRemove = yieldAll(11..20)
            primes.addAll((11..20) - toRemove)
        }

        while (iter.hasNext()) {
            val next = iter.next()
            if (!next.isPrime()) {
//                iter.hasNext() // not allowed by this impl
                iter.remove()
            }
        }

        assertTrue(primes.isNotEmpty())
        assertTrue((primes - primeTable).isEmpty())
    }

    val primeTable = setOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)

    fun Int.isPrime(): Boolean {
        if (this <= 1) return false
        if (this < 30) {
            return this in primeTable
        } else {
            TODO()
        }
    }

}