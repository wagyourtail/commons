package xyz.wagyourtail.commonskt.test.utils

import xyz.wagyourtail.commonskt.utils.compareTo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestIterableUtils {

    @Test
    fun testCompare() {
        val list1 = listOf(1, 2, 3)
        val list2 = listOf(1, 2, 3)
        assertEquals(0, list1.compareTo(list2))

        val list3 = listOf(1, 2, 4)
        assertTrue { list1 < list3 }
        assertTrue { list3 > list1 }

        val list4 = listOf(1, 2, 3, 4)
        assertTrue { list1 < list4 }
        assertTrue { list4 > list1 }
    }

}