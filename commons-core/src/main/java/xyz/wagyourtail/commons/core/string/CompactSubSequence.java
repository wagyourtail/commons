package xyz.wagyourtail.commons.core.string;

import org.jetbrains.annotations.NotNull;

/**
 * A implementation of CharSequence. designed to be used in memory-efficient contexts by not duplicating strings.
 */
public class CompactSubSequence implements CharSequence {
    private final CharSequence backing;
    private final int start;
    private final int end;

    public CompactSubSequence(CharSequence backing) {
        this(backing, 0, backing.length());
    }

    public CompactSubSequence(CharSequence backing, int start, int end) {
        this.backing = backing;
        if (start < 0 || start > backing.length() || end < 0 || end > backing.length() || start > end) {
            throw new IndexOutOfBoundsException("Invalid subsequence range: [" + start + ", " + end + "]");
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }
        return backing.charAt(start + index);
    }

    @NotNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return new CompactSubSequence(backing, this.start + start, this.start + end);
    }

    @NotNull
    @Override
    public String toString() {
        return backing.subSequence(start, end).toString();
    }
}
