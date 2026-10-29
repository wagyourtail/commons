package xyz.wagyourtail.commons.core.data;

/**
 * Lazy caching base for {@link StructuredData}: holds the optional
 * raw/content pair, derives missing halves once via {@link #buildRawContent()}
 * /{@link #buildContent()}, and implements {@link #accept}.
 *
 * @param <R> canonical raw type ({@code String} | {@code byte[]})
 */
public abstract class AbstractStructuredData<R> implements StructuredData<R> {

    private volatile R rawContent;
    private volatile Content content;

    protected AbstractStructuredData() {
    }

    protected AbstractStructuredData(R rawContent) {
        this.rawContent = rawContent;
    }

    protected AbstractStructuredData(Content content) {
        this.content = content;
    }

    @Override
    public R getRawContent() {
        R r = rawContent;
        if (r == null) {
            synchronized (this) {
                r = rawContent;
                if (r == null) {
                    rawContent = r = buildRawContent();
                }
            }
        }
        return r;
    }

    @Override
    public Content getContent() {
        Content c = content;
        if (c == null) {
            synchronized (this) {
                c = content;
                if (c == null) {
                    content = c = buildContent();
                }
            }
        }
        return c;
    }

    protected abstract R buildRawContent();

    protected abstract Content buildContent();

    @Override
    public void accept(DataVisitor visitor) {
        if (visitor.visit(this)) {
            for (Object entry : getContent().getEntries()) {
                if (entry instanceof StructuredData) {
                    ((StructuredData<?>) entry).accept(visitor);
                } else {
                    visitor.visit(entry);
                }
            }
        }
        visitor.visitEnd(this);
    }

    @Override
    public abstract String toString();

}
