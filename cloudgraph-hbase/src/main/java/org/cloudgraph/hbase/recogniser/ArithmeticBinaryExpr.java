package org.cloudgraph.hbase.recogniser;

import org.plasma.query.model.ArithmeticOperator;

public interface ArithmeticBinaryExpr extends BinaryExpr {
	public ArithmeticOperator getOperator();
}
