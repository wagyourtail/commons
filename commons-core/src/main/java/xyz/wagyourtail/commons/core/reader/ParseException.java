package xyz.wagyourtail.commons.core.reader;

import org.jetbrains.annotations.NotNull;

public class ParseException extends RuntimeException implements Comparable<ParseException> {
    final int line;
    final int column;

    public ParseException(String message) {
        super(message);
        this.line = 0;
        this.column = 0;
    }

    public ParseException(String message, int line, int column) {
        super(message + getPosition(line, column));
        this.line = line;
        this.column = column;
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.line = 0;
        this.column = 0;
    }

    public ParseException(String message, int line, int column, Throwable cause) {
        super(message + getPosition(line, column), cause);
        this.line = line;
        this.column = column;
    }

    public static String getPosition(int line, int column) {
        if (column == -1) return "";
        if (line == 0) return " (at " + column + ")";
        return " (at " + line + ":" + column + ")";
    }


    @Override
    public int compareTo(@NotNull ParseException o) {
        if (line != o.line) return Integer.compare(line, o.line);
        if (column != o.column) return Integer.compare(column, o.column);
        return 0;
    }

}
