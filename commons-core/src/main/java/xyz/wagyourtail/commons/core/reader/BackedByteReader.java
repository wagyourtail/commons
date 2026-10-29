package xyz.wagyourtail.commons.core.reader;

/**
 * A {@link ByteReader} whose position indexes a stable backing buffer —
 * enables zero-copy range views: a grammar can record the position before and
 * after reading a node and take the {@code (buffer, start, end)} slice
 * without copying.
 *
 * Implemented by {@link ByteArrayByteReader} and
 * {@link ByteReader.BufferedByteReader} (including its copy readers).
 * Sequential stream readers are not backed.
 */
public interface BackedByteReader {

    /**
     * the shared backing buffer that positions index into. May be replaced
     * with a larger array as the buffer grows; ranges captured earlier remain
     * valid because the array contents at those indices were already written.
     */
    byte[] getBackingBuffer();

    /**
     * current absolute index into {@link #getBackingBuffer()}.
     */
    int getBackingPosition();

}
