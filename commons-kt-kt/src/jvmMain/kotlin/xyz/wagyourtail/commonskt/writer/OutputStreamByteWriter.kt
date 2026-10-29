package xyz.wagyourtail.commonskt.writer

import java.io.OutputStream

/**
 * [ByteWriter] over a [java.io.OutputStream] — sequential writes only.
 */
fun OutputStream.asByteWriter(): ByteWriter = OutputStreamByteWriter(this)

class OutputStreamByteWriter(private val stream: OutputStream) : ByteWriter(), AutoCloseable {

    override var position: Int = 0
        private set

    override fun put(b: Int) {
        stream.write(b)
        position++
    }

    override fun put(bytes: ByteArray) {
        stream.write(bytes)
        position += bytes.size
    }

    override fun put(bytes: ByteArray, offset: Int, length: Int) {
        stream.write(bytes, offset, length)
        position += length
    }

    override fun close() {
        stream.close()
    }

}
