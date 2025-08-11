package xyz.wagyourtail.commons.parsers;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;

import java.util.function.Function;

public abstract class StringData<E extends Data.Content> extends Data<String, E> {

    public StringData(String rawContent) {
        super(rawContent);
    }

    public StringData(E content) {
        super(content);
    }

    @Override
    protected String buildRawContent() {
        return getContent().toString();
    }

    @Override
    public String toString() {
        return getRawContent();
    }

    public abstract static class OnlyRaw<E extends Data.Content> extends StringData<E> {
        private final Function<CharReader<?>, E> contentBuilder;

        public OnlyRaw(String rawContent, Function<CharReader<?>, E> contentBuilder) {
            super(rawContent);
            this.contentBuilder = contentBuilder;
        }

        public OnlyRaw(CharReader<?> reader, Function<CharReader<?>, E> contentBuilder) {
            this(contentBuilder.apply(reader).toString(), contentBuilder);
        }

        @Override
        public E getContent() {
            return buildContent();
        }

        @Override
        protected E buildContent() {
            StringCharReader reader = new StringCharReader(getRawContent());
            val content = contentBuilder.apply(reader);
            reader.expectEOS();
            return content;
        }

        protected String buildRawContent() {
            throw new IllegalStateException("raw content should always be present");
        }
    }

    public abstract static class OnlyParsed<E extends Data.Content> extends StringData<E> {

        public OnlyParsed(E content) {
            super(content);
        }

        protected E buildContent() {
            throw new IllegalStateException("content should always be present");
        }
    }

    public static class BuildStringVisitor implements DataVisitor {
        private final StringBuilder sb = new StringBuilder();

        public static String apply(Data<?, ?> data) {
            BuildStringVisitor visitor = new BuildStringVisitor();
            data.accept(visitor);
            return visitor.build();
        }

        @Override
        public boolean visit(Object o) {
            if (!(o instanceof Data<?, ?>)) {
                sb.append(o);
            }
            return true;
        }

        public String build() {
            return sb.toString();
        }
    }

}
