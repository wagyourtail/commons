package xyz.wagyourtail.commons.core.data;

import xyz.wagyourtail.commons.core.writer.ByteArrayByteWriter;
import xyz.wagyourtail.commons.core.writer.ByteWriter;

/**
 * Rebuilds a node's canonical bytes by walking its entries via
 * {@link StructuredData#accept}: each non-node entry is emitted per
 * {@link BinaryData#writeRawPiece}.
 */
public class BuildBytesVisitor implements DataVisitor {

    private final ByteWriter writer;

    public BuildBytesVisitor() {
        this(new ByteArrayByteWriter());
    }

    public BuildBytesVisitor(ByteWriter writer) {
        this.writer = writer;
    }

    @Override
    public boolean visit(Object o) {
        if (!(o instanceof StructuredData)) {
            BinaryData.writeRawPiece(writer, o);
        }
        return true;
    }

    @Override
    public void visitEnd(Object o) {
    }

    public byte[] build() {
        if (!(writer instanceof ByteArrayByteWriter)) {
            throw new IllegalStateException("build() requires a ByteArrayByteWriter");
        }
        return ((ByteArrayByteWriter) writer).toByteArray();
    }

    public static byte[] apply(StructuredData<?> data) {
        BuildBytesVisitor visitor = new BuildBytesVisitor();
        data.accept(visitor);
        return visitor.build();
    }

}
