package xyz.wagyourtail.commons.core.reader;

public class StringCharReader extends CharReader<StringCharReader> {
    private final String buffer;
    private int pos;
    private int mark;

    public StringCharReader(String buffer) {
        this(buffer, 0);
    }

    public StringCharReader(String buffer, int pos) {
        this.buffer = buffer;
        this.pos = pos;
    }

    @Override
    public StringCharReader copy() {
        return new StringCharReader(buffer, pos);
    }

    @Override
    public int peek() {
        if (pos >= buffer.length()) {
            return -1;
        }
        return buffer.charAt(pos);
    }

    @Override
    public int take() {
        if (pos >= buffer.length()) {
            return -1;
        }
        return buffer.charAt(pos++);
    }

    @Override
    public String takeRemaining() {
        if (pos >= buffer.length()) {
            return "";
        }
        String value = buffer.substring(pos);
        pos = buffer.length();
        return value;
    }

    @Override
    public String takeUntil(char character) {
        int next = buffer.indexOf(character, pos);
        if (next == -1) {
            String str = buffer.substring(pos);
            pos = buffer.length();
            return str;
        }
        String value = buffer.substring(pos, next);
        pos = next;
        return value;
    }

    @Override
    public void mark() {
        mark = pos;
    }

    @Override
    public void reset() {
        pos = mark;
    }

}
