package xyz.wagyourtail.commons.core.reader;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Byte-oriented counterpart to {@link CharReader}: a pull reader over a byte
 * source with mark/reset, limited sub-reader {@link #copy(int)}, speculative
 * {@link #parse(ElementReader)} alternatives, and endian-aware numeric takes.
 * Byte values are surfaced as {@code int} in {@code 0..255}, with {@code -1}
 * signalling EOS — mirroring {@link CharReader}'s {@code int} char.
 *
 * All multi-byte numeric takes default to <b>big-endian</b> (the byte order
 * of JVM-era formats like .class); {@code *LE} variants read little-endian.
 */
public abstract class ByteReader<T extends ByteReader<T>> {

    /**
     * @return byte value 0..255, or -1 for eos
     */
    public abstract int peek();

    /**
     * @return byte value 0..255, or -1 for eos
     */
    public abstract int take();

    /**
     * @return current read position, in bytes consumed
     */
    public abstract int getPosition();

    /**
     * @return the number of bytes skipped
     */
    public int skip(int count) {
        int skipped = 0;
        while (skipped < count) {
            if (take() == -1) break;
            skipped++;
        }
        return skipped;
    }

    /**
     * @return the next count bytes (fewer if EOS is hit)
     */
    public byte[] take(int count) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(count);
        for (int i = 0; i < count; i++) {
            int b = take();
            if (b == -1) break;
            out.write(b);
        }
        return out.toByteArray();
    }

    /**
     * @return a copy of the reader at the current position. Copies have their
     * own position/mark state; multiple copies may be active and interleaved
     * freely — reading a copy never moves the parent or sibling copies.
     * Sequential (stream) readers cannot copy; wrap them in
     * {@link BufferedByteReader} first.
     */
    public abstract ByteReader<?> copy(int limit);

    /**
     * set this position as remembered.
     * @param limit maximum bytes that may be consumed before the mark becomes
     * invalid — advisory for buffer/array readers (always replayable), but
     * on sequential readers it bounds the recorded replay buffer like
     * java.io's read-ahead limit.
     */
    public abstract void mark(int limit);

    /**
     * reset to remembered mark position, or beginning if no mark.
     * On sequential readers this throws {@link IllegalStateException} when
     * there is no active mark (unset, or invalidated by exceeding the mark
     * limit or by {@link #skip(int)}).
     */
    public abstract void reset();

    public boolean exhausted() {
        return peek() == -1;
    }

    public byte[] takeRemaining() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (peek() != -1) {
            out.write(take());
        }
        return out.toByteArray();
    }

    public byte[] takeUntil(final int b) {
        return takeUntil(new ByteAccepter() {
            @Override
            public boolean accept(int next) {
                return next == (b & 0xFF);
            }
        });
    }

    public byte[] takeUntil(ByteAccepter sep) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int next = peek();
        while (next != -1 && !sep.accept(next)) {
            out.write(take());
            next = peek();
        }
        return out.toByteArray();
    }

    public byte[] takeWhile(final ByteAccepter acceptor) {
        return takeUntil(new ByteAccepter() {
            @Override
            public boolean accept(int next) {
                return !acceptor.accept(next);
            }
        });
    }

    /* numeric takes — big-endian */

    public int takeU1() {
        int b = take();
        if (b == -1) throw createException("Expected u1 but got EOS", null);
        return b;
    }

    public int takeU2() {
        return (takeU1() << 8) | takeU1();
    }

    public long takeU4() {
        return takeI4() & 0xFFFFFFFFL;
    }

    public long takeU8() {
        return takeI8();
    }

    public byte takeI1() {
        return (byte) takeU1();
    }

    public short takeI2() {
        return (short) takeU2();
    }

    public int takeI4() {
        return (takeU1() << 24) | (takeU1() << 16) | (takeU1() << 8) | takeU1();
    }

    public long takeI8() {
        return ((long) takeI4() << 32) | (takeI4() & 0xFFFFFFFFL);
    }

    public float takeFloat() {
        return Float.intBitsToFloat(takeI4());
    }

    public double takeDouble() {
        return Double.longBitsToDouble(takeI8());
    }

    /* numeric takes — little-endian */

    public int takeU2LE() {
        return takeU1() | (takeU1() << 8);
    }

    public long takeU4LE() {
        return takeI4LE() & 0xFFFFFFFFL;
    }

    public long takeU8LE() {
        return takeI8LE();
    }

    public short takeI2LE() {
        return (short) takeU2LE();
    }

    public int takeI4LE() {
        return takeU1() | (takeU1() << 8) | (takeU1() << 16) | (takeU1() << 24);
    }

    public long takeI8LE() {
        return (takeI4LE() & 0xFFFFFFFFL) | ((long) takeI4LE() << 32);
    }

    public float takeFloatLE() {
        return Float.intBitsToFloat(takeI4LE());
    }

    public double takeDoubleLE() {
        return Double.longBitsToDouble(takeI8LE());
    }

    public byte[] takeBytes(int count) {
        return take(count);
    }

    public String takeUtf8(int count) {
        return new String(takeBytes(count), StandardCharsets.UTF_8);
    }

    /**
     * decode the JVM's modified UTF-8 (CONSTANT_Utf8_info):
     * {@code U+0000} is stored as {@code 0xC0 0x80}, chars above {@code U+FFFF}
     * are stored as UTF-16 surrogate pairs, and 4-byte sequences do not occur.
     */
    public String takeModifiedUtf8(int count) {
        byte[] bytes = takeBytes(count);
        StringBuilder chars = new StringBuilder(count);
        int i = 0;
        while (i < bytes.length) {
            int b = bytes[i] & 0xFF;
            if (b >> 7 == 0) {
                chars.append((char) b);
                i += 1;
            } else if (b >> 5 == 0b110) {
                if (i + 1 >= bytes.length) throw createException("Truncated 2-byte modified utf-8 char", null);
                int b2 = bytes[i + 1] & 0xFF;
                if (b2 >> 6 != 0b10) throw createException("Invalid modified utf-8 continuation byte", null);
                chars.append((char) (((b & 0x1F) << 6) | (b2 & 0x3F)));
                i += 2;
            } else if (b >> 4 == 0b1110) {
                if (i + 2 >= bytes.length) throw createException("Truncated 3-byte modified utf-8 char", null);
                int b2 = bytes[i + 1] & 0xFF;
                int b3 = bytes[i + 2] & 0xFF;
                if (b2 >> 6 != 0b10 || b3 >> 6 != 0b10) throw createException("Invalid modified utf-8 continuation byte", null);
                chars.append((char) (((b & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F)));
                i += 3;
            } else {
                throw createException("Invalid modified utf-8 lead byte", null);
            }
        }
        return chars.toString();
    }

    /* expects */

    public int expect(int b) {
        int it = take();
        if (it != (b & 0xFF)) {
            throw createException("Expected " + (b & 0xFF) + " but got " + (it == -1 ? "EOS" : it), null);
        }
        return b & 0xFF;
    }

    public byte[] expect(byte[] bytes) {
        for (byte b : bytes) {
            expect(b & 0xFF);
        }
        return bytes;
    }

    public void expectEOF() {
        int it = take();
        if (it != -1) throw createException("Expected EOF but got " + it, null);
    }

    public void expectEOS() {
        int it = take();
        if (it != -1) throw createException("Expected EOS but got " + it, null);
    }

    /* parse alternatives */

    public <R> R parse(ElementReader<R> reader) throws ParseException {
        this.mark(Integer.MAX_VALUE);
        try {
            BufferedByteReader br = new BufferedByteReader(this, Integer.MAX_VALUE);
            R ret = reader.apply(br);
            this.reset();
            this.skip(br.position);
            return ret;
        } catch (ParseException e) {
            this.reset();
            throw e;
        }
    }

    @SafeVarargs
    public final <R> R parse(ElementReader<R>... readers) throws ParseException {
        List<ParseException> exceptions = new ArrayList<>();
        for (ElementReader<R> reader : readers) {
            try {
                return parse(reader);
            } catch (ParseException e) {
                exceptions.add(e);
            }
        }
        throw createCompositeException("Failed to parse as any", exceptions.toArray(new ParseException[0]));
    }

    @SafeVarargs
    public final <R> R parse(String type, ElementReader<R>... readers) throws ParseException {
        List<ParseException> exceptions = new ArrayList<>();
        for (ElementReader<R> reader : readers) {
            try {
                return parse(reader);
            } catch (ParseException e) {
                exceptions.add(e);
            }
        }
        throw createCompositeException("Failed to parse as any of " + type, exceptions.toArray(new ParseException[0]));
    }

    @SafeVarargs
    public final <R> R parseOrNull(ElementReader<R>... readers) {
        for (ElementReader<R> reader : readers) {
            try {
                return parse(reader);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    public ParseException createException(String msg, Throwable cause) {
        return ParseException.offset(msg, getPosition(), cause);
    }

    public ParseException createCompositeException(String msg, ParseException... exceptions) {
        ParseException exception = createException(msg, null);
        for (ParseException e : exceptions) {
            exception.addSuppressed(e);
        }
        return exception;
    }

    /**
     * single-method interface (usable as a lambda on Java 8+) for
     * {@link #parse(ElementReader)}.
     */
    public interface ElementReader<R> {
        R apply(ByteReader<?> reader) throws ParseException;
    }

    /**
     * single-method interface (usable as a lambda on Java 8+) byte predicate
     * for {@link #takeUntil(ByteAccepter)}/{@link #takeWhile(ByteAccepter)}.
     */
    public interface ByteAccepter {
        boolean accept(int b);
    }

    /**
     * This wrapping class uses the reader class as a delegate,
     * so don't do multithreaded stuff or interleave using the outer reader
     * with the wrapped reader.
     */
    public static class BufferedByteReader extends ByteReader<BufferedByteReader> implements BackedByteReader {
        private final ByteReader<?> reader;
        private final int limit;
        private byte[] buffer = new byte[64];
        private int buffered = 0;
        int position = 0;
        int mark = 0;

        @Override
        public byte[] getBackingBuffer() {
            return buffer;
        }

        @Override
        public int getBackingPosition() {
            return position;
        }

        public BufferedByteReader(ByteReader<?> reader, int limit) {
            this.reader = reader;
            this.limit = limit;
        }

        private void ensure(int capacity) {
            if (capacity > buffer.length) {
                byte[] grown = new byte[Math.max(capacity, buffer.length * 2)];
                System.arraycopy(buffer, 0, grown, 0, buffered);
                buffer = grown;
            }
        }

        @Override
        public int peek() {
            return peekAt(position);
        }

        public int peekAt(int position) {
            if (position >= limit) {
                return -1;
            }
            if (position > buffered) {
                return -1;
            }
            if (position == buffered) {
                return reader.peek();
            }
            return buffer[position] & 0xFF;
        }

        @Override
        public int take() {
            if (position >= limit) {
                return -1;
            }
            int next = takeAt(position);
            if (next == -1) return -1;
            position++;
            return next;
        }

        public int takeAt(int position) {
            if (position >= limit) {
                return -1;
            }
            if (position > buffered) {
                throw new IllegalStateException("position " + position + " is beyond the buffered frontier " + buffered);
            }
            if (position == buffered) {
                int next = reader.take();
                if (next == -1) return -1;
                ensure(buffered + 1);
                buffer[buffered++] = (byte) next;
                return next;
            }
            return buffer[position] & 0xFF;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public CopyReader copy(int limit) {
            if (limit < 0) throw new IllegalArgumentException("limit < 0");
            limit = position + limit;
            return new CopyReader(position, Math.min(limit < position ? Integer.MAX_VALUE : limit, this.limit));
        }

        @Override
        /**
         * parse is mark-transparent: the caller's mark/reset state is
         * preserved across both success and failure.
         */
        public <R> R parse(ElementReader<R> reader) {
            int savedPosition = position;
            int savedMark = mark;
            try {
                CopyReader br = copy(Integer.MAX_VALUE);
                R ret = reader.apply(br);
                this.position = br.position;
                this.mark = savedMark;
                return ret;
            } catch (ParseException e) {
                this.position = savedPosition;
                this.mark = savedMark;
                throw e;
            }
        }

        @Override
        public void mark(int limit) {
            mark = position;
        }

        @Override
        public void reset() {
            position = mark;
        }

        public class CopyReader extends ByteReader<CopyReader> implements BackedByteReader {
            int position;
            final int limit;
            int mark;

            public CopyReader(int position, int limit) {
                this.position = position;
                this.limit = limit;
                this.mark = position;
            }

            @Override
            public byte[] getBackingBuffer() {
                return buffer;
            }

            @Override
            public int getBackingPosition() {
                return position;
            }

            @Override
            public int peek() {
                if (position >= limit) {
                    return -1;
                }
                return peekAt(position);
            }

            @Override
            public int take() {
                if (position >= limit) {
                    return -1;
                }
                int next = takeAt(position);
                if (next == -1) return -1;
                position++;
                return next;
            }

            @Override
            public int getPosition() {
                return position;
            }

            @Override
            public CopyReader copy(int limit) {
                if (limit < 0) throw new IllegalArgumentException("limit < 0");
                limit = position + limit;
                return new CopyReader(position, Math.min(limit < position ? Integer.MAX_VALUE : limit, this.limit));
            }

            /**
             * parse is mark-transparent: the caller's mark/reset state is
             * preserved across both success and failure.
             */
            @Override
            public <R> R parse(ElementReader<R> reader) {
                int savedPosition = position;
                int savedMark = mark;
                try {
                    CopyReader br = copy(Integer.MAX_VALUE);
                    R ret = reader.apply(br);
                    this.position = br.position;
                    this.mark = savedMark;
                    return ret;
                } catch (ParseException e) {
                    this.position = savedPosition;
                    this.mark = savedMark;
                    throw e;
                }
            }

            @Override
            public void mark(int limit) {
                mark = position;
            }

            @Override
            public void reset() {
                position = mark;
            }

        }

    }

}
