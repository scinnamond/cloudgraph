package org.cloudgraph.hbase.query;

import org.plasma.query.model.LogicalOperator;

/**
 * Represents an expression composed of two parts or terms
 * joined by a <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/LogicalOperator.html">logical</a> operator.
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public interface LogicalBinaryExpr extends BinaryExpr {
	/**
	 * Returns the logical operator.
	 * @return the logical operator.
	 */
	public LogicalOperator getOperator();
}
