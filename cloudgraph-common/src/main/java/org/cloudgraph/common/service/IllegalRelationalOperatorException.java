package org.cloudgraph.common.service;

import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.RelationalOperatorValues;

 
public class IllegalRelationalOperatorException extends GraphServiceException
{
    private static final long serialVersionUID = 1L;
    
    
    public IllegalRelationalOperatorException(RelationalOperatorValues operator,
    		int fieldSeqNum, String fieldPath)
    {
        super("relational operator ("+ toUserString(operator) +") not allowed for user "
            + "defined row key fields with a hash algorithm applied - see configured hash settings for "
        	+ "user defined field (" + fieldSeqNum 
        	+ ") with path '"
        	+ fieldPath + "'");
    }
    
    
    public IllegalRelationalOperatorException(String message)
    {
        super(message);
    }
    public IllegalRelationalOperatorException(Throwable t)
    {
        super(t);
    }
    
	private static String toUserString(RelationalOperatorValues operator) {
		switch (operator) {
		case EQUALS: 	return "equals";
		case NOT_EQUALS: 	return "not equals";
		case GREATER_THAN: 	return "greater than";
		case GREATER_THAN_EQUALS: 	return "greater than equals";
		case LESS_THAN: 	return "less than";
		case LESS_THAN_EQUALS: 	return "less than equals";
		default:
			return operator.name();
		}
	}
    
}