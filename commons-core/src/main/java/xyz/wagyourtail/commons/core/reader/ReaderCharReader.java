package xyz.wagyourtail.commons.core.reader;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * Sequential {@link CharReader} over a {@link Reader}. Supports bounded
 * backtracking via {@link #mark(int)}/{@link #reset()} — mark records up to
 * `limit` consumed chars for replay on reset (java.io-style read-ahead limit
 * semantics: exceeding it invalidates the mark, as does {@link #skip(int)}).
 * {@link #copy(int)} is unsupported — use {@link #buffered()} for full
 * multi-copy/speculative parsing.
 */
public class ReaderCharReader extends CharReader<ReaderCharReader> implements AutoCloseable {
    private final PushbackReader reader;
    private int position = 0;
    private int markedPosition = -1;
    private int markLimit = 0;
    private char[] record = new char[64];
    private int recordLength = 0;
    private char[] serve = null;
    private int serveIndex = 0;
    private int serveLength = 0;

    public ReaderCharReader(Reader reader) throws IOException {
        this.reader = reader instanceof PushbackReader ? (PushbackReader) reader : new PushbackReader(reader, 1);
    }

    @SneakyThrows
    @Override
    public int peek() {
        if (serve != null && serveIndex < serveLength) {
            return serve[serveIndex];
        }
        int c = reader.read();
        if (c == -1) return -1;
        reader.unread(c);
        return c;
    }

    @SneakyThrows
    @Override
    public int take() {
        if (serve != null && serveIndex < serveLength) {
            int c = serve[serveIndex++];
            position++;
            return c;
        }
        serve = null;
        int c = reader.read();
        if (c == -1) return -1;
        if (markedPosition >= 0) {
            if (recordLength >= markLimit) {
                // read-ahead limit exceeded — mark invalidated
                markedPosition = -1;
                recordLength = 0;
            } else {
                if (recordLength == record.length) {
                    char[] grown = new char[Math.max(64, record.length * 2)];
                    System.arraycopy(record, 0, grown, 0, recordLength);
                    record = grown;
                }
                record[recordLength++] = (char) c;
            }
        }
        position++;
        return c;
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
            int n = (int) reader.skip(count - skipped);
            skipped += n;
            position += n;
        }
        // skipped chars aren't recorded — the mark can no longer be replayed
        markedPosition = -1;
        return skipped;
    }

    @Override
    public ReaderCharReader copy(int limit) {
        throw new UnsupportedOperationException("stream readers cannot be copied; wrap in BufferedCharReader first");
    }

    @Override
    public void mark(int limit) {
        markedPosition = position;
        markLimit = limit;
        if (serve != null && serveIndex < serveLength) {
            // pending replay chars are the first chars at the new mark —
            // move them into the record so reset() replays them correctly
            int pending = serveLength - serveIndex;
            record = new char[Math.max(pending, Math.max(1, Math.min(limit, 64)))];
            System.arraycopy(serve, serveIndex, record, 0, pending);
            recordLength = pending;
            serve = record;
            serveIndex = 0;
            serveLength = pending;
        } else {
            record = new char[Math.max(1, Math.min(limit, 64))];
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

    public Buffered buffered() {
        return buffered(Integer.MAX_VALUE);
    }

    public Buffered buffered(int limit) {
        return new Buffered(this, limit);
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }

    public class Buffered extends BufferedCharReader implements AutoCloseable {

        public Buffered(CharReader<?> reader, int limit) {
            super(reader, limit);
        }

        @Override
        public void close() throws Exception {
            reader.close();
        }

    }
}
