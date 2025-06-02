package xyz.wagyourtail.commons.core.reader;

import xyz.wagyourtail.commons.core.StringUtils;

public class StringCharReader extends CharReader<StringCharReader> {
    private final String buffer;
    private final int limit;
    private int pos;
    private int mark;

    public StringCharReader(String buffer) {
        this(buffer, 0);
    }

    public StringCharReader(String buffer, int pos) {
        this(buffer, pos, buffer.length());
    }

    public StringCharReader(String buffer, int pos, int limit) {
        this.buffer = buffer;
        this.pos = pos;
        this.limit = limit;
    }

    @Override
    public StringCharReader copy() {
        return copy(limit);
    }

    @Override
    public StringCharReader copy(int limit) {
        StringCharReader copy = new StringCharReader(buffer, pos, limit);
        copy.mark();
        return copy;
    }

    @Override
    public int peek() {
        if (pos >= limit) {
            return -1;
        }
        return buffer.charAt(pos);
    }

    @Override
    public int take() {
        if (pos >= limit) {
            return -1;
        }
        return buffer.charAt(pos++);
    }

    @Override
    public String take(int count) {
        if (pos >= limit) {
            return "";
        }
        int end = Math.min(pos + count, limit);
        String str = buffer.substring(pos, end);
        pos = end;
        return str;
    }

    @Override
    public String takeRemaining() {
        if (pos >= limit) {
            return "";
        }
        String value = buffer.substring(pos, limit);
        pos = limit;
        return value;
    }

    @Override
    public String takeUntil(char character) {
        int next = StringUtils.indexOf(buffer, character, pos, limit);
        if (next == -1) {
            String str = buffer.substring(pos, limit);
            pos = limit;
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
