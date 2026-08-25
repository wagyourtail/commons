package xyz.wagyourtail.commons.core.reader;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class ReaderCharReader extends CharReader<ReaderCharReader> implements AutoCloseable {
    private final Reader reader;
    private int nextChar;

    public ReaderCharReader(Reader reader) throws IOException {
        if (!reader.markSupported()) {
            reader = new BufferedReader(reader);
        }
        this.reader = reader;
        nextChar = reader.read();
    }

    @Override
    public int peek() {
        return nextChar;
    }

    @Override
    @SneakyThrows
    public int take() {
        int character = nextChar;
        nextChar = reader.read();
        return character;
    }

    @Override
    @SneakyThrows
    public int skip(int count) {
        return (int) reader.skip(count);
    }

    @Override
    public ReaderCharReader copy(int limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SneakyThrows
    public void mark(int limit) {
        reader.mark(limit);
    }

    @Override
    @SneakyThrows
    public void reset() {
        reader.reset();
        nextChar = reader.read();
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
