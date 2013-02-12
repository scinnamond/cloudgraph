package org.cloudgraph.hbase.recogniser;

import org.plasma.query.Term;

public interface BinaryExpr extends Expr {
	public Term getLeft();
	public Term getRight();
}
