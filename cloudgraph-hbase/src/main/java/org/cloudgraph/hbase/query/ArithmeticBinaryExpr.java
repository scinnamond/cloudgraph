package org.cloudgraph.hbase.query;

import org.plasma.query.model.ArithmeticOperator;

/**
 * Represents an expression composed of two parts or terms
 * joined by an <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/ArithmeticOperator.html">arithmetic</a> operator.
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public interface ArithmeticBinaryExpr extends BinaryExpr {
	/**
	 * Returns the arithmetic operator.
	 * @return the arithmetic operator.
	 */
	public ArithmeticOperator getOperator();
}
