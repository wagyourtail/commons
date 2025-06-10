package xyz.wagyourtail.commons.core.reader;

import lombok.val;
import xyz.wagyourtail.commons.core.StringUtils;

public class StringCharReader extends CharReader<StringCharReader> {
    private final String buffer;
    private final int endPos;
    private int pos;
    private int mark;

    public StringCharReader(String buffer) {
        this(buffer, 0);
    }

    public StringCharReader(String buffer, int pos) {
        this(buffer, pos, buffer.length());
    }

    public StringCharReader(String buffer, int pos, int endPos) {
        this.buffer = buffer;
        this.pos = pos;
        this.endPos = endPos;
    }

    @Override
    public StringCharReader copy() {
        return copy(endPos - pos);
    }

    @Override
    public StringCharReader copy(int limit) {
        StringCharReader copy = new StringCharReader(buffer, pos, pos + limit);
        copy.mark();
        return copy;
    }

    @Override
    public int peek() {
        if (pos >= endPos) {
            return -1;
        }
        return buffer.charAt(pos);
    }

    @Override
    public int take() {
        if (pos >= endPos) {
            return -1;
        }
        return buffer.charAt(pos++);
    }

    @Override
    public String take(int count) {
        if (pos >= endPos) {
            return "";
        }
        int end = Math.min(pos + count, endPos);
        String str = buffer.substring(pos, end);
        pos = end;
        return str;
    }

    @Override
    public String takeRemaining() {
        if (pos >= endPos) {
            return "";
        }
        String value = buffer.substring(pos, endPos);
        pos = endPos;
        return value;
    }

    @Override
    public String takeUntil(char character) {
        int next = StringUtils.indexOf(buffer, character, pos, endPos);
        if (next == -1) {
            String str = buffer.substring(pos, endPos);
            pos = endPos;
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

    public <R> R parse(StringCharReader.ElementReader<R> reader) {
        this.mark();
        try {
            val wrapping = copy();
            val value = reader.read(wrapping);
            this.pos = wrapping.pos;
            return value;
        } catch (ParseException t) {
            this.reset();
            throw t;
        }
    }

    @Override
    public ParseException createException(String message, Throwable cause) {
        return super.createException(message + " (at " + getPosition() + ")", cause);
    }

    public String getPosition() {
        if (!buffer.contains("\n")) {
            return String.valueOf(pos);
        }
        int line = StringUtils.count(buffer.substring(0, pos), '\n') + 1;
        int column = pos - buffer.substring(0, pos).lastIndexOf('\n');
        return line + ":" + column;
    }
}
