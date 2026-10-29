package xyz.wagyourtail.commonskt.reader

import java.io.InputStream
import java.io.PushbackInputStream

/**
 * [ByteReader] over a [java.io.InputStream] — the byte analog of commons-core's
 * `ReaderCharReader`. Supports bounded backtracking via [mark]/[reset] —
 * [mark] records up to `limit` consumed bytes for replay on [reset]
 * (java.io-style read-ahead limit semantics: exceeding it invalidates the
 * mark, as does [skip]). [copy] is unsupported — use [asByteReader] /
 * [ByteReader.BufferedByteReader] for full multi-copy/speculative parsing.
 */
fun InputStream.asByteReader(): ByteReader<*> =
    ByteReader.BufferedByteReader(InputStreamByteReader(this), Int.MAX_VALUE)

class InputStreamByteReader(stream: InputStream) : ByteReader<InputStreamByteReader>(), AutoCloseable {

    private val stream =
        if (stream is PushbackInputStream) stream else PushbackInputStream(stream, 1)

    override var position: Int = 0
        private set

    private var markedPosition = -1
    private var markLimit = 0
    private var record = ByteArray(64)
    private var recordLength = 0
    private var serve: ByteArray? = null
    private var serveIndex = 0
    private var serveLength = 0

    override fun peek(): Int? {
        if (serve != null && serveIndex < serveLength) {
            return serve!![serveIndex].toInt() and 0xFF
        }
        val b = stream.read()
        if (b == -1) return null
        stream.unread(b)
        return b
    }

    override fun take(): Int? {
        if (serve != null && serveIndex < serveLength) {
            val b = serve!![serveIndex++].toInt() and 0xFF
            position++
            return b
        }
        serve = null
        val b = stream.read()
        if (b == -1) return null
        if (markedPosition >= 0) {
            if (recordLength >= markLimit) {
                // read-ahead limit exceeded — mark invalidated
                markedPosition = -1
                recordLength = 0
            } else {
                if (recordLength == record.size) {
                    record = record.copyOf(maxOf(64, record.size * 2))
                }
                record[recordLength++] = b.toByte()
            }
        }
        position++
        return b
    }

    override fun skip(count: Int): Int {
        var skipped = 0
        if (serve != null) {
            val n = minOf(count, serveLength - serveIndex)
            serveIndex += n
            skipped += n
            position += n
        }
        if (skipped < count) {
            val n = stream.skip((count - skipped).toLong()).toInt()
            skipped += n
            position += n
        }
        // skipped bytes aren't recorded — the mark can no longer be replayed
        markedPosition = -1
        return skipped
    }

    /**
     * @return a buffering wrapper enabling full mark/reset/copy/parse support
     * over this stream.
     */
    fun buffered(limit: Int = Int.MAX_VALUE): ByteReader.BufferedByteReader =
        ByteReader.BufferedByteReader(this, limit)

    override fun copy(limit: Int): ByteReader<*> =
        throw UnsupportedOperationException("stream readers cannot be copied; wrap in BufferedByteReader first")

    override fun mark(limit: Int) {
        markedPosition = position
        markLimit = limit
        if (serve != null && serveIndex < serveLength) {
            // pending replay bytes are the first bytes at the new mark —
            // move them into the record so reset() replays them correctly
            val pending = serve!!.copyOfRange(serveIndex, serveLength)
            record = ByteArray(maxOf(pending.size, minOf(limit, 64).coerceAtLeast(1)))
            pending.copyInto(record)
            recordLength = pending.size
            serve = record
            serveIndex = 0
            serveLength = pending.size
        } else {
            record = ByteArray(minOf(limit, 64).coerceAtLeast(1))
            recordLength = 0
        }
    }

    override fun reset() {
        if (markedPosition < 0) {
            throw IllegalStateException("no active mark (unset, or read-ahead limit exceeded)")
        }
        serve = record
        serveIndex = 0
        serveLength = recordLength
        position = markedPosition
    }

    override fun close() {
        stream.close()
    }

}
