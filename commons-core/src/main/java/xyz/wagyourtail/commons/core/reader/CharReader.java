package xyz.wagyourtail.commons.core.reader;

import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.commons.core.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class CharReader<T extends CharReader<? super T>> {

    public CharReader() {
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
     * @return a copy of the reader at the current position
     */
    public abstract T copy();

    /**
     * set this position as remembered.
     */
    public abstract void mark();

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

    public String takeString() {
        return takeString(true, false);
    }

    public String takeString(boolean leinient) {
        return takeString(leinient, false);
    }

    public String takeString(boolean leinient, boolean escapeDoubleQuote) {
        expect('"');
        StringBuilder sb = new StringBuilder();
        int escapes = 0;
        int next = take();
        while (next != -1) {
            if (next == '"' && escapes == 0) {
                if (escapeDoubleQuote && peek() == '"') {
                    sb.append('\\');
                    sb.append((char) take());
                } else {
                    break;
                }
            }
            if (next == '\\') {
                escapes++;
            } else {
                escapes = 0;
            }
            sb.append((char) next);
            if (escapes == 2) {
                escapes = 0;
            }
            next = take();
        }
        return StringUtils.translateEscapes(sb.toString(), leinient);
    }

    public char expect(char character) {
        int next = take();
        if (next != character) {
            throw new IllegalArgumentException("Expected " + character + " but got " + next);
        }
        return (char) next;
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
            String value = takeString(leinient, true);
            String whiteSpace = takeNonNewlineWhitespace();
            next = peek();
            // check if string actually ends column
            if (next != '\n' && next != -1 && !sep.accept((char) next)) {
                if (!leinient) {
                    throw new IllegalArgumentException("Expected separator char, got " + next);
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

    /* Char Acceptors */

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

    public interface CharAccepter {

        boolean accept(char c);

    }
}
