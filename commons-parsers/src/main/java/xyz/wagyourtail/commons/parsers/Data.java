package xyz.wagyourtail.commons.parsers;

import lombok.Getter;
import xyz.wagyourtail.commons.core.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    public abstract static class OnlyParsed<T, E extends Content> extends Data<T, E> {

        public OnlyParsed(E content) {
            super(content);
        }

        @Override
        public T getRawContent() {
            return buildRawContent();
        }

        @Override
        protected E buildContent() {
            throw new IllegalStateException("content should always be present");
        }
    }

    public static abstract class Content {

        public abstract Iterable<Object> getEntries();

        @Override
        public String toString() {
            return StringUtils.joinToString("", getEntries());
        }
    }

    @Getter
    public static class SingleContent<T> extends Data.Content {
        private final T value;

        public SingleContent(T content) {
            this.value = content;
        }

        @Deprecated
        public SingleContent(Collection<T> content) {
            if (content.size() != 1) {
                throw new IllegalArgumentException("Content must have exactly one element");
            }
            this.value = content.iterator().next();
        }

        @Override
        public Iterable<Object> getEntries() {
            return Collections.singletonList(value);
        }
    }

    public static class ListContent extends Data.Content {
        private final List<Object> entries;

        public ListContent(List<Object> entries) {
            this.entries = entries;
        }

        @Override
        public Iterable<Object> getEntries() {
            return entries;
        }
    }

}
