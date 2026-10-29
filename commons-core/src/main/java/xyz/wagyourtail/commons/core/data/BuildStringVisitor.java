package xyz.wagyourtail.commons.core.data;

/**
 * Rebuilds a node's canonical text by walking its entries via
 * {@link StructuredData#accept}: every non-node entry is appended with
 * {@code toString()}.
 */
public class BuildStringVisitor implements DataVisitor {

    private final StringBuilder sb = new StringBuilder();

    @Override
    public boolean visit(Object o) {
        if (!(o instanceof StructuredData)) {
            sb.append(o);
        }
        return true;
    }

    @Override
    public void visitEnd(Object o) {
    }

    public String build() {
        return sb.toString();
    }

    public static String apply(StructuredData<?> data) {
        BuildStringVisitor visitor = new BuildStringVisitor();
        data.accept(visitor);
        return visitor.build();
    }

}
