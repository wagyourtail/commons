package xyz.wagyourtail.commons.parsers;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;

import java.util.function.Function;

public abstract class StringData extends Data<String, Data.Content> {
    private final Function<CharReader<?>, Data.Content> contentBuilder;

    public StringData(String rawContent, Function<CharReader<?>, Data.Content> contentBuilder) {
        super(rawContent);
        this.contentBuilder = contentBuilder;
    }

    public StringData(Data.Content content, Function<CharReader<?>, Data.Content> contentBuilder) {
        super(content);
        this.contentBuilder = contentBuilder;
    }

    @Override
    protected Content buildContent() {
        StringCharReader reader = new StringCharReader(getRawContent());
        val content = contentBuilder.apply(reader);
        reader.expectEOS();
        return content;
    }

    @Override
    protected String buildRawContent() {
        return getContent().toString();
    }

    @Override
    public String toString() {
        return getRawContent();
    }

    public abstract static class OnlyRaw extends StringData {

        public OnlyRaw(String rawContent, Function<CharReader<?>, Data.Content> contentBuilder) {
            super(rawContent, contentBuilder);
        }

        public OnlyRaw(CharReader<?> reader, Function<CharReader<?>, Data.Content> contentBuilder) {
            this(contentBuilder.apply(reader).toString(), contentBuilder);
        }

        @Override
        public Data.Content getContent() {
            return buildContent();
        }

        protected String buildRawContent() {
            throw new IllegalStateException("raw content should always be present");
        }

    }

    public static class BuildStringVisitor implements DataVisitor {
        private final StringBuilder sb = new StringBuilder();

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

        public static String apply(Data<?, ?> data) {
            BuildStringVisitor visitor = new BuildStringVisitor();
            data.accept(visitor);
            return visitor.build();
        }
    }

}
