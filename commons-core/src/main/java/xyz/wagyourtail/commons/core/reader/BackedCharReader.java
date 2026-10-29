package xyz.wagyourtail.commons.core.reader;

/**
 * A {@link CharReader} whose position indexes a stable backing buffer —
 * enables zero-copy range views: a grammar can record the position before and
 * after reading a node and take the {@code (buffer, start, end)} slice
 * without copying.
 *
 * Implemented by {@link StringCharReader} and
 * {@link CharReader.BufferedCharReader} (including its copy readers).
 * Sequential stream readers are not backed.
 */
public interface BackedCharReader {

    /**
     * the shared backing buffer that positions index into. May grow
     * (StringBuilder on buffered readers); already-consumed ranges are stable.
     */
    CharSequence getBackingBuffer();

    /**
     * current absolute index into {@link #getBackingBuffer()}.
     */
    int getBackingPosition();

}
