package xyz.wagyourtail.commons.core.writer;

/**
 * Byte sink counterpart to {@link xyz.wagyourtail.commons.core.reader.ByteReader}:
 * endian-aware writes that mirror the reader's numeric takes, for
 * serializing structured binary data. Byte values are passed as {@code int}
 * (low 8 bits used), mirroring {@code ByteReader}'s {@code int} surface.
 *
 * All multi-byte puts default to <b>big-endian</b>; {@code *LE} variants write
 * little-endian.
 */
public abstract class ByteWriter {

    /**
     * @return current write position, in bytes written
     */
    public abstract int getPosition();

    public abstract void put(int b);

    public abstract void put(byte[] bytes);

    public void put(byte[] bytes, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            put(bytes[i] & 0xFF);
        }
    }

    /* numeric puts — big-endian */

    public void putU1(int v) {
        put(v);
    }

    public void putU2(int v) {
        put(v >> 8);
        put(v);
    }

    public void putU4(long v) {
        putI4((int) v);
    }

    public void putU8(long v) {
        putI8(v);
    }

    public void putI1(byte v) {
        put(v);
    }

    public void putI2(short v) {
        putU2(v);
    }

    public void putI4(int v) {
        put(v >> 24);
        put(v >> 16);
        put(v >> 8);
        put(v);
    }

    public void putI8(long v) {
        putI4((int) (v >> 32));
        putI4((int) v);
    }

    public void putFloat(float v) {
        putI4(Float.floatToIntBits(v));
    }

    public void putDouble(double v) {
        putI8(Double.doubleToLongBits(v));
    }

    /* numeric puts — little-endian */

    public void putU2LE(int v) {
        put(v);
        put(v >> 8);
    }

    public void putU4LE(long v) {
        putI4LE((int) v);
    }

    public void putU8LE(long v) {
        putI8LE(v);
    }

    public void putI2LE(short v) {
        putU2LE(v);
    }

    public void putI4LE(int v) {
        put(v);
        put(v >> 8);
        put(v >> 16);
        put(v >> 24);
    }

    public void putI8LE(long v) {
        putI4LE((int) v);
        putI4LE((int) (v >> 32));
    }

    public void putFloatLE(float v) {
        putI4LE(Float.floatToIntBits(v));
    }

    public void putDoubleLE(double v) {
        putI8LE(Double.doubleToLongBits(v));
    }

    public void putUtf8(String v) {
        put(v.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * encode the JVM's modified UTF-8 (CONSTANT_Utf8_info):
     * {@code U+0000} is stored as {@code 0xC0 0x80}, chars above
     * {@code U+FFFF} are stored as UTF-16 surrogate pairs, and no 4-byte
     * sequences are produced.
     */
    public void putModifiedUtf8(String v) {
        for (int i = 0; i < v.length(); i++) {
            int code = v.charAt(i);
            if (code == 0) {
                put(0xC0);
                put(0x80);
            } else if (code <= 0x7F) {
                put(code);
            } else if (code <= 0x7FF) {
                put(0xC0 | (code >> 6));
                put(0x80 | (code & 0x3F));
            } else {
                put(0xE0 | (code >> 12));
                put(0x80 | ((code >> 6) & 0x3F));
                put(0x80 | (code & 0x3F));
            }
        }
    }

}
