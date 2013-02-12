package org.cloudgraph.hbase.recogniser;

import java.util.Map;

import org.plasma.query.model.Expression;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.Term;

public class DefaultLogicalExprPair extends DefaultBinaryExpr 
    implements LogicalBinaryExpr {
	private Expression leftExpression;
	private Expression rightExpression;
	private LogicalOperator oper;
	public DefaultLogicalExprPair(Term left, Term right,
			LogicalOperator oper,
			Map<Expression, Boolean> truthMap) {
		super(left, right, truthMap);
		if (oper == null)
			throw new IllegalArgumentException("expected arg 'oper'");
		if (left.getExpression() == null)
			throw new IllegalArgumentException("expected arg 'left' with expression");
		if (right.getExpression() == null)
			throw new IllegalArgumentException("expected arg 'right' with expression");
		this.leftExpression = left.getExpression();
		this.rightExpression = right.getExpression();
		this.oper = oper;
	}
	public LogicalOperator getOperator() {
		return oper;
	}
	public void setOperator(LogicalOperator oper) {
		this.oper = oper;
	}
	
	public boolean evaluate() {
		boolean leftTrue = truthMap.get(leftExpression);
		boolean rightTrue = truthMap.get(rightExpression);
		
		boolean result = false;
		switch (oper.getValue()) {
		case AND:
			result = leftTrue && rightTrue;
			break;
		case OR:
			result = leftTrue || rightTrue;
			break;
		case NOT:
		default:
			throw new IllegalStateException("unexpected logical operator, "
					+ oper.getValue());
		}
		return result;
	}
}