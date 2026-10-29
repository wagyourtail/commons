package xyz.wagyourtail.commons.core.data;

/**
 * A synthetic node built from a {@link Content} tree; {@link #getRawContent()}
 * serializes lazily via {@link BuildBytesVisitor}.
 */
public abstract class ParsedBinaryData extends BinaryData {

    protected ParsedBinaryData(Content content) {
        super(content);
    }

    @Override
    protected final Content buildContent() {
        throw new IllegalStateException("content is always present");
    }

}
