package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Json5Object extends RawStringData {

    protected Json5Object(String rawContent) {
        super(rawContent);
    }

    private Json5Object(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5Object(ObjectContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5Object> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return reader.peek() == '{';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            reader.expect('{');
            append.append('{');
            while (!reader.exhausted()) {
                while (!reader.exhausted()) {
                    Object value = reader.parseOrNull(
                            Json5Whitespace.COMPANION::read,
                            Json5Comment.COMPANION::read
                    );
                    if (value == null) break;
                    append.append(value);
                }
                if (reader.peek() == '}') {
                    break;
                }

                append.append(Json5ObjectEntry.COMPANION.read(reader));

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
            reader.expect('}');
            append.append('}');
        }

        @Override
        public Json5Object unchecked(String value) {
            return new Json5Object(value);
        }

        @Override
        public Json5Object uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5Object(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5Object parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static Json5Object parse(CharReader<?> reader) {
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
        return new ObjectContent(new ArrayList<>(list.subList(1, list.size() - 1)));
    }

    public static class ObjectContent extends Content {
        private final List<Object> entries;
        private final Map<String, Json5ObjectEntry> entryMap;

        public ObjectContent(List<Object> entries, Map<String, Json5ObjectEntry> entryMap) {
            this.entries = entries;
            this.entryMap = entryMap;
        }

        public ObjectContent(List<Object> entries) {
            this.entries = entries;
            this.entryMap = new HashMap<>();
            for (Object entry : entries) {
                if (entry instanceof Json5ObjectEntry) {
                    Json5ObjectEntry objectEntry = (Json5ObjectEntry) entry;
                    entryMap.put(objectEntry.getKey(), objectEntry);
                }
            }
        }

        public List<Object> getData() {
            return entries;
        }

        public Map<String, Json5ObjectEntry> getEntryMap() {
            return entryMap;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> result = new ArrayList<>();
            result.add('{');
            result.addAll(entries);
            result.add('}');
            return result;
        }
    }

}
