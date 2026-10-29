package xyz.wagyourtail.commons.core.data;

/**
 * A synthetic node built from a {@link Content} tree; {@link #getRawContent()}
 * serializes lazily via {@link BuildStringVisitor}.
 */
public abstract class ParsedStringData extends StringData {

    protected ParsedStringData(Content content) {
        super(content);
    }

    @Override
    protected final Content buildContent() {
        throw new IllegalStateException("content is always present");
    }

}
