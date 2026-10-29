package xyz.wagyourtail.commons.reader.test;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.ReaderCharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestCharReader {

    @Test
    public void test() {
        var reader = new StringCharReader("test");
        assertEquals('t', reader.peek());
        assertEquals('t', reader.take());
        assertEquals('e', reader.peek());
        assertEquals('e', reader.take());
        assertEquals('s', reader.peek());
        assertEquals('s', reader.take());
        assertEquals('t', reader.peek());
        assertEquals('t', reader.take());
        assertEquals(-1, reader.peek());
        assertEquals(-1, reader.take());
    }

    @Test
    public void testTakeUntil() {
        var reader = new StringCharReader("test");
        assertEquals("test", reader.takeUntil(e -> false));

        var reader2 = new StringCharReader("test");
        assertEquals("t", reader2.takeUntil(e -> e == 'e'));
        assertEquals("est", reader2.takeUntil(e -> false));

        var reader3 = new StringCharReader("abcdabcabcdeabcdefabcdefg");
        assertEquals("abcdabcabcde", reader3.takeUntil("abcdef"));
        assertEquals("abcdefabcdefg", reader3.takeRemaining());

        var reader4 = new StringCharReader("abaacabcababababcd");
        assertEquals("abaacabcabab", reader4.takeUntil("ababc"));
        assertEquals("ababcd", reader4.takeRemaining());
    }

    @Test
    public void testTakeWhile() {
        var reader = new StringCharReader("test");
        assertEquals("t", reader.takeWhile(e -> e == 't'));
        assertEquals("es", reader.takeWhile(e -> e != 't'));
        assertEquals("t", reader.takeWhile(e -> e == 't'));
    }

    @Test
    public void testTakeWhitespace() {
        var reader = new StringCharReader("  test");
        assertEquals("  ", reader.takeWhitespace());
        assertEquals("test", reader.takeRemaining());
    }

    @Test
    public void testTakeLine() {
        var reader = new StringCharReader("test\n");
        assertEquals("test", reader.takeLine());
    }

    @Test
    public void testTakeRemaining() {
        var reader = new StringCharReader("test\ntest2");
        assertEquals("test\ntest2", reader.takeRemaining());
    }

    @Test
    public void testTakeNext() {
        var reader = new StringCharReader("test test2");
        assertEquals("test", reader.takeNext());
        assertEquals("test2", reader.takeNext());
    }

    @Test
    public void testTakeNextWithStringLiteral() {
        var reader = new StringCharReader("\"test\" test2");
        assertEquals("test", reader.takeNext());
        assertEquals("test2", reader.takeNext());
    }

    @Test
    public void testTakeNextWithWhitespace() {
        var reader = new StringCharReader(" test test2");
        assertEquals("", reader.takeNext());
        assertEquals("test", reader.takeNext());
        assertEquals("test2", reader.takeNext());
    }

    @Test
    public void testTakeNextWithNewline() {
        var reader = new StringCharReader("test\ntest2");
        assertEquals("test", reader.takeNext());
        assertNull(reader.takeNext());
        reader.expect('\n');
        assertEquals("test2", reader.takeNext());
    }

    @Test
    public void testTakeNextLiteral() {
        var reader = new StringCharReader("test\ttest2");
        assertEquals("test", reader.takeNextLiteral());
        assertEquals("test2", reader.takeNextLiteral());
    }

    @Test
    public void testTakeNextLiteralWithNewline() {
        var reader = new StringCharReader("test\ntest2");
        assertEquals("test", reader.takeNextLiteral());
        assertNull(reader.takeNextLiteral());
        reader.expect('\n');
        assertEquals("test2", reader.takeNextLiteral());
    }

    @Test
    public void testTakeString() {
        var reader = new StringCharReader("\"test\"");
        assertEquals("test", reader.takeString());
    }

    @Test
    public void testTakeStringWithEscapes() {
        var reader = new StringCharReader("\"te\\\"st\"");
        assertEquals("te\"st", reader.takeString());

        var reader4 = new StringCharReader("\"te\\\\st\"");
        assertEquals("te\\st", reader4.takeString());

        var reader2 = new StringCharReader("\"te\"\"st\"");
        assertEquals("te\"st", reader2.takeString(StringCharReader.TAKE_STRING_ESCAPE_DOUBLE_QUOTE, "\""));

        var reader3 = new StringCharReader("\"\"\"test\nmultiline\"\"\"");
        assertEquals("test\nmultiline", reader3.takeString(StringCharReader.TAKE_STRING_MULTILINE, "\"\"\""));

        var reader5 = new StringCharReader("\"\"\"test\nmultiline\\\"\"\"string\"\"\"");
        assertEquals("test\nmultiline\"\"\"string", reader5.takeString(StringCharReader.TAKE_STRING_MULTILINE, "\"\"\""));

        var reader6 = new StringCharReader("\"\"\"test\nmultiline\\\"\"\"string\"\"\"");
        assertEquals("\"\"\"test\nmultiline\\\"\"\"string\"\"\"", reader6.takeString(StringCharReader.TAKE_STRING_MULTILINE | StringCharReader.TAKE_STRING_NO_TRANSLATE_ESCAPES, "\"\"\""));
    }

    @Test
    public void testCSV() {
        var reader = new StringCharReader("test,test2,test3\ntest4");
        assertEquals("test", reader.takeCol());
        assertEquals("test2", reader.takeCol());
        assertEquals("test3", reader.takeCol());
        assertNull(reader.takeCol());
        reader.expect('\n');
        assertEquals("test4", reader.takeCol());
    }

    @Test
    public void testCSVWithQuotes() {
        var reader = new StringCharReader("\"test\",\"test2\",\"test3\"\n\"test4\"");
        assertEquals("test", reader.takeCol());
        assertEquals("test2", reader.takeCol());
        assertEquals("test3", reader.takeCol());
        assertNull(reader.takeCol());
        reader.expect('\n');
        assertEquals("test4", reader.takeCol());
    }

    @Test
    public void testCSVWithQuotesAndCommas() {
        var reader = new StringCharReader("\"test,test\",\"test2\",\"test3\"\n\"test4\"");
        assertEquals("test,test", reader.takeCol());
        assertEquals("test2", reader.takeCol());
        assertEquals("test3", reader.takeCol());
        assertNull(reader.takeCol());
        reader.expect('\n');
        assertEquals("test4", reader.takeCol());
    }

    @Test
    public void testCSVTakeRemaining() {
        var reader = new StringCharReader("test,test2,test3\ntest4");
        assertEquals("test", reader.takeCol());
        assertEquals(List.of("test2", "test3"), reader.takeRemainingCol());
        assertNull(reader.takeCol());
        reader.expect('\n');
        assertEquals("test4", reader.takeCol());
    }

    @Test
    public void testCopyWithLimit() {
        var reader = new StringCharReader("abcdefghijkl");
        assertEquals('a', reader.take());
        var copy = reader.copy(5);
        assertEquals("bcdef", copy.takeRemaining());
        copy.reset();
        assertEquals("bcd", copy.takeUntil('e'));
        assertEquals("ef", copy.takeUntil('h'));
        assertEquals("bcdefghijkl", reader.takeRemaining());
        assertEquals(-1, copy.take());
    }

    @Test
    public void testParse() {
        val reader = new StringCharReader("test");
        assertEquals("test", reader.parse(r -> r.expect("test")));
        assertEquals(-1, reader.take());
    }

    // --- concurrent copies / EOS / mark-transparency ---

    @Test
    public void testMultipleConcurrentCopies() {
        CharReader.BufferedCharReader base = new CharReader.BufferedCharReader(new StringCharReader("abcdefghij"), Integer.MAX_VALUE);
        CharReader<?> a = base.copy(Integer.MAX_VALUE);
        assertEquals("abcd", a.take(4));
        CharReader<?> b = base.copy(Integer.MAX_VALUE);
        assertEquals("ab", b.take(2));
        assertEquals("ef", a.take(2));
        assertEquals('c', (char) b.take());
        assertEquals("abcd", base.take(4));
        assertEquals("ghij", a.takeRemaining());
        assertEquals("defghij", b.takeRemaining());
        assertEquals("efghij", base.takeRemaining());
    }

    @Test
    public void testTakeAtEOSIsStable() {
        CharReader.BufferedCharReader reader = new CharReader.BufferedCharReader(new StringCharReader("ab"), Integer.MAX_VALUE);
        CharReader<?> copy = reader.copy(Integer.MAX_VALUE);
        assertEquals("ab", copy.takeRemaining());
        assertEquals(-1, copy.take());
        assertEquals(-1, copy.take());
        CharReader<?> c2 = reader.copy(Integer.MAX_VALUE);
        assertEquals("ab", c2.takeRemaining());
        assertEquals(-1, c2.take());
        assertEquals(-1, c2.take());
        assertEquals("ab", reader.takeRemaining());
        assertEquals(-1, reader.take());
        assertEquals(-1, reader.take());
    }

    @Test
    public void testCopyLimitRespected() {
        CharReader.BufferedCharReader reader = new CharReader.BufferedCharReader(new StringCharReader("abcdef"), Integer.MAX_VALUE);
        CharReader<?> copy = reader.copy(2);
        assertEquals('a', (char) copy.take());
        assertEquals('b', (char) copy.take());
        assertEquals(-1, copy.take());
        assertEquals(-1, copy.take());
        assertEquals(-1, copy.peek());
        assertEquals("abcdef", reader.takeRemaining());
    }

    @Test
    public void testNestedCopies() {
        CharReader.BufferedCharReader reader = new CharReader.BufferedCharReader(new StringCharReader("aabbcc"), Integer.MAX_VALUE);
        CharReader<?> outer = reader.copy(4);
        assertEquals('a', (char) outer.take());
        CharReader<?> inner = outer.copy(2);
        assertEquals("ab", inner.takeRemaining());
        assertEquals(-1, inner.take());
        assertEquals("abb", outer.takeRemaining());
        assertEquals(-1, outer.take());
        assertEquals("aabbcc", reader.takeRemaining());
    }

    @Test
    public void testMarkSurvivesParse() {
        StringCharReader reader = new StringCharReader("hello world");
        reader.take(2);
        reader.mark();
        reader.take(4);
        assertEquals("ok", reader.parse(r -> {
            r.expect("world");
            return "ok";
        }));
        reader.reset();
        assertEquals("llo world", reader.takeRemaining());
    }

    @Test
    public void testStreamMarkReset() throws IOException {
        // bounded replay mark/reset on a raw stream reader
        ReaderCharReader reader = new ReaderCharReader(new StringReader("abcdef"));
        assertEquals('a', (char) reader.take());
        assertEquals('b', (char) reader.take());
        reader.mark(3);
        assertEquals('c', (char) reader.take());
        assertEquals('d', (char) reader.take());
        reader.reset();
        assertEquals('c', (char) reader.take());
        assertEquals('d', (char) reader.take());
        assertEquals('e', (char) reader.take());
        // reset again — mark still active
        reader.reset();
        assertEquals("cde", reader.take(3));
        // exceed the readlimit → mark invalidated
        reader.reset();
        reader.take(4); // c,d,e,f — recording 'f' exceeds limit 3
        try {
            reader.reset();
            org.junit.jupiter.api.Assertions.fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }
}
