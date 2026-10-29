package xyz.wagyourtail.commons.core.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ByteReader} over an in-memory {@code byte[]}, with random-access
 * {@link #seek(int)} and zero-copy {@link #copy(int)} sub-range readers — the
 * byte analog of {@link StringCharReader}.
 */
public class ByteArrayByteReader extends ByteReader<ByteArrayByteReader> implements BackedByteReader {
    private final byte[] buffer;
    private final int endPos;
    private int position;
    private int mark;

    @Override
    public byte[] getBackingBuffer() {
        return buffer;
    }

    @Override
    public int getBackingPosition() {
        return position;
    }

    public ByteArrayByteReader(byte[] buffer) {
        this(buffer, 0);
    }

    public ByteArrayByteReader(byte[] buffer, int pos) {
        this(buffer, pos, buffer.length);
    }

    public ByteArrayByteReader(byte[] buffer, int pos, int endPos) {
        this.buffer = buffer;
        this.position = pos;
        this.endPos = endPos;
        this.mark = pos;
    }

    @Override
    public int peek() {
        if (position >= endPos) {
            return -1;
        }
        return buffer[position] & 0xFF;
    }

    @Override
    public int take() {
        if (position >= endPos) {
            return -1;
        }
        return buffer[position++] & 0xFF;
    }

    @Override
    public int getPosition() {
        return position;
    }

    /**
     * jump to an absolute position within this reader's range.
     */
    public void seek(int pos) {
        if (pos < 0 || pos > endPos) {
            throw new IllegalArgumentException("seek position " + pos + " out of range 0.." + endPos);
        }
        this.position = pos;
    }

    @Override
    public int skip(int count) {
        int end = Math.min(position + count, endPos);
        int skipped = end - position;
        position = end;
        return skipped;
    }

    @Override
    public byte[] take(int count) {
        if (position < endPos) {
            int end = Math.min(position + count, endPos);
            byte[] out = new byte[end - position];
            System.arraycopy(buffer, position, out, 0, end - position);
            position = end;
            return out;
        }
        return new byte[0];
    }

    @Override
    public ByteArrayByteReader copy(int limit) {
        if (limit < 0) throw new IllegalArgumentException("limit < 0");
        limit = position + limit;
        ByteArrayByteReader copy = new ByteArrayByteReader(buffer, position, Math.min(limit < position ? Integer.MAX_VALUE : limit, endPos));
        copy.mark(Integer.MAX_VALUE);
        return copy;
    }

    @Override
    public byte[] takeRemaining() {
        if (position < endPos) {
            byte[] out = new byte[endPos - position];
            System.arraycopy(buffer, position, out, 0, endPos - position);
            position = endPos;
            return out;
        }
        return new byte[0];
    }

    @Override
    public byte[] takeUntil(int b) {
        byte needle = (byte) (b & 0xFF);
        int next = position;
        while (next < endPos && buffer[next] != needle) next++;
        byte[] out = new byte[next - position];
        System.arraycopy(buffer, position, out, 0, next - position);
        position = next;
        return out;
    }

    @Override
    public void mark(int limit) {
        mark = position;
    }

    @Override
    public void reset() {
        position = mark;
    }

    @Override
    /**
     * parse is mark-transparent: the caller's mark/reset state is preserved
     * across both success and failure.
     */
    public <R> R parse(ElementReader<R> reader) throws ParseException {
        int savedPosition = position;
        int savedMark = mark;
        try {
            ByteArrayByteReader wrapping = copy(Integer.MAX_VALUE);
            R value = reader.apply(wrapping);
            position = wrapping.position;
            mark = savedMark;
            return value;
        } catch (ParseException e) {
            position = savedPosition;
            mark = savedMark;
            throw e;
        }
    }

    @Override
    public ParseException createException(String msg, Throwable cause) {
        return ParseException.offset(msg, position, cause);
    }

    @Override
    public ParseException createCompositeException(String msg, ParseException... exceptions) {
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
            return createException(msg, lastException);
        } else {
            ParseException exception = createException(msg, null);
            for (ParseException e : lastExceptions) {
                exception.addSuppressed(e);
            }
            return exception;
        }
    }

}
