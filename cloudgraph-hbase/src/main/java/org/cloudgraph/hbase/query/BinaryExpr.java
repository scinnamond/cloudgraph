package org.cloudgraph.hbase.query;

import org.plasma.query.Term;

/**
 * Represents an expression composed of two parts or terms.
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public interface BinaryExpr extends Expr {
	/**
	 * Returns the "left" child node for the expression.
	 * @return the "left" child node for the expression.
	 */
	public Term getLeft();
	/**
	 * Returns the "right" child node for the expression.
	 * @return the "right" child node for the expression.
	 */
	public Term getRight();
	
	/**
	 * Begins the traversal of the expression as a root.
	 * @param visitor the traversal visitor
	 */
	public void accept(ExprVisitor visitor);
}
