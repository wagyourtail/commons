package xyz.wagyourtail.commons.core.writer;

import lombok.SneakyThrows;

import java.io.OutputStream;

/**
 * {@link ByteWriter} over a {@link OutputStream} — sequential writes only.
 */
public class OutputStreamByteWriter extends ByteWriter implements AutoCloseable {
    private final OutputStream stream;
    private int position = 0;

    public OutputStreamByteWriter(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    @SneakyThrows
    public void put(int b) {
        stream.write(b);
        position++;
    }

    @Override
    @SneakyThrows
    public void put(byte[] bytes) {
        stream.write(bytes);
        position += bytes.length;
    }

    @Override
    @SneakyThrows
    public void put(byte[] bytes, int offset, int length) {
        stream.write(bytes, offset, length);
        position += length;
    }

    @Override
    public void close() throws Exception {
        stream.close();
    }

}
