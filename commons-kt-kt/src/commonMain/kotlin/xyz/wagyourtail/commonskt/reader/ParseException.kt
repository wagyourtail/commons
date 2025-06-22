package xyz.wagyourtail.commonskt.reader

class ParseException(message: String, val line: Int = 0, val column: Int = 0, cause: Throwable? = null) : RuntimeException(message + getPosition(line, column), cause), Comparable<ParseException> {

    companion object {

        fun getPosition(line: Int, column: Int): String {
            if (column == -1) return ""
            if (line == 0) return " (at $column)"
            return " (at $line:$column)"
        }

    }

    override fun compareTo(other: ParseException): Int {
        if (line != other.line) return line.compareTo(other.line)
        if (column != other.column) return column.compareTo(other.column)
        return 0
    }

}
