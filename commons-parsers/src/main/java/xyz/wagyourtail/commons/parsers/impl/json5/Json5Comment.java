package xyz.wagyourtail.commons.parsers.impl.json5;

import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.ArrayList;
import java.util.List;

public class Json5Comment extends StringData.OnlyRaw<Data.ListContent> {
    public Json5Comment(String rawContent) {
        super(rawContent, Json5Comment::getContentChecked);
    }

    public Json5Comment(CharReader<?> reader) {
        super(reader, Json5Comment::getContentChecked);
    }

    public static Json5Comment parse(String reader) {
        StringCharReader charReader = new StringCharReader(reader);
        val content = new Json5Comment(charReader);
        charReader.expectEOS();
        return content;
    }

    public static Json5Comment unchecked(String rawContent) {
        return new Json5Comment(rawContent);
    }

    private static ListContent getContentChecked(CharReader<?> reader) {
        return new ListContent(
                reader.parse(
                    "comment",
                    r -> {
                        List<Object> content = new ArrayList<>();
                        content.add(r.expect("//"));
                        content.add(r.takeUntil("\n"));
                        if (!r.exhausted()) {
                            content.add(r.expect('\n'));
                        }
                        return content;
                    },
                    r -> {
                        List<Object> content = new ArrayList<>();
                        content.add(r.expect("/*"));
                        content.add(r.takeUntil("*/"));
                        content.add(r.expect("*/"));
                        return content;
                    }
                )
        );

    }
}
