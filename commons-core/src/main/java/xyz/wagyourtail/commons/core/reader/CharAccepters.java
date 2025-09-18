package xyz.wagyourtail.commons.core.reader;

import java.util.Arrays;
import java.util.Objects;

public class CharAccepters {
    public static final CharAccepter WHITESPACE = new CharAccepter() {
        @Override
        public boolean accept(char c) {
            return Character.isWhitespace(c);
        }
    };
    public static final CharAccepter NEWLINE = of('\n');
    public static final CharAccepter NOT_NEWLINE_WHITESPACE = and(WHITESPACE, not(NEWLINE));
    public static final CharAccepter COMMA = of(',');
    public static final CharAccepter ALPHA = new CharAccepter() {
        @Override
        public boolean accept(char c) {
            return Character.isAlphabetic(c);
        }
    };
    public static final CharAccepter DIGIT = new CharAccepter() {
        @Override
        public boolean accept(char c) {
            return Character.isDigit(c);
        }
    };
    public static final CharAccepter ALPHANUMERIC = or(ALPHA, DIGIT);

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

    public static CharAccepter ofAny(final char... characters) {
        Objects.requireNonNull(characters);
        Arrays.sort(characters);
        return new CharAccepter() {

            @Override
            public boolean accept(char c) {
                return Arrays.binarySearch(characters, c) >= 0;
            }

        };
    }
}