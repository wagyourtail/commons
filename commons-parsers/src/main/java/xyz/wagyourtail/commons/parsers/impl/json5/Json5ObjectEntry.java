package xyz.wagyourtail.commons.parsers.impl.json5;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

import java.util.ArrayList;
import java.util.List;

public class Json5ObjectEntry extends RawStringData {

    protected Json5ObjectEntry(String rawContent) {
        super(rawContent);
    }

    private Json5ObjectEntry(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public Json5ObjectEntry(ObjectEntry content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<Json5ObjectEntry> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return Json5ObjectKey.COMPANION.shouldRead(reader);
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(Json5ObjectKey.COMPANION.read(reader));
            while (!reader.exhausted()) {
                Object value = reader.parseOrNull(
                        Json5Whitespace.COMPANION::read,
                        Json5Comment.COMPANION::read
                );
                if (value == null) break;
                append.append(value);
            }
            reader.expect(':');
            append.append(':');
            while (!reader.exhausted()) {
                Object value = reader.parseOrNull(
                        Json5Whitespace.COMPANION::read,
                        Json5Comment.COMPANION::read
                );
                if (value == null) break;
                append.append(value);
            }
            append.append(Json5Value.COMPANION.read(reader));
        }

        @Override
        public Json5ObjectEntry unchecked(String value) {
            return new Json5ObjectEntry(value);
        }

        @Override
        public Json5ObjectEntry uncheckedView(CharSequence buffer, int start, int end) {
            return new Json5ObjectEntry(buffer, start, end);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static Json5ObjectEntry parse(String rawContent) {
        return COMPANION.checked(rawContent);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static Json5ObjectEntry parse(CharReader<?> reader) {
        return COMPANION.read(reader);
    }

    public String getKey() {
        return ((ObjectEntry) getContent()).key.getValue();
    }

    public Json5Value getValue() {
        return ((ObjectEntry) getContent()).value;
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
        int sep = list.indexOf(':');
        return new ObjectEntry(
            (Json5ObjectKey) list.get(0),
            list.subList(1, sep),
            list.subList(sep + 1, list.size() - 1),
            (Json5Value) list.get(list.size() - 1)
        );
    }

    public static class ObjectEntry extends Content {
        private final Json5ObjectKey key;
        private final List<Object> postKey;
        private final List<Object> preValue;
        private final Json5Value value;

        public ObjectEntry(Json5ObjectKey key, List<Object> postKey, List<Object> preValue, Json5Value value) {
            this.key = key;
            this.postKey = postKey;
            this.preValue = preValue;
            this.value = value;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            entries.add(key);
            entries.addAll(postKey);
            entries.add(":");
            entries.addAll(preValue);
            entries.add(value);
            return entries;
        }
    }

}
