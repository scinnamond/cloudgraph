package org.cloudgraph.hbase.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.plasma.query.model.Literal;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;

/**
 * An {@link RelationalBinaryExpr} implementation which uses a specific 
 * evaluation {@link RecogniserContext context} to locate
 * or recognize a given sequence based column qualifier within 
 * the context of the expression.       
 * @author Scott Cinnamond
 * @since 0.5.2
 * @see RecogniserContext
 */
public class RecogniserRelationalBinaryExpr extends DefaultRelationalBinaryExpr 
    implements RelationalBinaryExpr {
    private static Log log = LogFactory.getLog(RecogniserRelationalBinaryExpr.class);
    private String columnQualifierPrefix;
	
    /**
     * Constructs an expression based on the given terms
     * and column qualifier prefix. 
     * @param property the "left" property term
     * @param columnQualifierPrefix the qualifier prefix used
     * to evaluate the expression for a given context.
     * @param literal the "right" literal term
     * @param operator the relational operator
     * @see RecogniserContext
     */
    public RecogniserRelationalBinaryExpr(Property property,
			String columnQualifierPrefix,
			Literal literal, 
			RelationalOperator operator) {
		super(property, literal, operator);
		this.columnQualifierPrefix = columnQualifierPrefix;		
	}
	
	/**
	 * Returns a "truth" value for the expression using a specific 
     * evaluation {@link RecogniserContext context} to locate
     * or recognize a given sequence based column qualifier within 
     * the context of the expression.   
	 * @param context
	 * @return a "truth" value for the expression using a specific 
     * evaluation {@link RecogniserContext context} to locate
     * or recognize a given sequence based column qualifier within 
     * the context of the expression.
     * @see RecogniserContext
	 */
	@Override
	public boolean evaluate(EvaluationContext context) {
		RecogniserContext ctx = (RecogniserContext)context;		
		//FIXME: use Array copy
		String qualifier = this.columnQualifierPrefix 
				+ String.valueOf(ctx.getSequence());
		KeyValue value = ctx.getKeyMap().get(qualifier);
		
		
		boolean found = value != null;
		if (log.isDebugEnabled())
			log.debug("evaluate: " + found + " '" + qualifier
				+ "' in map " + ctx.getKeyMap().keySet());
		return found;
	}
	    
}
