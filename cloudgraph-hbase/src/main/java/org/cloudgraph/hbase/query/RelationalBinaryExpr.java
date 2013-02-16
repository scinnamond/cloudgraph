package org.cloudgraph.hbase.query;

import org.plasma.query.model.RelationalOperator;

/**
 * Represents an expression composed of two parts or terms
 * joined by a <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a> operator.
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public interface RelationalBinaryExpr extends BinaryExpr {
	/**
	 * Returns the relational operator.
	 * @return the relational operator.
	 */
	public RelationalOperator getOperator();
}
