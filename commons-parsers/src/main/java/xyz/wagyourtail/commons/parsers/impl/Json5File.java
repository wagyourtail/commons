package xyz.wagyourtail.commons.parsers.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import xyz.wagyourtail.commons.core.CollectionUtils;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;
import xyz.wagyourtail.commons.parsers.impl.json5.Json5Comment;
import xyz.wagyourtail.commons.parsers.impl.json5.Json5Value;
import xyz.wagyourtail.commons.parsers.impl.json5.Json5Whitespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Json5File extends StringData.OnlyParsed<Json5File.FileContent> {

    public Json5File(Json5File.FileContent content) {
        super(content);
    }

    public static Json5File parse(String reader) {
        StringCharReader charReader = new StringCharReader(reader);
        val content = contentBuilder(charReader);
        charReader.expectEOS();
        return new Json5File(content);
    }

    public static Json5File parse(CharReader<?> reader) {
        return new Json5File(contentBuilder(reader));
    }

    public static FileContent contentBuilder(CharReader<?> reader) {
        List<Object> preContent = new ArrayList<>();
        while (!reader.exhausted()) {
            val preValue = reader.parseOrNull(
                    Json5Whitespace::new,
                    Json5Comment::new
            );
            if (preValue == null) break;
            preContent.add(preValue);
        }

        Json5Value value = Json5Value.parse(reader);

        List<Object> postContent = new ArrayList<>();
        while (!reader.exhausted()) {
            val postValue = reader.parseOrNull(
                    Json5Whitespace::new,
                    Json5Comment::new
            );
            if (postValue == null) break;
            postContent.add(postValue);
        }
        return new FileContent(preContent, value, postContent);
    }

    public Json5Value getValue() {
        return getContent().getValue();
    }

    @Getter
    @AllArgsConstructor
    public static class FileContent extends Data.Content {
        private final List<Object> preContent;
        private final Json5Value value;
        private final List<Object> postContent;

        @Override
        public Iterable<Object> getEntries() {
            return CollectionUtils.concat(
                    preContent,
                    Collections.singleton(value),
                    postContent
            );
        }
    }


}
