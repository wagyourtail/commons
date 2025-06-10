package xyz.wagyourtail.commons.parsers;

import xyz.wagyourtail.commons.core.StringUtils;

import java.util.Set;

public abstract class Data<T, E extends Data.Content> {
    private volatile T rawContent;
    private volatile E content;

    public Data(T rawContent) {
        this.rawContent = rawContent;
    }

    public Data(E content) {
        this.content = content;
    }

    public T getRawContent() {
        if (rawContent == null) {
            synchronized (this) {
                if (rawContent == null) {
                    rawContent = buildRawContent();
                }
            }
        }
        return rawContent;
    }

    public E getContent() {
        if (content == null) {
            synchronized (this) {
                if (content == null) {
                    content = buildContent();
                }
            }
        }
        return content;
    }

    protected abstract T buildRawContent();

    protected abstract E buildContent();

    @Override
    public String toString() {
        return getContent().toString();
    }

    public void accept(DataVisitor visitor) {
        if (visitor.visit(this)) {
            for (Object o : getContent().getEntries()) {
                if (o instanceof Data<?, ?>) {
                    ((Data<?, ?>) o).accept(visitor);
                } else {
                    visitor.visit(o);
                }
            }
        }
    }

    public interface DataVisitor {

        boolean visit(Object o);

    }

    public abstract static class OnlyRaw<T, E extends Content> extends Data<T, E> {

        public OnlyRaw(T rawContent) {
            super(rawContent);
        }

        @Override
        public E getContent() {
            return buildContent();
        }

        protected T buildRawContent() {
            throw new IllegalStateException("raw content should always be present");
        }

    }

    public static abstract class Content {

        public abstract Set<Object> getEntries();

        @Override
        public String toString() {
            return StringUtils.joinToString("", getEntries());
        }

    }

    public static class DefaultContent extends Data.Content {
        private final Set<Object> entries;

        public DefaultContent(Set<Object> entries) {
            this.entries = entries;
        }

        @Override
        public Set<Object> getEntries() {
            return entries;
        }
    }

}
