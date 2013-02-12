package org.cloudgraph.hbase.recogniser;

import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.plasma.query.model.Literal;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;

public class DefaultRelationalBinarylExpr extends DefaultBinaryExpr 
    implements RelationalBinaryExpr {
    protected Property property;
    protected Literal literal;
    protected RelationalOperator operator;
    private Map<String, KeyValue> keyMap;
    private String propertyQualifier;
	public DefaultRelationalBinarylExpr(Property property,
			String propertyQualifier,
			Literal literal, 
			RelationalOperator operator,
			Map<String, KeyValue> keyMap) {
		super(property, literal, null);
		if (property == null)
			throw new IllegalArgumentException("expected arg 'property'");
		if (literal == null)
			throw new IllegalArgumentException("expected arg 'literal'");
		this.property = property;
		this.literal = literal;
		this.operator = operator;
		this.keyMap = keyMap;
		this.propertyQualifier = propertyQualifier;
		
	}
	@Override
	public boolean evaluate() {		
		KeyValue value = keyMap.get(this.propertyQualifier);
		boolean found = value != null;
		return found;
	}
	
	@Override
	public RelationalOperator getOperator() {
		return this.operator;
	}
    
}
