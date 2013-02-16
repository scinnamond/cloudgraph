package org.cloudgraph.hbase.query;

import org.plasma.query.model.Literal;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;

/**
 * Contains default functionality for 
 * <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a> 
 * binary query expressions.   
 * @author Scott Cinnamond
 * @since 0.5.2
 * @see Expr
 * @see ExprVisitor
 * @see EvaluationContext
 */
public class DefaultRelationalBinaryExpr extends DefaultBinaryExpr 
    implements RelationalBinaryExpr {
    protected Property property;
    protected Literal literal;
    protected RelationalOperator operator;
	/**
	 * Constructs a composite expression based on the given
	 * terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a>
	 * operator.
	 * @param left the "left" expression term
	 * @param right the "right" expression term
	 * @param oper the logical operator
	 */
	public DefaultRelationalBinaryExpr(Property property,
			Literal literal, 
			RelationalOperator operator) {
		super(property, literal);
		if (property == null)
			throw new IllegalArgumentException("expected arg 'property'");
		if (literal == null)
			throw new IllegalArgumentException("expected arg 'literal'");
		this.property = property;
		this.literal = literal;
		this.operator = operator;
	}
	
	/**
	 * Returns a "truth" value for the expression based
	 * on an evaluation of the <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a>
	 * operator within the given context. 
	 * @param context the context
	 * @return "truth" value for the expression based
	 * on an evaluation of the <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a>
	 * operator within the given context. 
	 */
	@Override
	public boolean evaluate(EvaluationContext context) {
		return true;
	}
	
	/**
	 * Returns the operator for the expression.
	 * @return the operator for the expression.
	 */
	@Override
	public RelationalOperator getOperator() {
		return this.operator;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(this.getClass().getSimpleName());
		buf.append(" [");
		buf.append(property.getName());
		buf.append(" ");
		buf.append(this.operator.getValue().name());
		buf.append(" ");
		buf.append(this.literal.getValue());
		buf.append("]");
		return buf.toString();
	}
    
}
