package xyz.wagyourtail.commons.reader.test;

import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.core.reader.StringCharReader;

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
        assertEquals(null, reader.takeNextLiteral());
        reader.expect('\n');
        assertEquals("test2", reader.takeNextLiteral());
    }

    public void testTakeString() {
        var reader = new StringCharReader("\"test\"");
        assertEquals("test", reader.takeString());
    }

    @Test
    public void testTakeStringWithEscapes() {
        var reader = new StringCharReader("\"te\\\"st\"");
        assertEquals("te\"st", reader.takeString());
    }

    @Test
    public void testTakeStringWithEscapes2() {
        var reader = new StringCharReader("\"te\\\\st\"");
        assertEquals("te\\st", reader.takeString());
    }

    @Test
    public void testCSV() {
        var reader = new StringCharReader("test,test2,test3\ntest4");
        assertEquals("test", reader.takeCol());
        assertEquals("test2", reader.takeCol());
        assertEquals("test3", reader.takeCol());
        assertEquals(null, reader.takeCol());
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
        assertEquals(null, reader.takeCol());
        reader.expect('\n');
        assertEquals("test4", reader.takeCol());
    }

    @Test
    public void testCSVTakeRemaining() {
        var reader = new StringCharReader("test,test2,test3\ntest4");
        assertEquals("test", reader.takeCol());
        assertEquals(List.of("test2", "test3"), reader.takeRemainingCol());
        assertEquals(null, reader.takeCol());
        reader.expect('\n');
        assertEquals("test4", reader.takeCol());
    }

}