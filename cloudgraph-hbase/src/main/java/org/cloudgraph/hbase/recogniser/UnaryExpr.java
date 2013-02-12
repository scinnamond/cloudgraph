package org.cloudgraph.hbase.recogniser;

public interface UnaryExpr extends Expr {
	public boolean evaluate(boolean other);
}
