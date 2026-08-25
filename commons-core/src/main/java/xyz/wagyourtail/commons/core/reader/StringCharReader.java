package xyz.wagyourtail.commons.core.reader;

import lombok.val;
import xyz.wagyourtail.commons.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StringCharReader extends CharReader<StringCharReader> {
    private final CharSequence buffer;
    private final int endPos;
    private int pos;
    private int mark;

    public StringCharReader(CharSequence buffer) {
        this(buffer, 0);
    }

    public StringCharReader(CharSequence buffer, int pos) {
        this(buffer, pos, buffer.length());
    }

    public StringCharReader(CharSequence buffer, int pos, int endPos) {
        this.buffer = buffer;
        this.pos = pos;
        this.endPos = endPos;
    }

    @Override
    public StringCharReader copy(int limit) {
        limit = pos + limit;
        StringCharReader copy = new StringCharReader(buffer, pos, Math.min(limit < pos ? Integer.MAX_VALUE : limit, endPos));
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
        val str = buffer.subSequence(pos, end);
        pos = end;
        return str.toString();
    }

    @Override
    public String takeRemaining() {
        if (pos >= endPos) {
            return "";
        }
        val value = buffer.subSequence(pos, endPos);
        pos = endPos;
        return value.toString();
    }

    @Override
    public String takeUntil(char character) {
        int next = StringUtils.indexOf(buffer, character, pos, endPos);
        if (next == -1) {
            val str = buffer.subSequence(pos, endPos);
            pos = endPos;
            return str.toString();
        }
        val value = buffer.subSequence(pos, next);
        pos = next;
        return value.toString();
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
            val wrapping = copy(Integer.MAX_VALUE);
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
        if (StringUtils.indexOf(buffer, '\n', 0, buffer.length()) == -1) {
            count = -1;
        } else {
            do {
                int next = StringUtils.indexOf(buffer, '\n', lineStart, buffer.length()) + 1;
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
