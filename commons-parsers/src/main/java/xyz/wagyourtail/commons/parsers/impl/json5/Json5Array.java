package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

import java.util.ArrayList;
import java.util.List;

public class Json5Array extends RawStringData {

    protected Json5Array(String rawContent) {
        super(rawContent);
    }

    private Json5Array(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5Array(ArrayContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5Array> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return reader.peek() == '[';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            reader.expect('[');
            append.append('[');
            while (!reader.exhausted()) {
                while (!reader.exhausted()) {
                    Object value = reader.parseOrNull(
                            Json5Whitespace.COMPANION::read,
                            Json5Comment.COMPANION::read
                    );
                    if (value == null) break;
                    append.append(value);
                }
                if (reader.peek() == ']') {
                    break;
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
                if (reader.peek() == ',') {
                    append.append((char) reader.take());
                } else {
                    break;
                }
            }
            reader.expect(']');
            append.append(']');
        }

        @Override
        public Json5Array unchecked(String value) {
            return new Json5Array(value);
        }

        @Override
        public Json5Array uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5Array(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5Array parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static Json5Array parse(CharReader<?> reader) {
        return COMPANION.read(reader);
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
        List<Object> data = list.subList(1, list.size() - 1);
        List<Json5Value> values = new ArrayList<>();
        for (Object o : data) {
            if (o instanceof Json5Value) values.add((Json5Value) o);
        }
        return new ArrayContent(new ArrayList<>(data), values);
    }

    public static class ArrayContent extends Content {
        private final List<Object> entries;
        private final List<Json5Value> values;

        public ArrayContent(List<Object> entries, List<Json5Value> values) {
            this.entries = entries;
            this.values = values;
        }

        public ArrayContent(List<Object> entries) {
            this.entries = entries;
            this.values = new ArrayList<>();
            for (Object entry : entries) {
                if (entry instanceof Json5Value) {
                    values.add((Json5Value) entry);
                }
            }
        }

        public List<Object> getData() {
            return entries;
        }

        public List<Json5Value> getValues() {
            return values;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> result = new ArrayList<>();
            result.add('[');
            result.addAll(entries);
            result.add(']');
            return result;
        }
    }

}
