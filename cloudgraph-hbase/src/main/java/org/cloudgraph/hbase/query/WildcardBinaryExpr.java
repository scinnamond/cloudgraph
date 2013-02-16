package org.cloudgraph.hbase.query;

import org.plasma.query.model.WildcardOperator;

/**
 * Represents an expression composed of two parts or terms
 * joined by a <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/WildcardOperator.html">wildcard</a> operator.
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public interface WildcardBinaryExpr extends BinaryExpr {
	/**
	 * Returns the wildcard operator.
	 * @return the wildcard operator.
	 */
	public WildcardOperator getOperator();
}
