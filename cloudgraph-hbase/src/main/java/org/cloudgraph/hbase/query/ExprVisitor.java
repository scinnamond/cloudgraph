package org.cloudgraph.hbase.query;

/**
 * Expression visitor pattern client interface.
 * @author Scott Cinnamond
 * @since 0.5.2
 * @see Expr
 */
public interface ExprVisitor {
	
	/**
	 * The client event received when a new node is encountered.  
	 * @param target the target node
	 * @param source the target node
	 * @param level the traversal level
     * @see Expr
	 */
    public void visit(Expr target, Expr source, int level);
}
