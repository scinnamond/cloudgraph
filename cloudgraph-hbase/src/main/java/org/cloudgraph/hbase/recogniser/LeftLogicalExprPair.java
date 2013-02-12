package org.cloudgraph.hbase.recogniser;

import java.util.Map;

import org.plasma.query.Term;
import org.plasma.query.model.Expression;
import org.plasma.query.model.LogicalOperator;

public class LeftLogicalExprPair extends DefaultExpr 
    implements LogicalBinaryExpr {
	private Expr leftExpression;
	private Expression rightExpression;
	private LogicalOperator oper;
	public LeftLogicalExprPair(Expr left, Expression right,
			LogicalOperator oper,
			Map<Expression, Boolean> truthMap) {
		super(truthMap);
		if (oper == null)
			throw new IllegalArgumentException("expected arg 'oper'");
		this.leftExpression = left;
		this.rightExpression = right;
		this.oper = oper;
	}
	public LogicalOperator getOperator() {
		return oper;
	}
	public void setOperator(LogicalOperator oper) {
		this.oper = oper;
	}
	
	public boolean evaluate() {
		boolean leftTrue = leftExpression.evaluate();
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
	@Override
	public Term getLeft() {
		return leftExpression;
	}
	@Override
	public Term getRight() {
		return rightExpression;
	}
}