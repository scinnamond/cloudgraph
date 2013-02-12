package org.cloudgraph.hbase.recogniser;

import java.util.Map;

import org.plasma.query.Term;
import org.plasma.query.model.Expression;

public class DefaultBinaryExpr extends DefaultExpr 
    implements BinaryExpr {
	
	public DefaultBinaryExpr(Term left,
			Term right, 
			Map<Expression, Boolean> truthMap) {
		super(truthMap);
		if (left == null)
			throw new IllegalArgumentException("expected arg 'left'");
		if (right == null)
			throw new IllegalArgumentException("expected arg 'right'");
		this.left = left;
		this.right = right;
	}
	private Term left;
	private Term right;

	@Override
	public Term getLeft() {
		return left;
	}
	public void setLeft(Term left) {
		this.left = left;
	}
	@Override
	public Term getRight() {
		return right;
	}
	public void setRight(Term right) {
		this.right = right;
	}
	@Override
	public boolean evaluate() {
		return false;
	}

}
