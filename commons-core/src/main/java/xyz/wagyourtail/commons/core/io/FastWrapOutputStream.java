package xyz.wagyourtail.commons.core.io;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;

public class FastWrapOutputStream extends ByteArrayOutputStream {

    @Override
    public synchronized void reset() {
        count = 0;
        this.buf = new byte[32];
    }

    @SuppressWarnings("resource")
    public SeekableByteChannel wrap() {
        return new SeekableInMemoryByteChannel(this.buf).truncate(this.count);
    }

    public InputStream asInputStream() {
        return new ByteArrayInputStream(this.buf, 0, this.count);
    }

}
