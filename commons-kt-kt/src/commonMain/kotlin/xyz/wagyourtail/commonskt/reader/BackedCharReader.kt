package xyz.wagyourtail.commonskt.reader

/**
 * A [CharReader] whose position indexes a stable backing buffer — enables
 * zero-copy range views: a grammar can record the position before and after
 * reading a node and take the `(buffer, start, end)` slice without copying.
 *
 * Implemented by [StringCharReader] and [CharReader.BufferedCharReader]
 * (including its copy readers). Sequential stream readers are not backed.
 */
interface BackedCharReader {

    /**
     * the shared backing buffer that positions index into. May grow
     * (StringBuilder on buffered readers); already-consumed ranges are stable.
     */
    val backingBuffer: CharSequence

    /**
     * current absolute index into [backingBuffer].
     */
    val backingPosition: Int

}
