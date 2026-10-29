package xyz.wagyourtail.commons.core.data;

import xyz.wagyourtail.commons.core.reader.ByteReader;

/**
 * A node whose raw is known up front — either a canonical array or a
 * {@code (buffer, start, end)} view. {@link #buildContent()} parses the raw
 * range lazily via {@link #checkedBuildContent(ByteReader)} and validates
 * full consumption.
 */
public abstract class RawBinaryData extends BinaryData {

    protected RawBinaryData(byte[] rawContent) {
        super(rawContent);
    }

    protected RawBinaryData(byte[] viewBuffer, int start, int end) {
        super(viewBuffer, start, end);
    }

    protected RawBinaryData(Content content) {
        super(content);
    }

    @Override
    protected final Content buildContent() {
        ByteReader<?> reader = rawRangeReader();
        Content content = checkedBuildContent(reader);
        reader.expectEOS();
        return content;
    }

    /**
     * parse this node's content from a reader positioned at (and bounded by)
     * the raw range.
     */
    protected abstract Content checkedBuildContent(ByteReader<?> reader);

}
