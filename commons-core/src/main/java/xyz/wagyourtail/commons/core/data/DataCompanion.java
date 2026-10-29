package xyz.wagyourtail.commons.core.data;

/**
 * The grammar/constructor half of a {@link StructuredData} type — UML's
 * {@code TypeCompanion} shape generalized over raw type, buffer type, and
 * reader.
 *
 * @param <R>  canonical raw type ({@code String} | {@code byte[]})
 * @param <B>  backing buffer type for view construction
 *             ({@code CharSequence} | {@code byte[]})
 * @param <T>  the node type
 * @param <RD> the reader type ({@code CharReader<?>} | {@code ByteReader<?>})
 *
 * The two {@code read} overloads form the contract: {@link #read(Object,
 * Append)} is the grammar — it consumes one value and emits every piece
 * (literals and child nodes) in order; {@link #read(Object)} derives node
 * construction from it, producing a zero-copy view node when the reader is
 * buffer-backed.
 */
public interface DataCompanion<R, B, T extends StructuredData<R>, RD> {

    /**
     * grammar lookahead — {@code true} if the reader is positioned at a
     * value of this type. Must not consume.
     */
    boolean shouldRead(RD reader);

    /**
     * read one value from {@code reader}, emitting every consumed piece —
     * literals and child nodes — to {@code append} in order.
     * Throws {@link xyz.wagyourtail.commons.core.reader.ParseException} on
     * bad input.
     */
    void read(RD reader, Append append);

    /**
     * read one value → a node. Emits a zero-copy range view via
     * {@link #uncheckedView} when the reader is buffer-backed; otherwise
     * rebuilds the canonical raw from the emitted pieces → {@link #unchecked}.
     */
    T read(RD reader);

    /**
     * wrap raw without validation — {@code value} must be canonical.
     */
    T unchecked(R value);

    /**
     * wrap a range of a shared buffer — default materializes the slice into
     * {@link #unchecked}; types with view-capable node ctors override to
     * produce real zero-copy nodes.
     */
    T uncheckedView(B buffer, int start, int end);

    /**
     * parse-validate a whole input → a node; trailing input is an error.
     */
    T checked(R value);

    /**
     * replay the grammar over {@code reader}, collecting emitted pieces as
     * {@link Content} — expects the reader to be exhausted afterwards (used
     * by view-backed nodes reparsing their own range).
     */
    Content buildContent(RD reader);

    /**
     * sink for emitted grammar pieces — a SAM so Java 8+ callers can use
     * lambdas.
     */
    interface Append {
        void append(Object o);
    }

}
