package xyz.wagyourtail.commons.parsers.impl.json5;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import xyz.wagyourtail.commons.core.CollectionUtils;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Json5ObjectEntry extends StringData.OnlyParsed<Json5ObjectEntry.ObjectEntry> {


    public Json5ObjectEntry(ObjectEntry content) {
        super(content);
    }

    public static Json5ObjectEntry parse(String raw) {
        StringCharReader charReader = new StringCharReader(raw);
        val content = contentBuilder(charReader);
        charReader.expectEOS();
        return new Json5ObjectEntry(content);
    }

    public static Json5ObjectEntry parse(CharReader<?> charReader) {
        return new Json5ObjectEntry(contentBuilder(charReader));
    }

    public static ObjectEntry contentBuilder(CharReader<?> reader) {
        Json5ObjectKey key = new Json5ObjectKey(reader);
        List<Object> postKey = new ArrayList<>();
        while (!reader.exhausted()) {
            val value = reader.parseOrNull(
                    Json5Whitespace::new,
                    Json5Comment::new
            );
            if (value == null) break;
            postKey.add(value);
        }
        reader.expect(':');
        List<Object> preValue = new ArrayList<>();
        while (!reader.exhausted()) {
            val value = reader.parseOrNull(
                    Json5Whitespace::new,
                    Json5Comment::new
            );
            if (value == null) break;
            preValue.add(value);
        }
        Json5Value value = Json5Value.parse(reader);
        return new ObjectEntry(key, postKey, preValue, value);
    }

    public String getKey() {
        return getContent().getKey().getValue();
    }

    public Json5Value getValue() {
        return getContent().getValue();
    }

    @Getter
    @AllArgsConstructor
    public static class ObjectEntry extends Data.Content {
        private final Json5ObjectKey key;
        private final List<Object> postKey;
        private final List<Object> preValue;
        private final Json5Value value;

        @Override
        public Iterable<Object> getEntries() {
            return CollectionUtils.concat(
                    Collections.singleton(key),
                    postKey,
                    Collections.singleton(":"),
                    preValue,
                    Collections.singleton(value)
            );
        }

    }

}
