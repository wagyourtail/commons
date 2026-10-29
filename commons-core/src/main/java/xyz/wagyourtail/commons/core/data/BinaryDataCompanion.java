package xyz.wagyourtail.commons.core.data;

import xyz.wagyourtail.commons.core.reader.BackedByteReader;
import xyz.wagyourtail.commons.core.reader.ByteArrayByteReader;
import xyz.wagyourtail.commons.core.reader.ByteReader;
import xyz.wagyourtail.commons.core.writer.ByteArrayByteWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link DataCompanion} for binary nodes: {@code R=byte[]},
 * {@code B=byte[]}, {@code RD=ByteReader<?>}.
 */
public abstract class BinaryDataCompanion<T extends StructuredData<byte[]>>
    implements DataCompanion<byte[], byte[], T, ByteReader<?>> {

    @Override
    public T read(ByteReader<?> reader) {
        if (reader instanceof BackedByteReader) {
            BackedByteReader backed = (BackedByteReader) reader;
            int start = backed.getBackingPosition();
            read(reader, new Append() {
                @Override
                public void append(Object o) {
                }
            });
            return uncheckedView(backed.getBackingBuffer(), start, backed.getBackingPosition());
        }
        final ByteArrayByteWriter out = new ByteArrayByteWriter();
        read(reader, new Append() {
            @Override
            public void append(Object o) {
                BinaryData.writeRawPiece(out, o);
            }
        });
        return unchecked(out.toByteArray());
    }

    @Override
    public T uncheckedView(byte[] buffer, int start, int end) {
        return unchecked(Arrays.copyOfRange(buffer, start, end));
    }

    @Override
    public T checked(byte[] value) {
        ByteArrayByteReader reader = new ByteArrayByteReader(value);
        T data = read(reader);
        reader.expectEOS();
        return data;
    }

    @Override
    public Content buildContent(ByteReader<?> reader) {
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
     * {@code (this, ContentKey(raw))}. The array must not be mutated while
     * used as a cache key.
     */
    public <V> V cached(byte[] raw, ParseCache.Compute<V> compute) {
        return ParseCache.getOrPut(this, new ContentKey(raw), compute);
    }

    /**
     * value-equality wrapper so a {@code byte[]} can key {@link ParseCache}.
     * Holds the array by reference — do not mutate it afterwards.
     */
    public static class ContentKey {

        private final byte[] bytes;

        public ContentKey(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ContentKey && Arrays.equals(bytes, ((ContentKey) o).bytes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }

    }

}
