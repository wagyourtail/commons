package xyz.wagyourtail.commons.core.reader;

import org.jetbrains.annotations.NotNull;

public class ParseException extends RuntimeException implements Comparable<ParseException> {
    public final int line;
    public final int column;
    public final int byteOffset;

    public ParseException(String message) {
        super(message);
        this.line = 0;
        this.column = 0;
        this.byteOffset = -1;
    }

    public ParseException(String message, int line, int column) {
        super(message + getPosition(line, column));
        this.line = line;
        this.column = column;
        this.byteOffset = -1;
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.line = 0;
        this.column = 0;
        this.byteOffset = -1;
    }

    public ParseException(String message, int line, int column, Throwable cause) {
        super(message + getPosition(line, column), cause);
        this.line = line;
        this.column = column;
        this.byteOffset = -1;
    }

    private ParseException(String message, int line, int column, int byteOffset, Throwable cause) {
        super(message, cause);
        this.line = line;
        this.column = column;
        this.byteOffset = byteOffset;
    }

    /**
     * create a {@link ParseException} positioned at an absolute byte offset —
     * for binary readers where line:column is meaningless.
     */
    public static ParseException offset(String message, int byteOffset) {
        return offset(message, byteOffset, null);
    }

    /**
     * create a {@link ParseException} positioned at an absolute byte offset —
     * for binary readers where line:column is meaningless.
     */
    public static ParseException offset(String message, int byteOffset, Throwable cause) {
        return new ParseException(message + " (at 0x" + Integer.toHexString(byteOffset).toUpperCase() + ")", 0, -1, byteOffset, cause);
    }

    public static String getPosition(int line, int column) {
        if (column == -1) return "";
        if (line == 0) return " (at " + column + ")";
        return " (at " + line + ":" + column + ")";
    }


    @Override
    public int compareTo(@NotNull ParseException o) {
        if (byteOffset != o.byteOffset) return Integer.compare(byteOffset, o.byteOffset);
        if (line != o.line) return Integer.compare(line, o.line);
        if (column != o.column) return Integer.compare(column, o.column);
        return 0;
    }

}
