package xyz.wagyourtail.commons.core.data;

import xyz.wagyourtail.commons.core.reader.ByteArrayByteReader;
import xyz.wagyourtail.commons.core.reader.ByteReader;
import xyz.wagyourtail.commons.core.writer.ByteWriter;

import java.util.Arrays;

/**
 * {@link StructuredData} over bytes — the binary mirror of {@link StringData}.
 * Storage is one of:
 * <ul>
 * <li>a known canonical raw array,</li>
 * <li>a {@code (buffer, start, end)} view into a shared backing array
 * (zero-copy), or</li>
 * <li>a {@link Content} tree (synthetic; raw materializes via
 * {@link BuildBytesVisitor}).</li>
 * </ul>
 */
public abstract class BinaryData extends AbstractStructuredData<byte[]> {

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private byte[] viewBuffer;
    private int viewStart;
    private int viewEnd;

    protected BinaryData(byte[] rawContent) {
        super(rawContent);
    }

    /**
     * view-backed node over {@code (buffer, start, end)} — no slicing or
     * reparsing until {@link #getRawContent()}/{@link #getContent()} are
     * actually read.
     */
    protected BinaryData(byte[] viewBuffer, int start, int end) {
        if (start < 0 || start > end || end > viewBuffer.length) {
            throw new IllegalArgumentException(
                "invalid view range " + start + ".." + end + " for buffer of size " + viewBuffer.length
            );
        }
        this.viewBuffer = viewBuffer;
        this.viewStart = start;
        this.viewEnd = end;
    }

    protected BinaryData(Content content) {
        super(content);
    }

    /**
     * materialize this node's canonical bytes — the view range slice for
     * view-backed nodes, or a serialize of the content tree otherwise.
     */
    @Override
    protected byte[] buildRawContent() {
        if (viewBuffer != null) {
            return Arrays.copyOfRange(viewBuffer, viewStart, viewEnd);
        }
        return BuildBytesVisitor.apply(this);
    }

    /**
     * a reader over exactly this node's raw range — zero-copy for views,
     * whole array otherwise.
     */
    protected ByteReader<?> rawRangeReader() {
        if (viewBuffer != null) {
            return new ByteArrayByteReader(viewBuffer, viewStart, viewEnd);
        }
        return new ByteArrayByteReader(getRawContent());
    }

    /**
     * write this node's canonical bytes to {@code writer} — for view-backed
     * nodes this streams the range without materializing a slice.
     */
    public void writeBytesTo(ByteWriter writer) {
        if (viewBuffer != null) {
            writer.put(viewBuffer, viewStart, viewEnd - viewStart);
        } else {
            writer.put(getRawContent());
        }
    }

    /**
     * serialize one emitted piece to bytes — the shared emission contract of
     * {@link BuildBytesVisitor} and canonical reconstruction in
     * {@link BinaryDataCompanion#read}: {@code byte[]} → raw bytes,
     * {@code Byte}/{@code Integer} in {@code 0..255} → one byte,
     * {@code String} → UTF-8, {@link BinaryData} → {@link #writeBytesTo}.
     */
    public static void writeRawPiece(ByteWriter writer, Object piece) {
        if (piece == null) {
            return;
        }
        if (piece instanceof BinaryData) {
            ((BinaryData) piece).writeBytesTo(writer);
        } else if (piece instanceof byte[]) {
            writer.put((byte[]) piece);
        } else if (piece instanceof Byte) {
            writer.put(((Byte) piece) & 0xFF);
        } else if (piece instanceof Integer) {
            int v = (Integer) piece;
            if (v < 0 || v > 255) {
                throw new IllegalArgumentException("int piece " + v + " out of single-byte range");
            }
            writer.put(v);
        } else if (piece instanceof String) {
            writer.putUtf8((String) piece);
        } else {
            throw new IllegalArgumentException(
                "cannot serialize entry of " + piece.getClass() + " to bytes: " + piece
            );
        }
    }

    @Override
    public String toString() {
        byte[] raw = getRawContent();
        int n = Math.min(raw.length, 16);
        StringBuilder sb = new StringBuilder(n * 2 + 24);
        sb.append("0x");
        for (int i = 0; i < n; i++) {
            sb.append(HEX[(raw[i] >>> 4) & 0xF]);
            sb.append(HEX[raw[i] & 0xF]);
        }
        if (raw.length > n) {
            sb.append("…");
        }
        return sb.append(" (").append(raw.length).append(" bytes)").toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BinaryData && Arrays.equals(getRawContent(), ((BinaryData) o).getRawContent());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getRawContent());
    }

}
