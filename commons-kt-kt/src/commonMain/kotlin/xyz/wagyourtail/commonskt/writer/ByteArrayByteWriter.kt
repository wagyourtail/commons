package xyz.wagyourtail.commonskt.writer

import kotlin.math.min

/**
 * [ByteWriter] into a growable in-memory `ByteArray`, with random-access
 * [seek]/overwrite and a [toByteArray] snapshot — the byte analog of a
 * `StringBuilder`.
 */
class ByteArrayByteWriter(initialCapacity: Int = 64) : ByteWriter() {

    private var buffer = ByteArray(initialCapacity)

    override var position: Int = 0
        private set

    private fun ensure(capacity: Int) {
        if (capacity > buffer.size) {
            buffer = buffer.copyOf(maxOf(capacity, buffer.size * 2))
        }
    }

    override fun put(b: Int) {
        ensure(position + 1)
        buffer[position++] = b.toByte()
    }

    override fun put(bytes: ByteArray) {
        ensure(position + bytes.size)
        bytes.copyInto(buffer, position)
        position += bytes.size
    }

    override fun put(bytes: ByteArray, offset: Int, length: Int) {
        ensure(position + length)
        bytes.copyInto(buffer, position, offset, offset + length)
        position += length
    }

    /**
     * jump to an absolute position (growing with zero-fill if beyond the end).
     */
    fun seek(pos: Int) {
        require(pos >= 0) { "seek position $pos < 0" }
        if (pos > position) {
            ensure(pos)
        }
        position = pos
    }

    /**
     * overwrite a byte at an already-written position.
     */
    fun overwrite(pos: Int, b: Int) {
        require(pos in 0 until position) { "overwrite position $pos out of range 0..$position" }
        buffer[pos] = b.toByte()
    }

    /**
     * overwrite bytes at an already-written position — for backpatching
     * length-prefixed structures (write placeholder, fill after content).
     */
    fun overwrite(pos: Int, bytes: ByteArray) {
        require(pos in 0..position && pos + bytes.size <= position) {
            "overwrite range $pos..${pos + bytes.size} out of range 0..$position"
        }
        bytes.copyInto(buffer, pos)
    }

    /**
     * bytes written so far (a copy).
     */
    fun toByteArray(): ByteArray = buffer.copyOf(min(position, buffer.size))

}
