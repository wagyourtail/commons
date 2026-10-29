package xyz.wagyourtail.commons.core.data;

import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;

/**
 * {@link StructuredData} over text. Storage is one of:
 * <ul>
 * <li>a known canonical raw string,</li>
 * <li>a {@code (buffer, start, end)} view into a shared backing buffer
 * (zero-copy), or</li>
 * <li>a {@link Content} tree (synthetic; raw materializes via
 * {@link BuildStringVisitor}).</li>
 * </ul>
 *
 * The view constructor is how the companion's {@code uncheckedView} produces
 * range-backed nodes of a concrete type — parse-once laziness and
 * {@link #rawRangeReader()} handle the rest.
 */
public abstract class StringData extends AbstractStructuredData<String> {

    private CharSequence viewBuffer;
    private int viewStart;
    private int viewEnd;

    protected StringData(String rawContent) {
        super(rawContent);
    }

    /**
     * view-backed node over {@code (buffer, start, end)} — no slicing or
     * reparsing until {@link #getRawContent()}/{@link #getContent()} are
     * actually read.
     */
    protected StringData(CharSequence viewBuffer, int start, int end) {
        if (start < 0 || start > end || end > viewBuffer.length()) {
            throw new IllegalArgumentException(
                "invalid view range " + start + ".." + end + " for buffer of length " + viewBuffer.length()
            );
        }
        this.viewBuffer = viewBuffer;
        this.viewStart = start;
        this.viewEnd = end;
    }

    protected StringData(Content content) {
        super(content);
    }

    /**
     * materialize this node's canonical raw — the view range slice for
     * view-backed nodes, or a serialize of the content tree otherwise.
     */
    @Override
    protected String buildRawContent() {
        if (viewBuffer != null) {
            return viewBuffer.subSequence(viewStart, viewEnd).toString();
        }
        return BuildStringVisitor.apply(this);
    }

    /**
     * a reader over exactly this node's raw range — zero-copy for views
     * ({@link StringCharReader} over {@code (buffer, start, end)}), whole
     * string otherwise.
     */
    protected CharReader<?> rawRangeReader() {
        if (viewBuffer != null) {
            return new StringCharReader(viewBuffer, viewStart, viewEnd);
        }
        return new StringCharReader(getRawContent());
    }

    @Override
    public String toString() {
        return getRawContent();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StringData && getRawContent().equals(((StringData) o).getRawContent());
    }

    @Override
    public int hashCode() {
        return getRawContent().hashCode();
    }

}
