package xyz.wagyourtail.commons.core.data;

import xyz.wagyourtail.commons.core.reader.BackedCharReader;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DataCompanion} for text nodes: {@code R=String},
 * {@code B=CharSequence}, {@code RD=CharReader<?>}.
 */
public abstract class StringDataCompanion<T extends StructuredData<String>>
    implements DataCompanion<String, CharSequence, T, CharReader<?>> {

    @Override
    public T read(CharReader<?> reader) {
        if (reader instanceof BackedCharReader) {
            BackedCharReader backed = (BackedCharReader) reader;
            int start = backed.getBackingPosition();
            read(reader, new Append() {
                @Override
                public void append(Object o) {
                }
            });
            return uncheckedView(backed.getBackingBuffer(), start, backed.getBackingPosition());
        }
        final StringBuilder sb = new StringBuilder();
        read(reader, new Append() {
            @Override
            public void append(Object o) {
                sb.append(o);
            }
        });
        return unchecked(sb.toString());
    }

    @Override
    public T uncheckedView(CharSequence buffer, int start, int end) {
        return unchecked(buffer.subSequence(start, end).toString());
    }

    @Override
    public T checked(String value) {
        StringCharReader reader = new StringCharReader(value);
        T data = read(reader);
        reader.expectEOS();
        return data;
    }

    @Override
    public Content buildContent(CharReader<?> reader) {
        final List<Object> entries = new ArrayList<Object>();
        read(reader, new Append() {
            @Override
            public void append(Object o) {
                entries.add(o);
            }
        });
        reader.expectEOS();
        return new Content.ListContent(entries);
    }

    /**
     * {@link ParseCache}-backed derived-parts memoization keyed
     * {@code (this, raw)} — the atomic-leaf story for types that cannot
     * store per-instance state.
     */
    public <V> V cached(String raw, ParseCache.Compute<V> compute) {
        return ParseCache.getOrPut(this, raw, compute);
    }

}
