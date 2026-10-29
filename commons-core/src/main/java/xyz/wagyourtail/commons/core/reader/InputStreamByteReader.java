package xyz.wagyourtail.commons.core.reader;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * {@link ByteReader} over a {@link InputStream} — the byte analog of
 * {@link ReaderCharReader}. Supports bounded backtracking via
 * {@link #mark(int)}/{@link #reset()} — mark records up to `limit` consumed
 * bytes for replay on reset (java.io-style read-ahead limit semantics:
 * exceeding it invalidates the mark, as does {@link #skip(int)}).
 * {@link #copy(int)} is unsupported — use {@link #buffered()} for full
 * multi-copy/speculative parsing.
 */
public class InputStreamByteReader extends ByteReader<InputStreamByteReader> implements AutoCloseable {
    private final PushbackInputStream stream;
    private int position = 0;
    private int markedPosition = -1;
    private int markLimit = 0;
    private byte[] record = new byte[64];
    private int recordLength = 0;
    private byte[] serve = null;
    private int serveIndex = 0;
    private int serveLength = 0;

    public InputStreamByteReader(InputStream stream) throws IOException {
        this.stream = stream instanceof PushbackInputStream ? (PushbackInputStream) stream : new PushbackInputStream(stream, 1);
    }

    @SneakyThrows
    @Override
    public int peek() {
        if (serve != null && serveIndex < serveLength) {
            return serve[serveIndex] & 0xFF;
        }
        int b = stream.read();
        if (b == -1) return -1;
        stream.unread(b);
        return b;
    }

    @SneakyThrows
    @Override
    public int take() {
        if (serve != null && serveIndex < serveLength) {
            int b = serve[serveIndex++] & 0xFF;
            position++;
            return b;
        }
        serve = null;
        int b = stream.read();
        if (b == -1) return -1;
        if (markedPosition >= 0) {
            if (recordLength >= markLimit) {
                // read-ahead limit exceeded — mark invalidated
                markedPosition = -1;
                recordLength = 0;
            } else {
                if (recordLength == record.length) {
                    byte[] grown = new byte[Math.max(64, record.length * 2)];
                    System.arraycopy(record, 0, grown, 0, recordLength);
                    record = grown;
                }
                record[recordLength++] = (byte) b;
            }
        }
        position++;
        return b;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @SneakyThrows
    @Override
    public int skip(int count) {
        int skipped = 0;
        if (serve != null) {
            int n = Math.min(count, serveLength - serveIndex);
            serveIndex += n;
            skipped += n;
            position += n;
        }
        if (skipped < count) {
            int n = (int) stream.skip(count - skipped);
            skipped += n;
            position += n;
        }
        // skipped bytes aren't recorded — the mark can no longer be replayed
        markedPosition = -1;
        return skipped;
    }

    @Override
    public InputStreamByteReader copy(int limit) {
        throw new UnsupportedOperationException("stream readers cannot be copied; wrap in BufferedByteReader first");
    }

    @Override
    public void mark(int limit) {
        markedPosition = position;
        markLimit = limit;
        if (serve != null && serveIndex < serveLength) {
            // pending replay bytes are the first bytes at the new mark —
            // move them into the record so reset() replays them correctly
            int pending = serveLength - serveIndex;
            record = new byte[Math.max(pending, Math.max(1, Math.min(limit, 64)))];
            System.arraycopy(serve, serveIndex, record, 0, pending);
            recordLength = pending;
            serve = record;
            serveIndex = 0;
            serveLength = pending;
        } else {
            record = new byte[Math.max(1, Math.min(limit, 64))];
            recordLength = 0;
        }
    }

    @Override
    public void reset() {
        if (markedPosition < 0) {
            throw new IllegalStateException("no active mark (unset, or read-ahead limit exceeded)");
        }
        serve = record;
        serveIndex = 0;
        serveLength = recordLength;
        position = markedPosition;
    }

    /**
     * @return a buffering wrapper enabling full mark/reset/copy/parse support
     * over this stream.
     */
    public Buffered buffered() {
        return buffered(Integer.MAX_VALUE);
    }

    /**
     * @return a buffering wrapper enabling full mark/reset/copy/parse support
     * over this stream.
     */
    public Buffered buffered(int limit) {
        return new Buffered(this, limit);
    }

    @Override
    public ParseException createException(String message, Throwable cause) {
        return ParseException.offset(message, position, cause);
    }

    @Override
    public void close() throws Exception {
        stream.close();
    }

    public class Buffered extends BufferedByteReader implements AutoCloseable {

        public Buffered(ByteReader<?> reader, int limit) {
            super(reader, limit);
        }

        @Override
        public void close() throws Exception {
            InputStreamByteReader.this.close();
        }

    }
}
