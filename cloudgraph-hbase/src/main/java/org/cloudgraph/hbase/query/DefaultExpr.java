package org.cloudgraph.hbase.query;


/**
 * Default behavior for query expressions.  
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public abstract class DefaultExpr {
	protected DefaultExpr() {
		super();
	}
    
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(this.getClass().getSimpleName());
		return buf.toString();
	}
}
