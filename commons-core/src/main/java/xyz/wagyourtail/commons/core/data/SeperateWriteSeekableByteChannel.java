package xyz.wagyourtail.commons.core.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class SeperateWriteSeekableByteChannel implements SeekableByteChannel {

    private SeekableByteChannel original;
    private volatile SeekableByteChannel write;
    private boolean changed = false;

    public SeperateWriteSeekableByteChannel(SeekableByteChannel original) {
        this.original = original;
    }

    public boolean isChanged() {
        return this.changed;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (this.changed) {
            return this.write.read(dst);
        } else {
            return this.original.read(dst);
        }
    }

    private void createWrite() throws IOException {
        if (this.write == null) {
            synchronized (this) {
                if (this.write == null) {
                    this.changed = true;
                    if (this.original.size() > Integer.MAX_VALUE) {
                        throw new UnsupportedOperationException("File too large");
                    } else {
                        ByteBuffer buf = ByteBuffer.allocate((int) this.original.size());
                        long pos = this.original.position();
                        this.original.position(0);
                        this.original.read(buf);
                        this.write = new SeekableInMemoryByteChannel(buf.array());
                        this.original.position(pos);
                        this.write.position(pos);
                    }
                    this.original = null;
                }
            }
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        this.createWrite();
        return this.write.write(src);
    }

    @Override
    public long position() throws IOException {
        if (this.changed) {
            return this.write.position();
        } else {
            return this.original.position();
        }
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (this.changed) {
            this.write.position(newPosition);
        } else {
            this.original.position(newPosition);
        }
        return this;
    }

    @Override
    public long size() throws IOException {
        if (this.changed) {
            return this.write.size();
        } else {
            return this.original.size();
        }
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        if (this.size() != size) {
            this.createWrite();
            this.write.truncate(size);
        }
        return this;
    }

    @Override
    public boolean isOpen() {
        if (this.changed) {
            return this.write.isOpen();
        } else {
            return this.original.isOpen();
        }
    }

    @Override
    public void close() throws IOException {
        if (this.changed) {
            this.write.close();
        } else {
            this.original.close();
        }
    }

}
