package xyz.wagyourtail.commonskt.writer

import okio.Buffer
import okio.BufferedSink

/**
 * Adapt an okio [BufferedSink] to a [ByteWriter].
 */
fun BufferedSink.asByteWriter(): ByteWriter = SinkByteWriter(this)

/**
 * Adapt an okio [Buffer] to a [ByteWriter]; see [asByteWriter].
 */
fun Buffer.asByteWriter(): ByteWriter = (this as BufferedSink).asByteWriter()

private class SinkByteWriter(private val sink: BufferedSink) : ByteWriter() {

    override var position: Int = 0
        private set

    override fun put(b: Int) {
        sink.writeByte(b)
        position++
    }

    override fun put(bytes: ByteArray) {
        sink.write(bytes)
        position += bytes.size
    }

    override fun put(bytes: ByteArray, offset: Int, length: Int) {
        sink.write(bytes, offset, length)
        position += length
    }

}
