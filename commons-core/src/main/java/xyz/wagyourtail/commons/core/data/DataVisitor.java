package xyz.wagyourtail.commons.core.data;

public interface DataVisitor {

    /**
     * called for each node (before its children) and each non-node entry.
     * Return {@code false} to skip a node's children.
     */
    boolean visit(Object o);

    /**
     * called after a node's children have been visited.
     */
    void visitEnd(Object o);

}
