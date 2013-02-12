package org.cloudgraph.hbase.recogniser;

import java.util.HashMap;
import java.util.Map;

import org.plasma.query.model.Expression;

public class DefaultExpr {
	protected Map<Expression, Boolean> truthMap = new HashMap<Expression, Boolean>();
    @SuppressWarnings("unused")
	private DefaultExpr() {}
	public DefaultExpr(Map<Expression, Boolean> truthMap) {
		super();
		this.truthMap = truthMap;
	}
    
}
