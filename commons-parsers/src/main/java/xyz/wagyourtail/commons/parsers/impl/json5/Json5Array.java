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

public class Json5Array extends StringData.OnlyParsed<Json5Array.ArrayContent> {


    public Json5Array(ArrayContent content) {
        super(content);
    }

    public static Json5Array parse(String raw) {
        StringCharReader charReader = new StringCharReader(raw);
        val content = contentBuilder(charReader);
        charReader.expectEOS();
        return new Json5Array(content);
    }

    public static Json5Array parse(CharReader<?> charReader) {
        return new Json5Array(contentBuilder(charReader));
    }

    public static ArrayContent contentBuilder(CharReader<?> reader) {
        List<Object> entries = new ArrayList<>();
        List<Json5Value> entryList = new ArrayList<>();

        reader.expect('[');
        while (!reader.exhausted()) {
            while (!reader.exhausted()) {
                val value = reader.parseOrNull(
                        Json5Whitespace::new,
                        Json5Comment::new
                );
                if (value == null) break;
                entries.add(value);
            }
            if (reader.peek() == ']') {
                break;
            }

            val entry = Json5Value.parse(reader);
            entries.add(entry);
            entryList.add(entry);

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
        reader.expect(']');
        return new ArrayContent(entries, entryList);
    }

    @Getter
    @AllArgsConstructor
    public static class ArrayContent extends Data.Content {
        private final List<Object> entries;
        private final List<Json5Value> values;

        public ArrayContent(List<Object> entries) {
            this.entries = entries;
            this.values = new ArrayList<>();
            for (Object entry : entries) {
                if (entry instanceof Json5Value) {
                    values.add((Json5Value) entry);
                }
            }
        }

        @Override
        public Iterable<Object> getEntries() {
            return CollectionUtils.concat(
                    Collections.singleton('['),
                    entries,
                    Collections.singleton(']')
            );
        }
    }

}
