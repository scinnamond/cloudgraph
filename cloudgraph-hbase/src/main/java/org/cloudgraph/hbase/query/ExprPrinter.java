package org.cloudgraph.hbase.query;

/**
 * Simple visitor client which creates a "printable" 
 * representation of a binary expression tree for debugging
 * purposes.
 * 
 * @author Scott Cinnamond
 * @since 0.5.2
 * 
 * @see Expr
 */
public class ExprPrinter implements ExprVisitor {
	StringBuilder buf = new StringBuilder();
	
	@Override
	public void visit(Expr target, Expr source, int level) {
		buf.append("\n");
		for (int i = 0; i < level; i++)
			buf.append("\t");
		buf.append(target.toString());
		
	}
	public String toString() {
		return buf.toString();
	}
}
