package xyz.wagyourtail.commonskt.reader

class ParseException : RuntimeException, Comparable<ParseException> {

    val line: Int
    val column: Int
    val byteOffset: Int

    constructor(message: String, line: Int = 0, column: Int = 0, cause: Throwable? = null) :
        super(message + getPosition(line, column, -1), cause) {
        this.line = line
        this.column = column
        this.byteOffset = -1
    }

    private constructor(message: String, line: Int, column: Int, byteOffset: Int, cause: Throwable?) :
        super(message + getPosition(line, column, byteOffset), cause) {
        this.line = line
        this.column = column
        this.byteOffset = byteOffset
    }

    companion object {

        /**
         * create a [ParseException] positioned at an absolute byte offset —
         * for binary readers where line:column is meaningless.
         */
        fun offset(message: String, byteOffset: Int, cause: Throwable? = null) =
            ParseException(message, 0, -1, byteOffset, cause)

        fun getPosition(line: Int, column: Int): String {
            return getPosition(line, column, -1)
        }

        fun getPosition(line: Int, column: Int, byteOffset: Int): String {
            if (byteOffset >= 0) return " (at 0x${byteOffset.toString(16).uppercase()})"
            if (column == -1) return ""
            if (line == 0) return " (at $column)"
            return " (at $line:$column)"
        }

    }

    override fun compareTo(other: ParseException): Int {
        if (byteOffset != other.byteOffset) return byteOffset.compareTo(other.byteOffset)
        if (line != other.line) return line.compareTo(other.line)
        if (column != other.column) return column.compareTo(other.column)
        return 0
    }

}
