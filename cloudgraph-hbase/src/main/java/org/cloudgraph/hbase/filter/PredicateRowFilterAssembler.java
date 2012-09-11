package org.cloudgraph.hbase.filter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

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
import org.cloudgraph.hbase.key.CompositeRowKeyExpressionFactory;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.Expression;
import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.NullLiteral;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.Term;
import org.plasma.query.model.Where;
import org.plasma.query.model.WildcardOperator;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.xml.sax.SAXException;

/**
 * Processes a <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/Where.html" target="#">where</a> predicate <a href="http://plasma-sdo.org/alldocs/plasma/api/org/plasma/query/model/Expression.html" target="#">expression</a> tree 
 * into a set of HBase <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/RowFilter.html" target="#">row filters</a> arranged 
 * within a hierarchy of HBase 
 * <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.html" target="#">filter lists</a>. The
 * resulting <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.html" target="#">filter list</a> resembles
 * the given expression tree with AND/OR <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ALL" target="#">MUST_PASS_ALL</a>/<a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ONE" target="#">MUST_PASS_ONE</a> semantics 
 * closely resembling the input.
 * A <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.html" target="#">filter list</a> stack is
 * maintained which mirrors the query <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/Expression.html" target="#">expression</a> being
 * processed. 
 *  
 * @see org.cloudgraph.common.key.GraphRowKeyFactory
 */
public class PredicateRowFilterAssembler extends FilterHierarchyAssembler
{
    private static Log log = LogFactory.getLog(PredicateRowFilterAssembler.class);
    private String contextPropertyPath;
	private GraphRowKeyExpressionFactory rowKeyFac;

	@SuppressWarnings("unused")
	private PredicateRowFilterAssembler() {}
	
	/**
	 * Constructor which takes a {@link org.plasma.query.model.Query query} where
	 * clause containing any number of predicates and traverses
	 * these as a {org.plasma.query.visitor.QueryVisitor visitor} only
	 * processing various traversal events as needed against the given
	 * root type. 
	 * @param where the where clause
	 * @param rootType the root type
	 * @see org.plasma.query.visitor.QueryVisitor
	 * @see org.plasma.query.model.Query
	 */
	public PredicateRowFilterAssembler(Where where,
			PlasmaType rootType) 
	{
		this.rootType = rootType;
		
    	this.rootFilter = new FilterList(
    		FilterList.Operator.MUST_PASS_ALL);
    	 
    	this.filterStack.push(this.rootFilter);  
    	
        this.rowKeyFac = new CompositeRowKeyExpressionFactory(rootType);
        
    	for (int i = 0; i < where.getParameters().size(); i++)
    		params.add(where.getParameters().get(i).getValue());
    	
    	if (log.isDebugEnabled())
    		this.log(where);
    	
    	if (log.isDebugEnabled())
    		log.debug("begin traverse");
    	
    	where.accept(this); // traverse
    	
    	if (log.isDebugEnabled())
    		log.debug("end traverse");    	
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
	
    protected void log(Where root)
    {
    	String xml = "";
        PlasmaQueryDataBinding binding;
		try {
			binding = new PlasmaQueryDataBinding(
			    new DefaultValidationEventHandler());
	        xml = binding.marshal(root);
		} catch (JAXBException e) {
			log.debug(e);
		} catch (SAXException e) {
			log.debug(e);
		}
        log.debug("query: " + xml);
    }	

}
