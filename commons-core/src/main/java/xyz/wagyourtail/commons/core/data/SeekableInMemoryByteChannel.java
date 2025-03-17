package xyz.wagyourtail.commons.core.data;

import org.jetbrains.annotations.VisibleForTesting;
import xyz.wagyourtail.commons.core.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

public class SeekableInMemoryByteChannel implements SeekableByteChannel {
    private byte[] buffer;
    private int position;
    private int size;

    public SeekableInMemoryByteChannel() {
        buffer = new byte[0];
        position = 0;
        size = 0;
    }

    public SeekableInMemoryByteChannel(int size) {
        this(new byte[size]);
    }

    public SeekableInMemoryByteChannel(byte[] buffer) {
        this.buffer = buffer;
        this.position = 0;
        this.size = buffer.length;
    }

    @VisibleForTesting
    public byte[] getBuffer() {
        return buffer;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (position >= size) {
            return -1;
        }
        int readSize = Math.min(size - position, dst.remaining());
        dst.put(buffer, position, readSize);
        position += readSize;
        return readSize;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (src.remaining() > buffer.length - position) {
            expand(buffer.length + src.remaining() - (buffer.length - position));
        }
        int writeSize = src.remaining();
        src.get(buffer, position, writeSize);
        position += writeSize;
        if (position >= size) {
            size = position;
        }
        return writeSize;
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (newPosition < 0 || newPosition > Integer.MAX_VALUE) {
            throw new IOException("Position out of bounds");
        }
        this.position = (int) newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        return size;
    }

    /**
     * Changes the size of the channel
     *
     * @param size The new size, a non-negative byte count
     * @return self for chaining
     */
    @Override
    public SeekableByteChannel truncate(long size) {
        if (size < 0 || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size must be between 0 and Integer.MAX_VALUE");
        }
        if (size < this.size) {
            this.size = (int) size;
        }
        if (position > size) {
            this.position = (int) size;
        }
        return this;
    }

    /**
     * actually truncates the underlying buffer
     *
     * @param size The new size, a non-negative byte count
     * @return self for chaining
     */
    public SeekableByteChannel hardTruncate(long size) {
        if (size < 0 || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size must be between 0 and Integer.MAX_VALUE");
        }
        truncate(size);
        buffer = Arrays.copyOf(buffer, (int) size);
        return this;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // no-op
    }

    public void expand(int wantedSize) throws IOException {
        if (wantedSize < buffer.length) {
            size = wantedSize;
        }
        int size = Utils.nextPowerOf2(wantedSize);
        if (size < 0) {
            throw new IOException("attempted to expand greater than signed 32 bit limit");
        }
        buffer = Arrays.copyOf(buffer, size);
    }


}
