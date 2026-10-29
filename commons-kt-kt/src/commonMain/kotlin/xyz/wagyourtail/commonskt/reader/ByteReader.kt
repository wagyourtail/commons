package xyz.wagyourtail.commonskt.reader

import kotlin.math.min

/**
 * Byte-oriented counterpart to [CharReader]: a pull reader over a byte source
 * with mark/reset, limited sub-reader [copy], speculative [parse] alternatives,
 * and endian-aware numeric takes. Byte values are surfaced as `Int` in
 * `0..255`, with `null` signalling EOS — mirroring [CharReader]'s `Char?`.
 *
 * All multi-byte numeric takes default to **big-endian** (the byte order of
 * JVM-era formats like .class); `*LE` variants read little-endian.
 */
abstract class ByteReader<T : ByteReader<T>> {

    companion object {
        operator fun invoke(value: ByteArray) = ByteArrayByteReader(value)
    }

    /**
     * @return byte value 0..255, or null for eos
     */
    abstract fun peek(): Int?

    /**
     * @return byte value 0..255, or null for eos
     */
    abstract fun take(): Int?

    /**
     * current read position, in bytes consumed
     */
    abstract val position: Int

    /**
     * @return the number of bytes skipped
     */
    open fun skip(count: Int): Int {
        var skipped = 0
        while (skipped < count) {
            if (take() == null) break
            skipped++
        }
        return skipped
    }

    /**
     * @return the next count bytes (fewer if EOS is hit)
     */
    open fun take(count: Int): ByteArray {
        val out = ByteArray(count)
        var i = 0
        while (i < count) {
            out[i++] = (take() ?: break).toByte()
        }
        return if (i == count) out else out.copyOf(i)
    }

    /**
     * @return a copy of the reader at the current position, with its own
     * position/mark state; multiple copies may be active and interleaved
     * freely — reading a copy never moves the parent or sibling copies.
     * Sequential (stream) readers cannot copy; wrap them in
     * [BufferedByteReader] first.
     */
    abstract fun copy(limit: Int = Int.MAX_VALUE): ByteReader<*>

    /**
     * set this position as remembered.
     * @param limit maximum bytes that may be consumed before the mark becomes
     * invalid — advisory for buffer/array readers (always replayable), but
     * on sequential readers it bounds the recorded replay buffer like
     * java.io's read-ahead limit.
     */
    abstract fun mark(limit: Int = Int.MAX_VALUE)

    /**
     * reset to remembered mark position, or beginning if no mark.
     * On sequential readers this throws [IllegalStateException] when there is
     * no active mark (unset, or invalidated by exceeding the mark limit or by
     * [skip]).
     */
    abstract fun reset()

    fun exhausted() = peek() == null

    open fun takeRemaining(): ByteArray {
        val out = ArrayList<Byte>()
        while (peek() != null) {
            out.add(take()!!.toByte())
        }
        return out.toByteArray()
    }

    open fun takeUntil(byte: Int): ByteArray = takeUntil { it == (byte and 0xFF) }

    inline fun takeUntil(sep: (Int) -> Boolean): ByteArray {
        val out = ArrayList<Byte>()
        var next = peek()
        while (next != null && !sep(next)) {
            out.add(take()!!.toByte())
            next = peek()
        }
        return out.toByteArray()
    }

    inline fun takeWhile(acceptor: (Int) -> Boolean) = takeUntil { !acceptor(it) }

    /* numeric takes — big-endian */

    fun takeU1(): Int = take() ?: throw createException("Expected u1 but got EOS")

    fun takeU2(): Int = (takeU1() shl 8) or takeU1()

    fun takeU4(): UInt = takeI4().toUInt()

    fun takeU8(): ULong = takeI8().toULong()

    fun takeI1(): Byte = takeU1().toByte()

    fun takeI2(): Short = takeU2().toShort()

    fun takeI4(): Int = (takeU1() shl 24) or (takeU1() shl 16) or (takeU1() shl 8) or takeU1()

    fun takeI8(): Long = (takeI4().toLong() shl 32) or (takeI4().toLong() and 0xFFFFFFFFL)

    fun takeFloat(): Float = Float.fromBits(takeI4())

    fun takeDouble(): Double = Double.fromBits(takeI8())

    /* numeric takes — little-endian */

    fun takeU2LE(): Int = takeU1() or (takeU1() shl 8)

    fun takeU4LE(): UInt = takeI4LE().toUInt()

    fun takeU8LE(): ULong = takeI8LE().toULong()

    fun takeI2LE(): Short = takeU2LE().toShort()

    fun takeI4LE(): Int = takeU1() or (takeU1() shl 8) or (takeU1() shl 16) or (takeU1() shl 24)

    fun takeI8LE(): Long = (takeI4LE().toLong() and 0xFFFFFFFFL) or (takeI4LE().toLong() shl 32)

    fun takeFloatLE(): Float = Float.fromBits(takeI4LE())

    fun takeDoubleLE(): Double = Double.fromBits(takeI8LE())

    fun takeBytes(count: Int): ByteArray = take(count)

    fun takeUtf8(count: Int): String = takeBytes(count).decodeToString()

    /**
     * decode the JVM's modified UTF-8 (CONSTANT_Utf8_info):
     * `U+0000` is stored as `0xC0 0x80`, chars above `U+FFFF` are stored as
     * UTF-16 surrogate pairs, and 4-byte sequences do not occur.
     */
    fun takeModifiedUtf8(count: Int): String {
        val bytes = takeBytes(count)
        val chars = StringBuilder(count)
        var i = 0
        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xFF
            when {
                b shr 7 == 0 -> {
                    chars.append(b.toChar())
                    i += 1
                }
                b shr 5 == 0b110 -> {
                    if (i + 1 >= bytes.size) throw createException("Truncated 2-byte modified utf-8 char")
                    val b2 = bytes[i + 1].toInt() and 0xFF
                    if (b2 shr 6 != 0b10) throw createException("Invalid modified utf-8 continuation byte")
                    chars.append((((b and 0x1F) shl 6) or (b2 and 0x3F)).toChar())
                    i += 2
                }
                b shr 4 == 0b1110 -> {
                    if (i + 2 >= bytes.size) throw createException("Truncated 3-byte modified utf-8 char")
                    val b2 = bytes[i + 1].toInt() and 0xFF
                    val b3 = bytes[i + 2].toInt() and 0xFF
                    if (b2 shr 6 != 0b10 || b3 shr 6 != 0b10) throw createException("Invalid modified utf-8 continuation byte")
                    chars.append((((b and 0x0F) shl 12) or ((b2 and 0x3F) shl 6) or (b3 and 0x3F)).toChar())
                    i += 3
                }
                else -> throw createException("Invalid modified utf-8 lead byte")
            }
        }
        return chars.toString()
    }

    /* expects */

    fun expect(byte: Int): Int {
        val it = take()
        if (it != byte and 0xFF) throw createException("Expected ${byte and 0xFF} but got ${it ?: "EOS"}")
        return byte and 0xFF
    }

    fun expect(bytes: ByteArray): ByteArray {
        for (b in bytes) {
            expect(b.toInt() and 0xFF)
        }
        return bytes
    }

    fun expectEOF() {
        val it = take()
        if (it != null) throw createException("Expected EOF but got $it")
    }

    fun expectEOS() {
        val it = take()
        if (it != null) throw createException("Expected EOS but got $it")
    }

    /* parse alternatives */

    open fun <R> parse(reader: ByteReader<*>.() -> R): R {
        this.mark()
        try {
            val br = BufferedByteReader(this, Int.MAX_VALUE)
            val ret = reader(br)
            this.reset()
            this.skip(br.position)
            return ret
        } catch (e: ParseException) {
            this.reset()
            throw e
        }
    }

    fun <R> parse(vararg readers: ByteReader<*>.() -> R): R {
        val exceptions = mutableListOf<ParseException>()
        for (reader in readers) {
            try {
                return parse(reader)
            } catch (e: ParseException) {
                exceptions.add(e)
            }
        }
        throw createCompositeException("Failed to parse", *exceptions.toTypedArray())
    }

    fun <R> parse(type: String, vararg readers: ByteReader<*>.() -> R): R {
        val exceptions = mutableListOf<ParseException>()
        for (reader in readers) {
            try {
                return parse(reader)
            } catch (e: ParseException) {
                exceptions.add(e)
            }
        }
        throw createCompositeException("Failed to parse as any of $type", *exceptions.toTypedArray())
    }

    fun <R> parseOrNull(vararg readers: ByteReader<*>.() -> R): R? {
        for (reader in readers) {
            try {
                return parse(reader)
            } catch (_: ParseException) {
            }
        }
        return null
    }

    open fun createException(msg: String, cause: Throwable? = null) =
        ParseException.offset(msg, position, cause)

    open fun createCompositeException(msg: String, vararg exceptions: ParseException) =
        createException(msg, null).also {
            for (e in exceptions) {
                it.addSuppressed(e)
            }
        }

    /**
     * This wrapping class uses the reader class as a delegate,
     * so don't do multithreaded stuff or interleave using the outer reader
     * with the wrapped reader.
     */
    class BufferedByteReader(private val reader: ByteReader<*>, private var limit: Int) :
        ByteReader<BufferedByteReader>(), BackedByteReader {
        private var buffer = ByteArray(64)
        private var buffered = 0
        override var position = 0
            internal set
        private var mark = 0

        override val backingBuffer: ByteArray
            get() = buffer

        override val backingPosition: Int
            get() = position

        private fun ensure(capacity: Int) {
            if (capacity > buffer.size) {
                buffer = buffer.copyOf(maxOf(capacity, buffer.size * 2))
            }
        }

        override fun peek(): Int? {
            return peekAt(position)
        }

        fun peekAt(position: Int): Int? {
            if (position >= limit) {
                return null
            }
            if (position > buffered) {
                return null
            }
            if (position == buffered) {
                return reader.peek()
            }
            return buffer[position].toInt() and 0xFF
        }

        override fun take(): Int? {
            if (position >= limit) {
                return null
            }
            val next = takeAt(position) ?: return null
            position++
            return next
        }

        fun takeAt(position: Int): Int? {
            if (position >= limit) {
                return null
            }
            if (position > buffered) {
                throw IllegalStateException("position $position is beyond the buffered frontier $buffered")
            }
            if (position == buffered) {
                val next = reader.take() ?: return null
                ensure(buffered + 1)
                buffer[buffered++] = next.toByte()
                return next
            }
            return buffer[position].toInt() and 0xFF
        }

        override fun copy(limit: Int): CopyReader {
            if (limit < 0) throw IllegalArgumentException("limit < 0")
            val limit = position + limit
            return CopyReader(position, min(if (limit < position) Int.MAX_VALUE else limit, this.limit))
        }

        /**
         * parse is mark-transparent: the caller's mark/reset state is
         * preserved across both success and failure.
         */
        override fun <R> parse(reader: ByteReader<*>.() -> R): R {
            val savedPosition = position
            val savedMark = mark
            try {
                val br = copy()
                val ret = reader(br)
                this.position = br.position
                this.mark = savedMark
                return ret
            } catch (e: ParseException) {
                this.position = savedPosition
                this.mark = savedMark
                throw e
            }
        }

        override fun mark(limit: Int) {
            mark = position
        }

        override fun reset() {
            position = mark
        }

        inner class CopyReader(position: Int, private val limit: Int) : ByteReader<CopyReader>(), BackedByteReader {
            override var position: Int = position
                internal set
            private var mark: Int = position

            override val backingBuffer: ByteArray
                get() = buffer

            override val backingPosition: Int
                get() = position

            override fun peek(): Int? {
                if (position >= limit) {
                    return null
                }
                return peekAt(position)
            }

            override fun take(): Int? {
                if (position >= limit) {
                    return null
                }
                val next = takeAt(position) ?: return null
                position++
                return next
            }

            override fun copy(limit: Int): CopyReader {
                if (limit < 0) throw IllegalArgumentException("limit < 0")
                val limit = position + limit
                return CopyReader(position, min(if (limit < position) Int.MAX_VALUE else limit, this.limit))
            }

            /**
             * parse is mark-transparent: the caller's mark/reset state is
             * preserved across both success and failure.
             */
            override fun <R> parse(reader: ByteReader<*>.() -> R): R {
                val savedPosition = position
                val savedMark = mark
                try {
                    val br = copy()
                    val ret = reader(br)
                    this.position = br.position
                    this.mark = savedMark
                    return ret
                } catch (e: ParseException) {
                    this.position = savedPosition
                    this.mark = savedMark
                    throw e
                }
            }

            override fun mark(limit: Int) {
                mark = position
            }

            override fun reset() {
                position = mark
            }

        }

    }

}
