package xyz.wagyourtail.commons.core.data;

/**
 * One node of structured data: a lazily-derived pair of canonical
 * {@link #getRawContent()} and {@link #getContent()} (child entries).
 *
 * This is a contract, not a storage policy — three storage flavors exist:
 * raw-backed ({@link RawStringData}/{@link RawBinaryData}), view (the same
 * classes via their {@code (buffer, start, end)} protected constructor —
 * a zero-copy range into a shared backing buffer), and synthetic
 * ({@link ParsedStringData}/{@link ParsedBinaryData} — built from children,
 * raw materializes on serialize).
 *
 * Atomic leaf types may implement this interface directly and source their
 * parsed parts from {@link ParseCache}.
 *
 * @param <R> canonical raw type ({@code String} | {@code byte[]})
 */
public interface StructuredData<R> {

    /**
     * canonical serialized form; materialized lazily for view/synthetic nodes.
     */
    R getRawContent();

    /**
     * child entries — sub-nodes, raw slices, or literals — in order.
     * Derived once per node and cached.
     */
    Content getContent();

    /**
     * walk this node and its children with {@code visitor}; a {@code visit}
     * returning {@code false} skips that node's children.
     */
    void accept(DataVisitor visitor);

}
