package org.cloudgraph.hbase.query;

/**
 * Represents an expression composed of one term.
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public interface UnaryExpr extends Expr {
	/**
	 * Returns the single child node for the expression.
	 * @return the single child node for the expression.
	 */
	public Term getTerm();
}
