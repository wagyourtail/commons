package xyz.wagyourtail.commonskt.reader

/**
 * A [ByteReader] whose position indexes a stable backing buffer — enables
 * zero-copy range views: a grammar can record the position before and after
 * reading a node and take the `(buffer, start, end)` slice without copying.
 *
 * Implemented by [ByteArrayByteReader] and [ByteReader.BufferedByteReader]
 * (including its copy readers). Sequential stream readers are not backed.
 */
interface BackedByteReader {

    /**
     * the shared backing buffer that positions index into. May be replaced
     * with a larger array as the buffer grows; ranges captured earlier remain
     * valid because the array contents at those indices were already written.
     */
    val backingBuffer: ByteArray

    /**
     * current absolute index into [backingBuffer].
     */
    val backingPosition: Int

}
