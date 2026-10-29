package xyz.wagyourtail.commons.parsers.impl;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.json5.Json5Comment;
import xyz.wagyourtail.commons.parsers.impl.json5.Json5Value;
import xyz.wagyourtail.commons.parsers.impl.json5.Json5Whitespace;

import java.util.ArrayList;
import java.util.List;

public class Json5File extends RawStringData {

    protected Json5File(String rawContent) {
        super(rawContent);
    }

    private Json5File(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5File(FileContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5File> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return reader.peek() != -1;
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            while (!reader.exhausted()) {
                Object value = reader.parseOrNull(
                        Json5Whitespace.COMPANION::read,
                        Json5Comment.COMPANION::read
                );
                if (value == null) break;
                append.append(value);
            }
            append.append(Json5Value.COMPANION.read(reader));
            while (!reader.exhausted()) {
                Object value = reader.parseOrNull(
                        Json5Whitespace.COMPANION::read,
                        Json5Comment.COMPANION::read
                );
                if (value == null) break;
                append.append(value);
            }
        }

        @Override
        public Json5File unchecked(String value) {
            return new Json5File(value);
        }

        @Override
        public Json5File uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5File(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5File parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static Json5File parse(CharReader<?> reader) {
        return COMPANION.read(reader);
    }

    public Json5Value getValue() {
        return ((FileContent) getContent()).value;
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        final List<Object> list = new ArrayList<>();
        COMPANION.read(reader, new DataCompanion.Append() {
            @Override
            public void append(Object o) {
                list.add(o);
            }
        });
        int vi = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Json5Value) {
                vi = i;
                break;
            }
        }
        return new FileContent(
            new ArrayList<>(list.subList(0, vi)),
            (Json5Value) list.get(vi),
            new ArrayList<>(list.subList(vi + 1, list.size()))
        );
    }

    public static class FileContent extends Content {
        private final List<Object> preContent;
        private final Json5Value value;
        private final List<Object> postContent;

        public FileContent(List<Object> preContent, Json5Value value, List<Object> postContent) {
            this.preContent = preContent;
            this.value = value;
            this.postContent = postContent;
        }

        public List<Object> getPreContent() {
            return preContent;
        }

        public Json5Value getValue() {
            return value;
        }

        public List<Object> getPostContent() {
            return postContent;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            entries.addAll(preContent);
            entries.add(value);
            entries.addAll(postContent);
            return entries;
        }
    }

}
