package xyz.wagyourtail.commonskt.writer

/**
 * Byte sink counterpart to [xyz.wagyourtail.commonskt.reader.ByteReader]:
 * endian-aware writes that mirror the reader's numeric takes, for
 * serializing structured binary data. Byte values are passed as `Int`
 * (low 8 bits used), mirroring [ByteReader]'s `Int` surface.
 *
 * All multi-byte puts default to **big-endian**; `*LE` variants write
 * little-endian.
 */
abstract class ByteWriter {

    /**
     * current write position, in bytes written
     */
    abstract val position: Int

    abstract fun put(b: Int)

    abstract fun put(bytes: ByteArray)

    open fun put(bytes: ByteArray, offset: Int, length: Int) {
        for (i in offset until offset + length) {
            put(bytes[i].toInt() and 0xFF)
        }
    }

    /* numeric puts — big-endian */

    fun putU1(v: Int) = put(v)

    fun putU2(v: Int) {
        put(v shr 8)
        put(v)
    }

    fun putU4(v: UInt) = putI4(v.toInt())

    fun putU8(v: ULong) = putI8(v.toLong())

    fun putI1(v: Byte) = put(v.toInt())

    fun putI2(v: Short) = putU2(v.toInt())

    fun putI4(v: Int) {
        put(v shr 24)
        put(v shr 16)
        put(v shr 8)
        put(v)
    }

    fun putI8(v: Long) {
        putI4((v shr 32).toInt())
        putI4(v.toInt())
    }

    fun putFloat(v: Float) = putI4(v.toBits())

    fun putDouble(v: Double) = putI8(v.toBits())

    /* numeric puts — little-endian */

    fun putU2LE(v: Int) {
        put(v)
        put(v shr 8)
    }

    fun putU4LE(v: UInt) = putI4LE(v.toInt())

    fun putU8LE(v: ULong) = putI8LE(v.toLong())

    fun putI2LE(v: Short) = putU2LE(v.toInt())

    fun putI4LE(v: Int) {
        put(v)
        put(v shr 8)
        put(v shr 16)
        put(v shr 24)
    }

    fun putI8LE(v: Long) {
        putI4LE(v.toInt())
        putI4LE((v shr 32).toInt())
    }

    fun putFloatLE(v: Float) = putI4LE(v.toBits())

    fun putDoubleLE(v: Double) = putI8LE(v.toBits())

    fun putUtf8(v: String) = put(v.encodeToByteArray())

    /**
     * encode the JVM's modified UTF-8 (CONSTANT_Utf8_info):
     * `U+0000` is stored as `0xC0 0x80`, chars above `U+FFFF` are stored as
     * UTF-16 surrogate pairs, and no 4-byte sequences are produced.
     */
    fun putModifiedUtf8(v: String) {
        for (c in v) {
            val code = c.code
            when {
                code == 0 -> {
                    put(0xC0)
                    put(0x80)
                }
                code <= 0x7F -> put(code)
                code <= 0x7FF -> {
                    put(0xC0 or (code shr 6))
                    put(0x80 or (code and 0x3F))
                }
                else -> {
                    put(0xE0 or (code shr 12))
                    put(0x80 or ((code shr 6) and 0x3F))
                    put(0x80 or (code and 0x3F))
                }
            }
        }
    }

}
