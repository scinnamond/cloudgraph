package org.cloudgraph.hbase.query;

/**
 * Base interface representing a query expression.  
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public interface Expr extends Term {
	
	/**
	 * Returns a "truth" value for the expression based
	 * on the given context. 
	 * @param context
	 * @return a "truth" value for the expression based
	 * on the given context.
	 */
	public boolean evaluate(EvaluationContext context);
}
