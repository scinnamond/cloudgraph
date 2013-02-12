package org.cloudgraph.hbase.recogniser;

import org.plasma.query.Term;

public interface Expr extends Term {
	public boolean evaluate();
}
