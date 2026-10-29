package xyz.wagyourtail.commons.core.data;

import xyz.wagyourtail.commons.core.IteratorUtils;
import xyz.wagyourtail.commons.core.StringUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The ordered children of a {@link StructuredData} node. Entries are
 * arbitrary values: child nodes, raw slices, or literals (the concrete
 * grammar decides).
 */
public abstract class Content {

    public abstract Iterable<Object> getEntries();

    @Override
    public String toString() {
        return StringUtils.joinToString("", getEntries());
    }

    /**
     * shared empty content for leaf nodes with no children.
     */
    public static final Content EMPTY = new EmptyContent();

    /**
     * {@link Content} for a leaf node with no children.
     */
    public static class EmptyContent extends Content {

        @Override
        public Iterable<Object> getEntries() {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return "";
        }

    }

    /**
     * {@link Content} holding a single entry.
     */
    public static class SingleContent extends Content {

        private final Object value;

        public SingleContent(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public Iterable<Object> getEntries() {
            return Collections.singletonList(value);
        }

    }

    /**
     * {@link Content} holding a list of entries.
     */
    public static class ListContent extends Content {

        private final List<Object> entries;

        public ListContent(List<Object> entries) {
            this.entries = entries;
        }

        @Override
        public Iterable<Object> getEntries() {
            return entries;
        }

    }

    /**
     * {@link Content} that interleaves {@code delimiter} between each entry.
     */
    public static class ListContentWithDelimiter extends Content {

        private final List<Object> content;
        private final Object delimiter;

        public ListContentWithDelimiter(List<Object> content, Object delimiter) {
            this.content = content;
            this.delimiter = delimiter;
        }

        @Override
        public Iterable<Object> getEntries() {
            return new Iterable<Object>() {
                @Override
                public Iterator<Object> iterator() {
                    return IteratorUtils.withDelimiter(content.iterator(), delimiter);
                }
            };
        }

        public List<Object> getEntriesWithoutDelimiters() {
            return content;
        }

    }

}
