package xyz.wagyourtail.commonskt.test.collection

import xyz.wagyourtail.commonskt.collection.enum.enumSetAllOf
import xyz.wagyourtail.commonskt.collection.enum.enumSetOf
import xyz.wagyourtail.commonskt.collection.enum.mutableEnumSetAllOf
import xyz.wagyourtail.commonskt.collection.enum.mutableEnumSetOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EnumSetTest {

    enum class TestEnum {
        A,
        B,
        C,
        D,
        E
    }

    enum class TestLargeEnum {
        _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16,
        _17, _18, _19, _20, _21, _22, _23, _24, _25, _26, _27, _28, _29, _30, _31,
        _32, _33, _34, _35, _36, _37, _38, _39, _40, _41, _42, _43, _44, _45, _46, _47,
        _48, _49, _50, _51, _52, _53, _54, _55, _56, _57, _58, _59, _60, _61, _62, _63,
        _64, _65, _66, _67, _68, _69, _70, _71, _72, _73, _74, _75, _76, _77, _78, _79, _80
    }

    @Test
    fun testRegular() {
        val emptyEnumSet = enumSetOf<TestEnum>()
        assertTrue(emptyEnumSet.isEmpty())

        val enumSet = enumSetOf(TestEnum.A, TestEnum.B, TestEnum.C)
        assertTrue(enumSet.contains(TestEnum.A))
        assertTrue(enumSet.contains(TestEnum.B))
        assertTrue(enumSet.contains(TestEnum.C))
        assertTrue(!enumSet.contains(TestEnum.D))
        assertTrue(!enumSet.contains(TestEnum.E))

        assertTrue(enumSet.containsAll(listOf(TestEnum.A, TestEnum.B, TestEnum.C)))
        assertTrue(enumSet.containsAll(enumSetOf(TestEnum.A, TestEnum.B, TestEnum.C)))
        assertFalse(enumSet.containsAll(listOf(TestEnum.A, TestEnum.B, TestEnum.C, TestEnum.D)))
        assertFalse(enumSet.containsAll(enumSetOf(TestEnum.A, TestEnum.B, TestEnum.C, TestEnum.D)))

        val mut = mutableListOf<TestEnum>()
        for (testEnum in enumSet) {
            mut.add(testEnum)
        }
        assertEquals(listOf(TestEnum.A, TestEnum.B, TestEnum.C), mut)

        assertEquals("{A, B, C}", enumSet.toString())
    }

    @Test
    fun testJumbo() {
        val emptyEnumSet = enumSetOf<TestLargeEnum>()
        assertTrue(emptyEnumSet.isEmpty())

        val largeEnumSet = enumSetOf(TestLargeEnum._1, TestLargeEnum._2, TestLargeEnum._3)
        assertTrue(largeEnumSet.contains(TestLargeEnum._1))
        assertTrue(largeEnumSet.contains(TestLargeEnum._2))
        assertTrue(largeEnumSet.contains(TestLargeEnum._3))
        assertTrue(!largeEnumSet.contains(TestLargeEnum._4))
        assertTrue(!largeEnumSet.contains(TestLargeEnum._5))

        assertTrue(largeEnumSet.containsAll(listOf(TestLargeEnum._1, TestLargeEnum._2, TestLargeEnum._3)))
        assertTrue(largeEnumSet.containsAll(enumSetOf(TestLargeEnum._1, TestLargeEnum._2, TestLargeEnum._3)))
        assertFalse(largeEnumSet.containsAll(listOf(TestLargeEnum._1, TestLargeEnum._2, TestLargeEnum._3, TestLargeEnum._4)))

        val mut = mutableListOf<TestLargeEnum>()
        for (testEnum in largeEnumSet) {
            mut.add(testEnum)
        }
        assertEquals(listOf(TestLargeEnum._1, TestLargeEnum._2, TestLargeEnum._3), mut)

        assertEquals("{_1, _2, _3}", largeEnumSet.toString())
    }

    @Test
    fun testRegularMutable() {
        val enumSet = mutableEnumSetOf(TestEnum.A, TestEnum.B, TestEnum.C)
        assertTrue(enumSet.remove(TestEnum.A))
        assertFalse(enumSet.remove(TestEnum.A))

        assertTrue(enumSet.retainAll(setOf(TestEnum.C)))
        assertEquals("{C}", enumSet.toString())
        assertTrue(enumSet.addAll(enumSetOf(TestEnum.D, TestEnum.A)))
        assertEquals("{A, C, D}", enumSet.toString())

        enumSet.clear()
        assertTrue(enumSet.isEmpty())

        assertTrue(enumSet.addAll(setOf(TestEnum.A, TestEnum.B, TestEnum.C)))
        assertEquals("{A, B, C}", enumSet.toString())
        assertFalse(enumSet.add(TestEnum.A))

        assertTrue(enumSet.removeAll(setOf(TestEnum.A, TestEnum.B)))
        assertEquals("{C}", enumSet.toString())

        assertTrue(enumSet.containsAll(setOf(TestEnum.C)))
        assertFalse(enumSet.containsAll(setOf(TestEnum.A, TestEnum.B)))

        assertTrue(enumSet.contains(TestEnum.C))

        val iter = enumSet.iterator()
        assertTrue(iter.hasNext())
        assertEquals(TestEnum.C, iter.next())
        assertFalse(iter.hasNext())
        iter.remove()

        assertTrue(enumSet.isEmpty())

        val allOf = enumSetAllOf<TestEnum>()
        assertEquals(4, allOf.size)
        assertEquals(TestEnum.entries.toSet(), allOf)
        assertEquals(allOf, TestEnum.entries.toSet())

        val allOf2 = mutableEnumSetAllOf<TestEnum>()
        assertEquals(allOf, allOf2)
    }

    @Test
    fun testJumboMutable() {
        val largeEnumSet = mutableEnumSetOf(TestLargeEnum._1, TestLargeEnum._2, TestLargeEnum._3)
        assertTrue(largeEnumSet.remove(TestLargeEnum._1))
        assertFalse(largeEnumSet.remove(TestLargeEnum._1))

        assertEquals("{_2, _3}", largeEnumSet.toString())

        assertTrue(largeEnumSet.retainAll(setOf(TestLargeEnum._3)))
        assertEquals("{_3}", largeEnumSet.toString())
        assertTrue(largeEnumSet.addAll(enumSetOf(TestLargeEnum._4, TestLargeEnum._1)))
        assertEquals("{_1, _3, _4}", largeEnumSet.toString())

        largeEnumSet.clear()
        assertTrue(largeEnumSet.isEmpty())

        assertTrue(largeEnumSet.addAll(setOf(TestLargeEnum._1, TestLargeEnum._2, TestLargeEnum._3)))
        assertEquals("{_1, _2, _3}", largeEnumSet.toString())
        assertFalse(largeEnumSet.add(TestLargeEnum._1))

        assertTrue(largeEnumSet.removeAll(setOf(TestLargeEnum._1, TestLargeEnum._2)))
        assertEquals("{_3}", largeEnumSet.toString())

        assertTrue(largeEnumSet.containsAll(setOf(TestLargeEnum._3)))
        assertFalse(largeEnumSet.containsAll(setOf(TestLargeEnum._1, TestLargeEnum._2)))

        assertTrue(largeEnumSet.contains(TestLargeEnum._3))

        val iter = largeEnumSet.iterator()
        assertTrue(iter.hasNext())
        assertEquals(TestLargeEnum._3, iter.next())
        assertFalse(iter.hasNext())
        iter.remove()

        assertTrue(largeEnumSet.isEmpty())

        val allOf = enumSetAllOf<TestLargeEnum>()
        assertEquals(80, allOf.size)
        assertEquals(TestLargeEnum.entries.toSet(), allOf.toSet())
        assertEquals(allOf.toSet(), TestLargeEnum.entries.toSet())

        val allOf2 = mutableEnumSetAllOf<TestLargeEnum>()
        assertEquals(allOf, allOf2)
    }
}