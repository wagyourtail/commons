package xyz.wagyourtail.commons.core.writer;

/**
 * {@link ByteWriter} into a growable in-memory {@code byte[]}, with
 * random-access {@link #seek(int)}/overwrite and a {@link #toByteArray()}
 * snapshot — the byte analog of a {@link StringBuilder}.
 */
public class ByteArrayByteWriter extends ByteWriter {

    private byte[] buffer;
    private int position = 0;

    public ByteArrayByteWriter() {
        this(64);
    }

    public ByteArrayByteWriter(int initialCapacity) {
        buffer = new byte[initialCapacity];
    }

    private void ensure(int capacity) {
        if (capacity > buffer.length) {
            byte[] grown = new byte[Math.max(capacity, buffer.length * 2)];
            System.arraycopy(buffer, 0, grown, 0, position);
            buffer = grown;
        }
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void put(int b) {
        ensure(position + 1);
        buffer[position++] = (byte) b;
    }

    @Override
    public void put(byte[] bytes) {
        put(bytes, 0, bytes.length);
    }

    @Override
    public void put(byte[] bytes, int offset, int length) {
        ensure(position + length);
        System.arraycopy(bytes, offset, buffer, position, length);
        position += length;
    }

    /**
     * jump to an absolute position (growing with zero-fill if beyond the end).
     */
    public void seek(int pos) {
        if (pos < 0) throw new IllegalArgumentException("seek position " + pos + " < 0");
        if (pos > position) {
            ensure(pos);
        }
        position = pos;
    }

    /**
     * overwrite a byte at an already-written position.
     */
    public void overwrite(int pos, int b) {
        if (pos < 0 || pos >= position) {
            throw new IllegalArgumentException("overwrite position " + pos + " out of range 0.." + position);
        }
        buffer[pos] = (byte) b;
    }

    /**
     * overwrite bytes at an already-written position — for backpatching
     * length-prefixed structures (write placeholder, fill after content).
     */
    public void overwrite(int pos, byte[] bytes) {
        if (pos < 0 || pos + bytes.length > position) {
            throw new IllegalArgumentException("overwrite range " + pos + ".." + (pos + bytes.length) + " out of range 0.." + position);
        }
        System.arraycopy(bytes, 0, buffer, pos, bytes.length);
    }

    /**
     * @return bytes written so far (a copy)
     */
    public byte[] toByteArray() {
        byte[] out = new byte[position];
        System.arraycopy(buffer, 0, out, 0, position);
        return out;
    }

}
