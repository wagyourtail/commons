package xyz.wagyourtail.commons.data;

import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.core.data.SeekableInMemoryByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeekableInMemoryByteChannelTest {

    private final byte[] testData = "Some data".getBytes(StandardCharsets.UTF_8);

    @Test
    public void testReadingFromAPositionAfterEndReturnsEOF() throws Exception {
        try (SeekableByteChannel c = new SeekableInMemoryByteChannel(0)) {
            c.position(2);
            assertEquals(2, c.position());
            final ByteBuffer readBuffer = ByteBuffer.allocate(4);
            assertEquals(-1, c.read(readBuffer));
        }
    }


    @Test
    public void testShouldReadContentsProperly() throws IOException {
        try (SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            final ByteBuffer readBuffer = ByteBuffer.allocate(testData.length);
            final int readCount = c.read(readBuffer);
            assertEquals(testData.length, readCount);
            assertArrayEquals(testData, readBuffer.array());
            assertEquals(testData.length, c.position());
        }
    }


    @Test
    public void testShouldReadContentsWhenBiggerBufferSupplied() throws IOException {
        try (SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            final ByteBuffer readBuffer = ByteBuffer.allocate(testData.length + 1);
            final int readCount = c.read(readBuffer);
            assertEquals(testData.length, readCount);
            assertArrayEquals(testData, Arrays.copyOf(readBuffer.array(), testData.length));
            assertEquals(testData.length, c.position());
        }
    }

    @Test
    public void testShouldReadDataFromSetPosition() throws IOException {
        try (SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            final ByteBuffer readBuffer = ByteBuffer.allocate(4);
            c.position(5L);
            final int readCount = c.read(readBuffer);
            assertEquals(4L, readCount);
            assertEquals("data", new String(readBuffer.array(), StandardCharsets.UTF_8));
            assertEquals(testData.length, c.position());
        }
    }

    @Test
    public void testShouldSetProperPosition() throws IOException {
        try (SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            final long posAtFour = c.position(4L).position();
            final long posAtTheEnd = c.position(testData.length).position();
            final long posPastTheEnd = c.position(testData.length + 1L).position();
            assertEquals(4L, posAtFour);
            assertEquals(c.size(), posAtTheEnd);
            assertEquals(testData.length + 1L, posPastTheEnd);
        }
    }

    @Test
    public void testShouldSetProperPositionOnTruncate() throws IOException {
        try (SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            c.position(testData.length);
            c.truncate(4L);
            assertEquals(4L, c.position());
            assertEquals(4L, c.size());
        }
    }

    @Test
    public void testShouldSignalEOFWhenPositionAtTheEnd() throws IOException {
        try (SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            final ByteBuffer readBuffer = ByteBuffer.allocate(testData.length);
            c.position(testData.length + 1);
            final int readCount = c.read(readBuffer);
            assertEquals(0L, readBuffer.position());
            assertEquals(-1, readCount);
            assertEquals(-1, c.read(readBuffer));
        }
    }

    @Test
    public void testShouldTruncateContentsProperly() throws IOException {
        try (SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            c.truncate(4);
            final byte[] bytes = Arrays.copyOf(c.getBuffer(), (int) c.size());
            assertEquals("Some", new String(bytes, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testTruncateMovesPositionWhenNewSizeIsBiggerThanSizeAndPositionIsEvenBigger() throws Exception {
        try (SeekableByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            c.position(2 * testData.length);
            c.truncate(testData.length + 1);
            assertEquals(testData.length, c.size());
            assertEquals(testData.length + 1, c.position());
        }
    }

    @Test
    public void testTruncateMovesPositionWhenNotResizingButPositionBiggerThanSize() throws Exception {
        try (SeekableByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            c.position(2 * testData.length);
            c.truncate(testData.length);
            assertEquals(testData.length, c.size());
            assertEquals(testData.length, c.position());
        }
    }


    @Test
    public void testTruncateMovesPositionWhenShrinkingBeyondPosition() throws Exception {
        try (SeekableByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            c.position(4);
            c.truncate(3);
            assertEquals(3, c.size());
            assertEquals(3, c.position());
        }
    }


    @Test
    public void testTruncateToBiggerSizeDoesntChangeAnything() throws Exception {
        try (SeekableByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            assertEquals(testData.length, c.size());
            c.truncate(testData.length + 1);
            assertEquals(testData.length, c.size());
            final ByteBuffer readBuffer = ByteBuffer.allocate(testData.length);
            assertEquals(testData.length, c.read(readBuffer));
            assertArrayEquals(testData, Arrays.copyOf(readBuffer.array(), testData.length));
        }
    }


    @Test
    public void testTruncateToCurrentSizeDoesntChangeAnything() throws Exception {
        try (SeekableByteChannel c = new SeekableInMemoryByteChannel(testData)) {
            assertEquals(testData.length, c.size());
            c.truncate(testData.length);
            assertEquals(testData.length, c.size());
            final ByteBuffer readBuffer = ByteBuffer.allocate(testData.length);
            assertEquals(testData.length, c.read(readBuffer));
            assertArrayEquals(testData, Arrays.copyOf(readBuffer.array(), testData.length));
        }
    }

    @Test
    public void writingToAPositionAfterEndGrowsChannel() throws Exception {
        try (SeekableByteChannel c = new SeekableInMemoryByteChannel(0)) {
            c.position(2);
            assertEquals(2, c.position());
            final ByteBuffer inData = ByteBuffer.wrap(testData);
            assertEquals(testData.length, c.write(inData));
            assertEquals(testData.length + 2, c.size());

            c.position(2);
            final ByteBuffer readBuffer = ByteBuffer.allocate(testData.length);
            c.read(readBuffer);
            assertArrayEquals(testData, Arrays.copyOf(readBuffer.array(), testData.length));
        }
    }


}
