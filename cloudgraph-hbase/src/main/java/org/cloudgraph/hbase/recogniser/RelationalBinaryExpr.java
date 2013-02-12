package org.cloudgraph.hbase.recogniser;

import org.plasma.query.model.RelationalOperator;

public interface RelationalBinaryExpr extends BinaryExpr {
	public RelationalOperator getOperator();
}
