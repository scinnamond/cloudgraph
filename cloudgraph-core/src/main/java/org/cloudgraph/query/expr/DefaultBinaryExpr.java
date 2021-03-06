/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.query.expr;

import java.util.HashSet;

import org.plasma.query.Term;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.PathNode;
import org.plasma.query.model.Property;
import org.plasma.query.model.WildcardPathElement;

/**
 * Contains default functionality for query expressions composed of 
 * two parts or terms, including a visitor traversal implementation.   
 * @author Scott Cinnamond
 * @since 0.5.2
 * @see Term
 * @see ExprVisitor
 * @see EvaluationContext
 */
public abstract class DefaultBinaryExpr extends DefaultExpr 
    implements BinaryExpr {
	
	/**
	 * Constructs an expression using the given terms
	 * @param left the "left" expression term
	 * @param right the "right" expression term
	 */
	public DefaultBinaryExpr(Term left, Term right) {
		super();
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
	
	/**
	 * Returns a "truth" value for the expression based
	 * on the given context. 
	 * @param context
	 * @return a "truth" value for the expression based
	 * on the given context.
	 */
	@Override
	public boolean evaluate(EvaluationContext context) {
		return true;
	}
	
	/**
	 * Begins the traversal of the expression tree with this
	 * node as the root. 
	 * @param visitor the expression visitor
	 */
	public void accept(ExprVisitor visitor) {
		accept(this, null, visitor, new HashSet<Expr>(), 0);
	}

	private void accept(Expr target, Expr source, 
			ExprVisitor visitor, HashSet<Expr> visited, int level) {
		if (!visited.contains(target)) {
		    visitor.visit(target, source, level);
		    visited.add(target);
		}
		else
			return;
		if (this.left instanceof DefaultBinaryExpr) {
			DefaultBinaryExpr expr = (DefaultBinaryExpr)this.left;
			expr.accept(expr, this, visitor, visited, level + 1);
		}
		if (this.right instanceof DefaultBinaryExpr) {
			DefaultBinaryExpr expr = (DefaultBinaryExpr)this.right;
			expr.accept(expr, this, visitor, visited, level + 1);
		}
	}
	
	protected String createPropertyPath(Property property)
	{
		StringBuilder buf = new StringBuilder();
		if (property.getPath() != null) {
			for (PathNode node : property.getPath().getPathNodes()) {
				AbstractPathElement pathElem = node.getPathElement();
				if (pathElem instanceof PathElement) {
					buf.append(((PathElement)pathElem).getValue());
				}
				else if (pathElem instanceof WildcardPathElement){
					buf.append(((WildcardPathElement)pathElem).getValue());					
				}
				else
					buf.append("?");
				buf.append("/");
			}
		}
		buf.append(property.getName());
		return buf.toString();
		
	}
}
