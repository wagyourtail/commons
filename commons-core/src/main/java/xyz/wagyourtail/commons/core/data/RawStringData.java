package xyz.wagyourtail.commons.core.data;

import xyz.wagyourtail.commons.core.reader.CharReader;

/**
 * A node whose raw is known up front — either a canonical string or a
 * {@code (buffer, start, end)} view. {@link #buildContent()} parses the raw
 * range lazily via {@link #checkedBuildContent(CharReader)} and validates
 * full consumption.
 */
public abstract class RawStringData extends StringData {

    protected RawStringData(String rawContent) {
        super(rawContent);
    }

    protected RawStringData(CharSequence viewBuffer, int start, int end) {
        super(viewBuffer, start, end);
    }

    protected RawStringData(Content content) {
        super(content);
    }

    @Override
    protected final Content buildContent() {
        CharReader<?> reader = rawRangeReader();
        Content content = checkedBuildContent(reader);
        reader.expectEOS();
        return content;
    }

    /**
     * parse this node's content from a reader positioned at (and bounded by)
     * the raw range.
     */
    protected abstract Content checkedBuildContent(CharReader<?> reader);

}
