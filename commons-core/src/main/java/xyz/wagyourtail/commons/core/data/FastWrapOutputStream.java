package xyz.wagyourtail.commons.core.data;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;

public class FastWrapOutputStream extends ByteArrayOutputStream {

    @Override
    public synchronized void reset() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("resource")
    public SeekableByteChannel wrap() {
        return new SeekableInMemoryByteChannel(this.buf).truncate(this.count);
    }

    public InputStream asInputStream() {
        return new ByteArrayInputStream(this.buf, 0, this.count);
    }

}
