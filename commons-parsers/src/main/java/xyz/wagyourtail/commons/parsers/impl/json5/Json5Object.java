package xyz.wagyourtail.commons.parsers.impl.json5;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import xyz.wagyourtail.commons.core.CollectionUtils;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.*;

public class Json5Object extends StringData.OnlyParsed<Json5Object.ObjectContent> {


    public Json5Object(ObjectContent content) {
        super(content);
    }

    public static Json5Object parse(String raw) {
        StringCharReader charReader = new StringCharReader(raw);
        val content = contentBuilder(charReader);
        charReader.expectEOS();
        return new Json5Object(content);
    }

    public static Json5Object parse(CharReader<?> charReader) {
        return new Json5Object(contentBuilder(charReader));
    }

    public static ObjectContent contentBuilder(CharReader<?> reader) {
        List<Object> entries = new ArrayList<>();
        Map<String, Json5ObjectEntry> entryMap = new HashMap<>();
        reader.expect('{');
        while (!reader.exhausted()) {
            while (!reader.exhausted()) {
                val value = reader.parseOrNull(
                        Json5Whitespace::new,
                        Json5Comment::new
                );
                if (value == null) break;
                entries.add(value);
            }
            if (reader.peek() == '}') {
                break;
            }

            val entry = Json5ObjectEntry.parse(reader);
            entries.add(entry);
            entryMap.put(entry.getKey(), entry);

            while (!reader.exhausted()) {
                val value = reader.parseOrNull(
                        Json5Whitespace::new,
                        Json5Comment::new
                );
                if (value == null) break;
                entries.add(value);
            }
            if (reader.peek() == ',') {
                entries.add((char) reader.take());
            } else {
                break;
            }
        }
        reader.expect('}');
        return new ObjectContent(entries, entryMap);
    }

    @Getter
    @AllArgsConstructor
    public static class ObjectContent extends Data.Content {
        private final List<Object> entries;
        private final Map<String, Json5ObjectEntry> entryMap;

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

        @Override
        public Iterable<Object> getEntries() {
            return CollectionUtils.concat(
                Collections.singleton('{'),
                entries,
                Collections.singleton('}')
            );
        }
    }

}
