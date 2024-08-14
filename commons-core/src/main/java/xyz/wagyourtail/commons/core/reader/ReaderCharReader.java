package xyz.wagyourtail.commons.core.reader;

import xyz.wagyourtail.commons.core.Utils;

import java.io.IOException;
import java.io.Reader;

public class ReaderCharReader extends CharReader<ReaderCharReader> {
    private final Reader reader;
    private int nextChar;

    public ReaderCharReader(Reader reader) throws IOException {
        this.reader = reader;
        nextChar = reader.read();
    }

    @Override
    public int peek() {
        return nextChar;
    }

    @Override
    public int take() {
        int character = nextChar;
        try {
            nextChar = reader.read();
        } catch (IOException e) {
            Utils.<RuntimeException>sneakyThrow(e);
        }
        return character;
    }

    @Override
    public ReaderCharReader copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mark() {
        try {
            reader.mark(-1);
        } catch (IOException e) {
            Utils.<RuntimeException>sneakyThrow(e);
        }
    }

    @Override
    public void reset() {
        try {
            reader.reset();
            nextChar = reader.read();
        } catch (IOException e) {
            Utils.<RuntimeException>sneakyThrow(e);
        }
    }

}
