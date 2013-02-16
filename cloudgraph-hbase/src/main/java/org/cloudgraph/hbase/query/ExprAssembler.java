package org.cloudgraph.hbase.query;

import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.WildcardOperator;

/**
 * A factory oriented interface used to create various 
 * {@link Expr expression} implementations. 
 * 
 * 
 * @author Scott Cinnamond
 * @since 0.5.2
 * 
 * @see Expr
 */
public interface ExprAssembler {
	/**
	 * Creates and returns a relational binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a>
	 * operator.
	 * @param property the property term
	 * @param literal the literal term
	 * @param operator the <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a> operator
	 * @return a relational binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a>
	 * operator.
	 */
    public RelationalBinaryExpr createRelationalBinaryExpr(Property property,
		Literal literal, RelationalOperator operator);
	
    /**
	 * Creates and returns a wildcard binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/WildcardOperator.html">wildcard</a>
	 * operator.
	 * @param property the property term
	 * @param literal the literal term
	 * @param operator the <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/WildcardOperator.html">wildcard</a> operator
	 * @return a wildcard binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/WildcardOperator.html">wildcard</a>
	 * operator.
	 */
    public WildcardBinaryExpr createWildcardBinaryExpr(Property property,
		Literal literal, WildcardOperator operator);
    
    /**
	 * Creates and returns a logical binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/LogicalOperator.html">logical</a>
	 * operator.
	 * @param property the property term
	 * @param literal the literal term
	 * @param operator the <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/LogicalOperator.html">logical</a> operator
	 * @return a wildcard binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/LogicalOperator.html">logical</a>
	 * operator.
	 */
    public LogicalBinaryExpr createLogicalBinaryExpr(Expr left, Expr right,
		LogicalOperator operator);
}
