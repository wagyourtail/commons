package xyz.wagyourtail.commons.core.data;

import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

public class SeekableInMemoryByteChannel implements SeekableByteChannel {
    private static final int DEFAULT_SIZE = 8096;
    private byte[] buffer;
    private int position;
    private int size;

    public SeekableInMemoryByteChannel() {
        this(DEFAULT_SIZE);
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

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
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

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // no-op
    }

    public void expand(int wantedSize) throws IOException {
        int size = nextPowerOf2(wantedSize);
        if (size < 0) {
            throw new IOException("attempted to expand greater than signed 32 bit limit");
        }
        buffer = Arrays.copyOf(buffer, size);
    }

    private int nextPowerOf2(int num) {
        return Integer.highestOneBit(num - 1) << 1;
    }

}
