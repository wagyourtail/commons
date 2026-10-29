package xyz.wagyourtail.commonskt.reader

import okio.Buffer
import okio.BufferedSource
import okio.EOFException

/**
 * Adapt an okio [BufferedSource] to a [ByteReader].
 *
 * The returned reader is a [ByteReader.BufferedByteReader] over the source, so
 * mark/reset/[ByteReader.copy] all work — reads are buffered as they happen,
 * letting copies and resets replay already-consumed bytes while the source
 * continues to stream underneath.
 */
fun BufferedSource.asByteReader(): ByteReader<*> =
    ByteReader.BufferedByteReader(SourceByteReader(this), Int.MAX_VALUE)

/**
 * Adapt an okio [Buffer] to a [ByteReader]; see [asByteReader].
 */
fun Buffer.asByteReader(): ByteReader<*> = (this as BufferedSource).asByteReader()

/**
 * Adapt a [ByteReader] to an okio [BufferedSource].
 *
 * NOTE: this drains the reader eagerly into a [Buffer].
 */
fun ByteReader<*>.toBufferedSource(): BufferedSource = Buffer().write(takeRemaining())

/**
 * Sequential [ByteReader] over an okio [BufferedSource]. Supports bounded
 * backtracking via [mark]/[reset] — [mark] records up to `limit` consumed
 * bytes for replay on [reset] (java.io-style read-ahead limit semantics:
 * exceeding it invalidates the mark, as does [skip]). [copy] is unsupported —
 * wrap in [ByteReader.BufferedByteReader] (or use [asByteReader]) for full
 * multi-copy/speculative parsing.
 */
private class SourceByteReader(private val source: BufferedSource) : ByteReader<SourceByteReader>() {

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
        return try {
            source.peek().readByte().toInt() and 0xFF
        } catch (_: EOFException) {
            null
        }
    }

    override fun take(): Int? {
        if (serve != null && serveIndex < serveLength) {
            val b = serve!![serveIndex++].toInt() and 0xFF
            position++
            return b
        }
        serve = null
        val b = try {
            source.readByte().toInt() and 0xFF
        } catch (_: EOFException) {
            return null
        }
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
        // drain via take() — BufferedSource.skip consumes an unknown partial
        // amount on EOF
        var skipped = 0
        while (skipped < count && take() != null) skipped++
        // skipped bytes aren't recorded — the mark can no longer be replayed
        markedPosition = -1
        return skipped
    }

    override fun copy(limit: Int): ByteReader<*> =
        throw UnsupportedOperationException("source readers cannot be copied; wrap in BufferedByteReader first")

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

}
