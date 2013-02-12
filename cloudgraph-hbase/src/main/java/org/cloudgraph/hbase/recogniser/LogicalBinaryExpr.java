package org.cloudgraph.hbase.recogniser;

import org.plasma.query.model.LogicalOperator;

public interface LogicalBinaryExpr extends BinaryExpr {
	public LogicalOperator getOperator();
}
