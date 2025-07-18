package xyz.wagyourtail.commonskt.test.reader

import xyz.wagyourtail.commonskt.reader.StringCharReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.expect


class TestCharReader {

    @Test
    fun test() {
        val reader = StringCharReader("test")
        assertEquals('t', reader.peek())
        assertEquals('t', reader.take())
        assertEquals('e', reader.peek())
        assertEquals('e', reader.take())
        assertEquals('s', reader.peek())
        assertEquals('s', reader.take())
        assertEquals('t', reader.peek())
        assertEquals('t', reader.take())
        assertEquals(null, reader.peek())
        assertEquals(null, reader.take())
    }

    @Test
    fun testTakeUntil() {
        val reader = StringCharReader("test")
        assertEquals("test", reader.takeUntil { false })

        val reader2 = StringCharReader("test")
        assertEquals("t", reader2.takeUntil { it == 'e' })
        assertEquals("est", reader2.takeUntil { false })

        val reader3 = StringCharReader("abcdabcabcdeabcdefabcdefg")
        assertEquals("abcdabcabcde", reader3.takeUntil("abcdef"))
        assertEquals("abcdefabcdefg", reader3.takeRemaining())

        val reader4 = StringCharReader("abaacabcababababcd")
        assertEquals("abaacabcabab", reader4.takeUntil("ababc"))
        assertEquals("ababcd", reader4.takeRemaining())
    }

    @Test
    fun testTakeWhile() {
        val reader = StringCharReader("test")
        assertEquals("t", reader.takeWhile { it == 't' })
        assertEquals("es", reader.takeWhile { it != 't' })
        assertEquals("t", reader.takeWhile { it == 't' })
    }

    @Test
    fun testTakeWhitespace() {
        val reader = StringCharReader("  test")
        assertEquals("  ", reader.takeWhitespace())
        assertEquals("test", reader.takeUntil { false })
    }

    @Test
    fun testTakeLine() {
        val reader = StringCharReader("test\n")
        assertEquals("test", reader.takeLine())
    }

    @Test
    fun testTakeRemaining() {
        val reader = StringCharReader("test\ntest2")
        assertEquals("test\ntest2", reader.takeRemaining())
    }

    @Test
    fun testTakeNext() {
        val reader = StringCharReader("test test2")
        assertEquals("test", reader.takeNext())
        assertEquals("test2", reader.takeNext())
    }

    @Test
    fun testTakeNextWithStringLiteral() {
        val reader = StringCharReader("\"test\" test2")
        assertEquals("test", reader.takeNext())
        assertEquals("test2", reader.takeNext())
    }

    @Test
    fun testTakeNextWithWhitespace() {
        val reader = StringCharReader(" test test2")
        assertEquals("", reader.takeNext())
        assertEquals("test", reader.takeNext())
        assertEquals("test2", reader.takeNext())
    }

    @Test
    fun testTakeNextWithNewline() {
        val reader = StringCharReader("test\ntest2")
        assertEquals("test", reader.takeNext())
        assertEquals(null, reader.takeNext())
        reader.expect('\n')
        assertEquals("test2", reader.takeNext())
    }

    @Test
    fun testTakeNextLiteral() {
        val reader = StringCharReader("test\ttest2")
        assertEquals("test", reader.takeNextLiteral())
        assertEquals("test2", reader.takeNextLiteral())
    }

    @Test
    fun testTakeNextLiteralWithNewline() {
        val reader = StringCharReader("test\ntest2")
        assertEquals("test", reader.takeNextLiteral())
        assertEquals(null, reader.takeNextLiteral())
        reader.expect('\n')
        assertEquals("test2", reader.takeNextLiteral())
    }

    @Test
    fun testTakeString() {
        val reader = StringCharReader("\"test\"")
        assertEquals("test", reader.takeString())
    }

    @Test
    fun testTakeStringWithEscapes() {
        val reader = StringCharReader("\"te\\\"st\"")
        assertEquals("te\"st", reader.takeString())

        val reader4 = StringCharReader("\"te\\\\st\"")
        assertEquals("te\\st", reader4.takeString())

        val reader2 = StringCharReader("\"te\"\"st\"")
        assertEquals("te\"st", reader2.takeString(escapeDoubleQuote = true, quote = "\""))

        val reader3 = StringCharReader("\"\"\"test\nmultiline\"\"\"")
        assertEquals("test\nmultiline", reader3.takeString(multiline = true, quote = "\"\"\""))

        val reader5 = StringCharReader("\"\"\"test\nmultiline\\\"\"\"string\"\"\"")
        assertEquals("test\nmultiline\"\"\"string", reader5.takeString(multiline = true, quote = "\"\"\""))

        val reader6 = StringCharReader("\"\"\"test\nmultiline\\\"\"\"string\"\"\"")
        assertEquals("\"\"\"test\nmultiline\\\"\"\"string\"\"\"", reader6.takeString(multiline = true, noTranslateEscapes =  true, quote = "\"\"\""))
    }

    @Test
    fun testCSV() {
        val reader = StringCharReader("test,test2,test3\ntest4")
        assertEquals("test", reader.takeCol())
        assertEquals("test2", reader.takeCol())
        assertEquals("test3", reader.takeCol())
        assertEquals(null, reader.takeCol())
        reader.expect('\n')
        assertEquals("test4", reader.takeCol())
    }

    @Test
    fun testCSVWithQuotes() {
        val reader = StringCharReader("\"test\",\"test2\",\"test3\"\n\"test4\"")
        assertEquals("test", reader.takeCol())
        assertEquals("test2", reader.takeCol())
        assertEquals("test3", reader.takeCol())
        assertEquals(null, reader.takeCol())
        reader.expect('\n')
        assertEquals("test4", reader.takeCol())
    }

    @Test
    fun testCSVWithQuotesAndCommas() {
        val reader = StringCharReader("\"test,test\",\"test2\",\"test3\"\n\"test4\"")
        assertEquals("test,test", reader.takeCol())
        assertEquals("test2", reader.takeCol())
        assertEquals("test3", reader.takeCol())
        assertEquals(null, reader.takeCol())
        reader.expect('\n')
        assertEquals("test4", reader.takeCol())
    }

    @Test
    fun testCSVTakeRemaining() {
        val reader = StringCharReader("test,test2,test3\ntest4")
        assertEquals("test", reader.takeCol())
        assertEquals(listOf("test2", "test3"), reader.takeRemainingCol())
        assertEquals(null, reader.takeCol())
        reader.expect('\n')
        assertEquals("test4", reader.takeCol())
    }

    @Test
    fun testCopyWithLimit() {
        val reader = StringCharReader("abcdefghijkl")
        assertEquals('a', reader.take())
        val copy = reader.copy(5)
        assertEquals("bcdef", copy.takeRemaining())
        copy.reset()
        assertEquals("bcd", copy.takeUntil('e'))
        assertEquals("ef", copy.takeUntil('h'))
        assertEquals("bcdefghijkl", reader.takeRemaining())
        assertEquals(null, copy.take())
    }


    @Test
    fun testParse() {
        val reader = StringCharReader("test")
        assertEquals("test", reader.parse { expect("test") })
        assertNull(reader.take())

        reader.reset()
        assertEquals("test", reader.parse(
            { expect("false") },
            { expect("test") }
        ))
        reader.expectEOS()
    }
}