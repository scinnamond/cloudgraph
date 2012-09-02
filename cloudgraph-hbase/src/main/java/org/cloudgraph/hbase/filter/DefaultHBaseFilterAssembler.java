package org.cloudgraph.hbase.filter;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.plasma.query.QueryException;
import org.plasma.query.model.Expression;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.Literal;
import org.plasma.query.model.NullLiteral;
import org.plasma.query.model.QueryConstants;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Term;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;

public class DefaultHBaseFilterAssembler extends DefaultQueryVisitor 
    implements QueryConstants, HBaseFilterAssembler
{
    private static Log log = LogFactory.getLog(DefaultHBaseFilterAssembler.class);

	protected List<Object> params;
	protected FilterList rootFilter;
    protected Stack<FilterList> filterStack = new Stack<FilterList>();
	protected PlasmaType rootType;
	protected PlasmaType contextType;
	protected PlasmaProperty contextProperty;
	protected CompareFilter.CompareOp contextOp;
	protected boolean contextOpWildcard;

	/* (non-Javadoc)
	 * @see org.cloudgraph.hbase.filter.HBaseFilterAssembler#getFilter()
	 */
	public Filter getFilter() {
		return rootFilter;
	}

	/* (non-Javadoc)
	 * @see org.cloudgraph.hbase.filter.HBaseFilterAssembler#getParams()
	 */
	public Object[] getParams() {
		Object[] result = new Object[params.size()];
		Iterator<Object> iter = params.iterator();
		for (int i = 0; iter.hasNext(); i++) {
			Object param = iter.next();
			if (!(param instanceof NullLiteral))
				result[i] = param;
			else
				result[i] = null;
		}
		return result;
	}
	
	
	protected boolean hasChildExpressions(Expression expression) {
		for (Term term : expression.getTerms())
			if (term.getExpression() != null)
				return true;
		return false;
	}
	
	public void start(RelationalOperator operator) {

		this.contextOpWildcard = false;
		
		switch (operator.getValue()) {
		case EQUALS:
			this.contextOp = CompareFilter.CompareOp.EQUAL;
			break;
		case NOT_EQUALS:
			this.contextOp = CompareFilter.CompareOp.NOT_EQUAL;
			break;
		case GREATER_THAN:
			this.contextOp = CompareFilter.CompareOp.GREATER;
			break;
		case GREATER_THAN_EQUALS:
			this.contextOp = CompareFilter.CompareOp.GREATER_OR_EQUAL;
			break;
		case LESS_THAN:
			this.contextOp = CompareFilter.CompareOp.LESS;
			break;
		case LESS_THAN_EQUALS:
			this.contextOp = CompareFilter.CompareOp.LESS_OR_EQUAL;
			break;
		default:
			throw new DataAccessException("unknown operator '"
					+ operator.getValue().toString() + "'");
		}
		super.start(operator);
	}
	
	public void start(GroupOperator operator) {
		switch (operator.getValue()) {
		case RP_1:  		
	        if (log.isDebugEnabled())
				log.debug("pushing expression filter");
	        this.pushFilter(); 
	        break;
		case RP_2:  		
	        if (log.isDebugEnabled())
				log.debug("pushing 2 expression filters");
	        this.pushFilter(); 
	        this.pushFilter(); 
		    break;
		case RP_3:  			
	        if (log.isDebugEnabled())
				log.debug("pushing 3 expression filters");
	        this.pushFilter(); 
	        this.pushFilter(); 
	        this.pushFilter(); 
			break;
		case LP_1:  
	        if (log.isDebugEnabled())
				log.debug("poping expression filter");
			this.popFilter() ;
			break;
		case LP_2:  			
	        if (log.isDebugEnabled())
				log.debug("poping 2 expression filters");
			this.popFilter() ;
			this.popFilter() ;
			break;
		case LP_3:  
	        if (log.isDebugEnabled())
				log.debug("poping 3 expression filters");
			this.popFilter() ;
			this.popFilter() ;
			this.popFilter() ;
			break;
		default:
			throw new QueryException("unknown group operator, "
						+ operator.getValue().name());
		}
		super.start(operator);
	}
	
	protected void pushFilter() {
        FilterList top = this.filterStack.peek();
        FilterList next = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        top.addFilter(next);
        this.filterStack.push(next);  		
	}
	
	protected void popFilter() {
		this.filterStack.pop();
	}	
	
	// String.split() can cause empty tokens under some circumstances
	protected String[] filterTokens(String[] tokens) {
		int count = 0;
		for (int i = 0; i < tokens.length; i++)
			if (tokens[i].length() > 0)
				count++;
		String[] result = new String[count];
		int j = 0;
		for (int i = 0; i < tokens.length; i++)
			if (tokens[i].length() > 0) {
				result[j] = tokens[i];
				j++;
			}
		return result;
	}
	
	protected boolean hasWildcard(Expression expression) {
		for (int i = 0; i < expression.getTerms().size(); i++) {
			if (expression.getTerms().get(i).getWildcardOperator() != null)
			{
			    Literal literal = expression.getTerms().get(i + 1).getLiteral();
			    if (literal.getValue().indexOf(WILDCARD) >= 0) // otherwise we can treat the expr like any other
				    return true;
		    }
		}
		return false;
	}


}
