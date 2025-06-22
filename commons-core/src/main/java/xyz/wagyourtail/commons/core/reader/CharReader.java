package xyz.wagyourtail.commons.core.reader;

import lombok.val;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.commons.core.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class CharReader<T extends CharReader<? super T>> {

    public static final CharAccepter WHITESPACE = new CharAccepter() {
        @Override
        public boolean accept(char c) {
            return Character.isWhitespace(c);
        }
    };
    public static final CharAccepter NEWLINE = new CharAccepter() {
        @Override
        public boolean accept(char c) {
            return c == '\n';
        }
    };
    public static final CharAccepter COMMA = new CharAccepter() {
        @Override
        public boolean accept(char c) {
            return c == ',';
        }
    };
    public static final CharAccepter NOT_NEWLINE_WHITESPACE = and(WHITESPACE, not(NEWLINE));

    public CharReader() {
    }

    public static CharAccepter and(final CharAccepter a, final CharAccepter b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return new CharAccepter() {
            @Override
            public boolean accept(char c) {
                return a.accept(c) && b.accept(c);
            }
        };
    }

    public static CharAccepter or(final CharAccepter a, final CharAccepter b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return new CharAccepter() {
            @Override
            public boolean accept(char c) {
                return a.accept(c) || b.accept(c);
            }
        };
    }

    public static CharAccepter not(final CharAccepter a) {
        Objects.requireNonNull(a);
        return new CharAccepter() {
            @Override
            public boolean accept(char c) {
                return !a.accept(c);
            }
        };
    }

    public static CharAccepter of(final char character) {
        return new CharAccepter() {
            @Override
            public boolean accept(char c) {
                return character == c;
            }
        };
    }

    /**
     * @return either a char, or -1 for eof
     */
    public abstract int peek();

    /**
     * @return either a char, or -1 for eof
     */
    public abstract int take();

    /**
     * @param count the number of chars to skip
     * @return the number of chars skipped
     */
    public int skip(int count) {
        for (int i = 0; i < count; i++) {
            int ch = take();
            if (ch == -1) {
                return i;
            }
        }
        return count;
    }

    /**
     * @return a string of the next count chars
     */
    public String take(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int ch = take();
            if (ch == -1) break;
            builder.append((char) ch);
        }
        return builder.toString();
    }

    /**
     * @return a copy of the reader at the current position
     */
    @Deprecated
    public T copy() {
        return copy(Integer.MAX_VALUE);
    }

    /**
     * @return a copy of the reader at the current position, with a limit to the number of characters it can read.
     * @since 1.0.4
     */
    @Deprecated
    public abstract T copy(int limit);

    /**
     * set this position as remembered.
     */
    public void mark() {
        mark(Integer.MAX_VALUE);
    }

    public abstract void mark(int limit);

    /**
     * reset to remembered mark position, or beginning if no mark.
     */
    public abstract void reset();

    public boolean exhausted() {
        return peek() == -1;
    }

    public String takeRemaining() {
        StringBuilder sb = new StringBuilder();
        while (peek() != -1) {
            sb.append((char) take());
        }
        return sb.toString();
    }

    public String takeLine() {
        return takeUntil('\n');
    }

    public String takeUntil(char character) {
        StringBuilder sb = new StringBuilder();
        int next = peek();
        while (next != -1 && next != character) {
            sb.append((char) take());
            next = peek();
        }
        return sb.toString();
    }

    public String takeUntil(String characters) {
        StringBuilder sb = new StringBuilder();
        int next = peek();
        String taken = "";
        while (next != -1) {
            do {
                if (characters.startsWith(taken + (char) next)) {
                    if (taken.isEmpty()) {
                        mark();
                    }
                    taken += (char) next;
                    break;
                } else if (!taken.isEmpty()) {
                    val newStart = taken.indexOf(characters.charAt(0), 1);
                    if (newStart == -1) {
                        taken = "";
                        if (characters.startsWith(taken + (char) next)) {
                            mark();
                            taken += (char) next;
                        }
                        break;
                    }
                    reset();
                    skip(newStart);
                    mark();
                    taken = taken.substring(newStart);
                    skip(taken.length());
                }
            } while (!taken.isEmpty());
            sb.append((char) take());
            if (characters.equals(taken)) {
                val out = StringUtils.removeSuffix(sb.toString(), characters);
                reset();
                return out;
            }
            next = peek();
        }
        return sb.toString();
    }

    public String takeUntil(CharAccepter accepter) {
        Objects.requireNonNull(accepter);
        StringBuilder sb = new StringBuilder();
        int next = peek();
        while (next != -1 && !accepter.accept((char) next)) {
            sb.append((char) take());
            next = peek();
        }
        return sb.toString();
    }

    public String takeWhile(CharAccepter accepter) {
        Objects.requireNonNull(accepter);
        StringBuilder sb = new StringBuilder();
        int next = peek();
        while (next != -1 && accepter.accept((char) next)) {
            sb.append((char) take());
            next = peek();
        }
        return sb.toString();
    }

    public String takeWhitespace() {
        return takeWhile(WHITESPACE);
    }

    public String takeNonNewlineWhitespace() {
        return takeWhile(NOT_NEWLINE_WHITESPACE);
    }

    public @Nullable String takeNext() {
        return takeNext(WHITESPACE);
    }

    public @Nullable String takeNext(char sep) {
        int next = peek();
        if (next == -1 || next == '\n') return null;
        String value;
        if (next == '"') value = takeString();
        else {
            StringBuilder sb = new StringBuilder();
            while (next != -1) {
                if (next == '\n' || next == sep) break;
                sb.append((char) take());
                next = peek();
            }
            value = sb.toString();
        }
        // take trailing sep
        next = peek();
        if (next == sep) {
            take();
        }
        return value;
    }

    public @Nullable String takeNext(CharAccepter sep) {
        int next = peek();
        if (next == -1 || next == '\n') return null;
        String value;
        if (next == '"') value = takeString();
        else value = takeUntil(or(sep, NEWLINE));
        // take trailing sep
        next = peek();
        if (next != -1 && next != '\n' && sep.accept((char) next)) {
            take();
        }
        return value;
    }

    public @Nullable String takeNextLiteral() {
        return takeNextLiteral(WHITESPACE);
    }

    public @Nullable String takeNextLiteral(char sep) {
        int next = peek();
        if (next == -1 || next == '\n') return null;
        StringBuilder sb = new StringBuilder();
        while (next != -1) {
            if (next == '\n' || next == sep) break;
            sb.append((char) take());
            next = peek();
        }
        // take trailing sep
        if (next == sep) {
            take();
        }
        return sb.toString();
    }

    public @Nullable String takeNextLiteral(CharAccepter sep) {
        int next = peek();
        if (next == -1 || next == '\n') return null;
        StringBuilder sb = new StringBuilder();
        while (next != -1) {
            if (next == '\n' || sep.accept((char) next)) break;
            sb.append((char) take());
            next = peek();
        }
        // take trailing sep
        if (next != -1 && next != '\n' && sep.accept((char) next)) {
            take();
        }
        return sb.toString();
    }

    public List<String> takeRemainingOnLine() {
        return takeRemainingOnLine(WHITESPACE);
    }

    public List<String> takeRemainingOnLine(char sep) {
        List<String> args = new ArrayList<>();
        String next = takeNext(sep);
        while (next != null) {
            args.add(next);
            next = takeNext(sep);
        }
        return args;
    }

    public List<String> takeRemainingOnLine(CharAccepter sep) {
        List<String> args = new ArrayList<>();
        String next = takeNext(sep);
        while (next != null) {
            args.add(next);
            next = takeNext(sep);
        }
        return args;
    }

    public List<String> takeRemainingLiteralOnLine() {
        return takeRemainingOnLine(WHITESPACE);
    }

    public List<String> takeRemainingLiteralOnLine(char sep) {
        List<String> args = new ArrayList<>();
        String next = takeNextLiteral(sep);
        while (next != null) {
            args.add(next);
            next = takeNextLiteral(sep);
        }
        return args;
    }

    public List<String> takeRemainingLiteralOnLine(CharAccepter sep) {
        List<String> args = new ArrayList<>();
        String next = takeNextLiteral(sep);
        while (next != null) {
            args.add(next);
            next = takeNextLiteral(sep);
        }
        return args;
    }

    public static final int TAKE_STRING_LEINIENT             = 0b1;
    public static final int TAKE_STRING_ESCAPE_DOUBLE_QUOTE  = 0b10;
    public static final int TAKE_STRING_ESCAPE_NEWLINE       = 0b100;
    public static final int TAKE_STRING_MULTILINE            = 0b1000;
    public static final int TAKE_STRING_NO_START_QUOTE       = 0b10000;
    public static final int TAKE_STRING_NO_TRANSLATE_ESCAPES = 0b100000;

    public String takeString() {
        return takeString(TAKE_STRING_LEINIENT, "\"");
    }

    @Deprecated
    public String takeString(boolean leinient) {
        return takeString((leinient ? TAKE_STRING_LEINIENT : 0), "\"");
    }

    @Deprecated
    public String takeString(boolean leinient, boolean escapeDoubleQuote) {
        return takeString( (leinient ? TAKE_STRING_LEINIENT : 0) | (escapeDoubleQuote ? TAKE_STRING_ESCAPE_DOUBLE_QUOTE : 0), "\"");
    }

    @Deprecated
    public String takeString(boolean leinient, boolean escapeDoubleQuote, char quote) {
        return takeString((leinient ? TAKE_STRING_LEINIENT : 0) | (escapeDoubleQuote ? TAKE_STRING_ESCAPE_DOUBLE_QUOTE : 0), String.valueOf(quote));
    }

    public String takeString(int flags, String quote) {
        if ((flags & TAKE_STRING_NO_START_QUOTE) == 0) expect(quote);
        StringBuilder sb = new StringBuilder();
        while (!exhausted()) {
            String[] lines = takeUntil(quote).split("\r?\n", -1);
            String last = lines[lines.length - 1];
            if ((flags & (TAKE_STRING_MULTILINE | TAKE_STRING_ESCAPE_NEWLINE)) != 0) {
                for (int i = 0; i < lines.length - 1; i++) {
                    String next = lines[i];
                    if ((flags & TAKE_STRING_ESCAPE_NEWLINE) != 0) {
                        // count ending \'s
                        int count = 0;
                        for (int j = next.length() - 1; j >= 0; j--) {
                            if (next.charAt(j) == '\\') {
                                count++;
                            } else {
                                break;
                            }
                        }
                        if (count % 2 != 0) {
                            // escaped
                            if ((flags & TAKE_STRING_NO_TRANSLATE_ESCAPES) == 0) {
                                next = next.substring(0, next.length() - 1);
                            }
                            sb.append(next);
                            if ((flags & TAKE_STRING_NO_TRANSLATE_ESCAPES) != 0) {
                                sb.append('\n');
                            }
                        } else if (((flags & TAKE_STRING_MULTILINE) != 0)) {
                            sb.append(next).append('\n');
                        } else {
                            throw createException("Unexpected EOL in string literal");
                        }
                    } else {
                        sb.append(next).append('\n');
                    }
                }
            } else if (lines.length > 1) {
                throw createException("Unexpected EOL in string literal");
            }

            // count ending \'s
            int count = 0;
            for (int i = last.length() - 1; i >= 0; i--) {
                if (last.charAt(i) == '\\') {
                    count++;
                } else {
                    break;
                }
            }
            if (count % 2 != 0) {
                // escaped
                if ((flags & TAKE_STRING_NO_TRANSLATE_ESCAPES) == 0) {
                    last = last.substring(0, last.length() - 1);
                }
                sb.append(last);
                sb.append(expect(quote));
            } else {
                sb.append(last);
                if ((flags & TAKE_STRING_ESCAPE_DOUBLE_QUOTE) != 0) {
                    mark();
                    expect(quote);
                    try {
                        sb.append(expect(quote));
                        if ((flags & TAKE_STRING_NO_TRANSLATE_ESCAPES) != 0) {
                            sb.append(quote);
                        }
                    } catch (ParseException e) {
                        reset();
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        expect(quote);
        if ((flags & TAKE_STRING_NO_TRANSLATE_ESCAPES) != 0) return quote + sb + quote;
        return StringUtils.translateEscapes(sb.toString(), (flags & TAKE_STRING_LEINIENT) != 0);
    }

    /* Char Acceptors */

    public char expect(char character) {
        return expect(character, false);
    }

    public char expect(char character, boolean ignoreCase) {
        int next = take();
        if (next == -1) throw createException("Expected " + character + " but got EOS");
        if (ignoreCase) {
            if (Character.toLowerCase((char) next) != Character.toLowerCase(character)) {
                throw createException("Expected " + character + " but got " + (char) next);
            }
        } else {
            if ((char) next != character) {
                throw createException("Expected " + character + " but got " + (char) next);
            }
        }
        return (char) next;
    }

    public char expect(String valueType, CharAccepter accepter) {
        int next = take();
        if (next == -1) throw createException("Expected " + valueType + " but got EOS");
        if (!accepter.accept((char) next)) {
            throw createException("Expected " + valueType + " but got " + (char) next);
        }
        return (char) next;
    }

    public String expect(String value) {
        return expect(value, false);
    }

    public String expect(String value, boolean ignoreCase) {
        for (char c : value.toCharArray()) {
            expect(c, ignoreCase);
        }
        return value;
    }

    public void expectEOF() {
        val c = take();
        if (c != -1) {
            throw createException("Expected EOF but got " + (char) c);
        }
    }

    public void expectEOS() {
        val c = take();
        if (c != -1) {
            throw createException("Expected EOS but got " + (char) c);
        }
    }

    /* CSV Specific */
    public List<String> takeRemainingCol() {
        return takeRemainingCol(true, COMMA);
    }

    public List<String> takeRemainingCol(boolean leinient) {
        return takeRemainingCol(leinient, COMMA);
    }

    public List<String> takeRemainingCol(boolean leinient, CharAccepter sep) {
        List<String> cols = new ArrayList<>();
        String next = takeCol(leinient, sep);
        while (next != null) {
            cols.add(next);
            next = takeCol(leinient, sep);
        }
        return cols;
    }

    public String takeCol() {
        return takeCol(true, COMMA);
    }

    public String takeCol(boolean leinient) {
        return takeCol(leinient, COMMA);
    }

    public @Nullable String takeCol(boolean leinient, CharAccepter sep) {
        int next = peek();
        if ((next == -1 || next == '\n')) {
            return null;
        }
        if (next == '"') {
            String value = takeString(TAKE_STRING_ESCAPE_DOUBLE_QUOTE | (leinient ? TAKE_STRING_LEINIENT : 0), "\"");
            String whiteSpace = takeNonNewlineWhitespace();
            next = peek();
            // check if string actually ends column
            if (next != '\n' && next != -1 && !sep.accept((char) next)) {
                if (!leinient) {
                    throw createException("Expected separator char, got " + next);
                }
                return value + whiteSpace + takeCol(true, sep);
            }
            // check if next is sep char and take it
            next = peek();
            if (next != -1 && sep.accept((char) next)) {
                take();
            }
            return value;
        }
        String value = takeUntil(or(sep, NEWLINE));
        next = peek();
        // check if next is sep char and take it
        if (sep.accept((char) next)) {
            take();
        }
        return value;
    }

    public <R> R parse(StringCharReader.ElementReader<R> reader) {
        this.mark();
        try {
            return reader.read(new WrappingReader(this, Integer.MAX_VALUE));
        } catch (ParseException t) {
            this.reset();
            throw t;
        }
    }

    @SafeVarargs
    public final <R> R parse(StringCharReader.ElementReader<R>... readers) {
        List<ParseException> exceptions = new ArrayList<>();
        for (StringCharReader.ElementReader<R> reader : readers) {
            try {
                return parse(reader);
            } catch (ParseException e) {
                exceptions.add(e);
            }
        }
        throw createCompositeException("Failed to parse as any", exceptions.toArray(new ParseException[0]));
    }

    @SafeVarargs
    public final <R> R parse(String type, StringCharReader.ElementReader<R>... readers) {
        List<ParseException> exceptions = new ArrayList<>();
        for (StringCharReader.ElementReader<R> reader : readers) {
            try {
                return parse(reader);
            } catch (ParseException e) {
                exceptions.add(e);
            }
        }
        throw createCompositeException("Failed to parse as any of " + type, exceptions.toArray(new ParseException[0]));
    }

    @Nullable
    @SafeVarargs
    public final <R> R parseOrNull(StringCharReader.ElementReader<R>... readers) {
        for (StringCharReader.ElementReader<R> reader : readers) {
            try {
                return parse(reader);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    public ParseException createException(String message) {
        return createException(message, null);
    }

    public ParseException createException(String message, Throwable cause) {
        return new ParseException(message, cause);
    }

    public ParseException createCompositeException(String message, ParseException... exceptions) {
        val exception = createException(message);
        for (ParseException e : exceptions) {
            exception.addSuppressed(e);
        }
        return exception;
    }

    public interface ElementReader<R> {

        R read(CharReader<?> reader) throws ParseException;

    }

    public interface CharAccepter {
        boolean accept(char c);
    }

    /**
     * This wrapping class uses the reader class as a delegate,
     * so don't do multithreaded stuff, or interleave using the outer reader
     * with the wrapped reader.
     */
    public static class WrappingReader extends CharReader<WrappingReader> {
        private final StringBuilder sb = new StringBuilder();
        private final CharReader<?> reader;
        private final int limit;
        private int position = 0;
        private int mark = 0;

        public WrappingReader(CharReader<?> reader, int limit) {
            this.reader = reader;
            this.limit = limit;
        }

        @Override
        public int peek() {
            if (position == limit) {
                return -1;
            }
            if (position >= sb.length()) {
                int next = reader.take();
                if (next == -1) return -1;
                sb.append((char) next);
                return next;
            }
            return sb.charAt(position);
        }

        @Override
        public int take() {
            if (position == limit) {
                return -1;
            }
            if (position >= sb.length()) {
                int next = reader.take();
                position++;
                if (next == -1) return -1;
                sb.append((char) next);
                return next;
            }
            return sb.charAt(position++);
        }

        @Override
        public WrappingReader copy() {
            return new WrappingReader(reader, limit - position);
        }

        @Override
        public WrappingReader copy(int limit) {
            return new WrappingReader(reader, limit);
        }

        @Override
        public void mark(int limit) {
            mark = position;
        }

        @Override
        public void reset() {
            position = mark;
        }

        public String getAllRead() {
            return sb.toString();
        }
    }

}
