package xyz.wagyourtail.commons.core.reader;

import lombok.val;
import xyz.wagyourtail.commons.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
    public void mark(int limit) {
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
        int count = 0;
        int lineStart = 0;
        if (!buffer.contains("\n")) {
            count = -1;
        } else {
            do {
                int next = buffer.indexOf('\n', lineStart) + 1;
                if (next == 0 || next > pos) break;
                lineStart = next;
                count++;
            } while (true);
        }
        return new ParseException(message, count + 1, pos - lineStart + 1, cause);
    }

    @Override
    public ParseException createCompositeException(String message, ParseException... exceptions) {
        // find furthest exception
        List<ParseException> lastExceptions = new ArrayList<>();
        ParseException lastException = null;
        for (ParseException e : exceptions) {
            if (lastException == null) {
                lastException = e;
                lastExceptions.add(e);
                continue;
            }
            int compare = lastException.compareTo(e);
            if (compare < 0) {
                lastExceptions.clear();
                lastExceptions.add(e);
                lastException = e;
            } else if (compare == 0) {
                lastExceptions.add(e);
            }
        }
        if (lastExceptions.size() == 1) {
            return createException(message, lastException);
        } else {
            val exception = createException(message);
            for (ParseException e : lastExceptions) {
                exception.addSuppressed(e);
            }
            return exception;
        }
    }

}
