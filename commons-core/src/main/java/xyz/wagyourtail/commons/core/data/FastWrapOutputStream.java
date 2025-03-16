package xyz.wagyourtail.commons.core.data;


import java.io.ByteArrayOutputStream;
import java.nio.channels.SeekableByteChannel;

public class FastWrapOutputStream extends ByteArrayOutputStream {

    public SeekableByteChannel wrap() {
        return new SeekableInMemoryByteChannel(this.buf).truncate(this.count);
    }

}
