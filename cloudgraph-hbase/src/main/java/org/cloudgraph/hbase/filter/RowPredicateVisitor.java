package org.cloudgraph.hbase.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;
import org.cloudgraph.common.filter.GraphFilterException;
import org.cloudgraph.common.key.GraphRowKeyExpressionFactory;
import org.cloudgraph.common.key.TokenValue;
import org.plasma.query.QueryException;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.Expression;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.NullLiteral;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.QueryConstants;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Term;
import org.plasma.query.model.WildcardOperator;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;

/**
 * Creates a hierarchy of regular expression based HBase row filters 
 * using {@link GraphRowKeyExpressionFactory}
 * and HBase <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/RegexStringComparator.html">RegexStringComparator</a>.
 * Processes visitor events for query model elements specific to assembly of HBase row
 * filters, such as properties, wildcards, literals, logical operators, relational 
 * operators, within the context of HBase filter hierarchy assembly.
 * Maintains various context information useful to subclasses. 
 *  
 * @see org.cloudgraph.common.key.GraphRowKeyExpressionFactory
 */
public class RowPredicateVisitor extends PredicateVisitor {
    private static Log log = LogFactory.getLog(RowPredicateVisitor.class);
    protected String contextPropertyPath;
    protected GraphRowKeyExpressionFactory rowKeyFac;

    public RowPredicateVisitor(PlasmaType rootType) {
		super(rootType);
	}

	/**
	 * Process the traversal start event for a query {@link org.plasma.query.model.Expression expression}
	 * creating a new HBase <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.html">filter list</a> with a
	 * default <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ALL">MUST_PASS_ALL</a> operator and pushes it onto the
	 * stack. Any subsequent {@link org.plasma.query.model.Literal literals} encountered then cause
	 * a new <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/RowFilter.html">row filter</a> to be created
	 * and added to this new filter list which is on the top of the stack.
	 * @param expression the expression
	 */
	@Override
	public void start(Expression expression)
    {
        if (hasChildExpressions(expression)) {
            if (log.isDebugEnabled())
    			log.debug("pushing expression filter");
            this.pushFilter(); 
        }

        for (Term term : expression.getTerms())
        	if (term.getSubqueryOperator() != null)
                throw new GraphFilterException("subqueries for row filters not yet supported");
    }                               
 
	
	/**
	 * Process the traversal end event for a query {@link org.plasma.query.model.Expression expression}
	 * removing the current (top) HBase {@link org.apache.hadoop.hbase.filter.FilterList filter list} 
     * from the stack.
	 * @param expression the expression
	 */
	@Override
	public void end(Expression expression)
    {
        if (hasChildExpressions(expression)) {
            if (log.isDebugEnabled())
    			log.debug("poping expression filter");
            this.popFilter(); 
        }
    }
	
	/**
	 * Process the traversal start event for a query {@link org.plasma.query.model.Property property}
     * within an {@link org.plasma.query.model.Expression expression} just
     * traversing the property path if exists and capturing context information
     * for the current {@link org.plasma.query.model.Expression expression}.  
	 * @see org.plasma.query.visitor.DefaultQueryVisitor#start(org.plasma.query.model.Property)
	 */
	@Override
    public void start(Property property)
    {                
        org.plasma.query.model.FunctionValues function = property.getFunction();
        if (function != null)
            throw new GraphFilterException("aggregate functions only supported in subqueries not primary queries");
          
        Path path = property.getPath();
        PlasmaType targetType = (PlasmaType)this.rootType;                
        if (path != null)
        {
            for (int i = 0 ; i < path.getPathNodes().size(); i++)
            {    
            	AbstractPathElement pathElem = path.getPathNodes().get(i).getPathElement();
                if (pathElem instanceof WildcardPathElement)
                    throw new DataAccessException("wildcard path elements applicable for 'Select' clause paths only, not 'Where' clause paths");
                String elem = ((PathElement)pathElem).getValue();
                PlasmaProperty prop = (PlasmaProperty)targetType.getProperty(elem);                
                targetType = (PlasmaType)prop.getType(); // traverse
            }
        }
        PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
        this.contextProperty = endpointProp;
        this.contextType = targetType;
        this.contextPropertyPath = property.asPathString();
        
        super.start(property);
    }     

	public void start(WildcardOperator operator) {
		switch (operator.getValue()) {
		case LIKE:
			this.contextOp = CompareFilter.CompareOp.EQUAL;
			this.contextOpWildcard = true;
			break;
		default:
			throw new GraphFilterException("unknown operator '"
					+ operator.getValue().toString() + "'");
		}
		super.start(operator);
	}
	
	
    /**
     * Process the traversal start event for a query {@link org.plasma.query.model.Literal literal}
     * within an {@link org.plasma.query.model.Expression expression} creating an HBase 
     * {@link org.apache.hadoop.hbase.filter.RowFilter row filter} and adding it to the filter hierarchy. Looks at the context
     * under which the literal is encountered and if a user defined 
     * row key token configuration is found, creates a regular expression
     * based HBase row filter.
     * @param literal the expression literal
     * @throws GraphFilterException if no user defined row-key token
     * is configured for the current literal context.
     */
	@Override
	public void start(Literal literal) {
		String content = literal.getValue();
		if (this.contextProperty == null)
			throw new IllegalStateException("expected context property for literal");
		if (this.contextType == null)
			throw new IllegalStateException("expected context type for literal");
		if (this.rootType == null)
			throw new IllegalStateException("expected context type for literal");
		if (this.contextOp == null)
			throw new IllegalStateException("expected context operator for literal");
		
		// Match the current property to a user defined 
		// row key token, if match we can add a row filter.
		if (this.rowKeyFac.hasUserDefinedRowKeyToken(this.rootType, this.contextPropertyPath)) 
		{
			TokenValue pair = new TokenValue(
					this.contextProperty, 
					content);			
			pair.setPropertyPath(this.contextPropertyPath);
			if (this.contextOpWildcard)
				pair.setIsWildcard(true);
			
			// FIXME: can't several of these be lumped together if in the same AND expression parent??
			List<TokenValue> pairs = new ArrayList<TokenValue>();
			pairs.add(pair);
			
	        String rowKeyExpr = this.rowKeyFac.createRowKeyExpr(
	        	pairs);        
			
	        WritableByteArrayComparable exprComp = 
	        	new RegexStringComparator(rowKeyExpr);
	        
	        Filter rowFilter = new RowFilter(this.contextOp,
					exprComp);			
			FilterList top = this.filterStack.peek();
			top.addFilter(rowFilter);
			
			if (log.isDebugEnabled())
				log.debug("created row filter: " 
					+ rowKeyExpr + " operator: " + this.contextOp);
		}
		else
	        throw new GraphFilterException("no user defined row-key token for query path '"
			    	+ this.contextPropertyPath + "'");
		
		super.start(literal);
	}

	/**
	 * (non-Javadoc)
	 * @see org.plasma.query.visitor.DefaultQueryVisitor#start(org.plasma.query.model.NullLiteral)
	 */
	@Override
	public void start(NullLiteral nullLiteral) {
        throw new GraphFilterException("null literals for row filters not yet supported");
	}
	
	/**
	 * Process a {@link org.plasma.query.model.LogicalOperator logical operator} query traversal
	 * start event. If the {@link FilterList filter list} on the top of
	 * the filter stack is not an 'OR' filter, since it's immutable
	 * and we cannot modify its operator, create an 'OR'
	 * filter and swaps out the existing filters into
	 * the new 'OR' {@link FilterList filter list}. 
	 */
	public void start(LogicalOperator operator) {
		
		switch (operator.getValue()) {
		case AND:
			break; // default filter list oper is must-pass-all (AND)
		case OR:			
			FilterList top = this.filterStack.peek();			
			if (top.getOperator().ordinal() != FilterList.Operator.MUST_PASS_ONE.ordinal()) {
				FilterList orList = new FilterList(
				    	FilterList.Operator.MUST_PASS_ONE);
				for (Filter filter : top.getFilters())
					orList.addFilter(filter);
				top.getFilters().clear();
				this.filterStack.pop();
				FilterList previous = this.filterStack.peek();
				if (!previous.getFilters().remove(top))
					throw new IllegalStateException("could not remove filter list");
				previous.addFilter(orList);
				this.filterStack.push(orList);
			}
			break;
		}
		super.start(operator);
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
			    if (literal.getValue().indexOf(QueryConstants.WILDCARD) >= 0) // otherwise we can treat the expr like any other
				    return true;
		    }
		}
		return false;
	}
}
