package xyz.wagyourtail.commons.core.data;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class SeekableByteChannelInputStream extends InputStream {

    private final SeekableByteChannel channel;
    private long pos = 0;
    private long mark = 0;

    public SeekableByteChannelInputStream(SeekableByteChannel channel) {
        this.channel = channel;
    }

    @Override
    public int available() throws IOException {
        long avail = (this.channel.size() - this.pos);
        return avail > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) avail;
    }

    @Override
    public int read() throws IOException {
        if (this.pos >= this.channel.size()) {
            return -1;
        }
        synchronized (this.channel) {
            if (this.channel.position() != this.pos) this.channel.position(this.pos);
            this.pos++;
            ByteBuffer buf = ByteBuffer.allocate(1);
            this.channel.read(buf);
            return buf.get(0) & 0xFF;
        }
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        if (this.pos >= this.channel.size()) {
            return -1;
        }
        synchronized (this.channel) {
            if (this.channel.position() != this.pos) this.channel.position(this.pos);
            int i = this.channel.read(ByteBuffer.wrap(b, off, len));
            this.pos += i;
            return i;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.mark = this.pos;
    }

    @Override
    public synchronized void reset() {
        this.pos = this.mark;
    }

}