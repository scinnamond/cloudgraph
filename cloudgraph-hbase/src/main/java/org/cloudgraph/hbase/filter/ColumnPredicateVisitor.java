package org.cloudgraph.hbase.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.filter.GraphFilterException;
import org.cloudgraph.hbase.key.CompositeColumnKeyFactory;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.Expression;
import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.NullLiteral;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.Term;
import org.plasma.query.model.WildcardOperator;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;

/**
 * Creates a column filter hierarchy using <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/QualifierFilter.html">QualifierFilter</a> 
 *  /<a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/ValueFilter.html">ValueFilter</a> pairs
 * recreating composite column qualifier prefixes using {@link CompositeColumnKeyFactory}. 
 * Processes visitor events for query model elements specific to assembly of HBase column
 * filters, such as properties, wildcards, literals, logical operators, relational 
 * operators, within the context of HBase filter hierarchy assembly.
 * Maintains various context information useful to subclasses. 
 * <p>
 * HBase filters may be collected into 
 * lists using <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.html" target="#">FilterList</a>
 * each with a 
 * <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ALL" target="#">MUST_PASS_ALL</a> or <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ONE" target="#">MUST_PASS_ONE</a>
 *  (logical) operator. Lists may then be assembled into hierarchies 
 * used to represent complex expression trees filtering either rows
 * or columns in HBase.
 * </p> 
 * @see org.cloudgraph.common.key.CompositeColumnKeyFactory
 */
public class ColumnPredicateVisitor extends PredicateVisitor {
    private static Log log = LogFactory.getLog(ColumnPredicateVisitor.class);
	protected CompositeColumnKeyFactory columnKeyFac;
	protected String contextPropertyPath;

    public ColumnPredicateVisitor(PlasmaType rootType) {
		super(rootType);
	}
    
	/**
	 * Process the traversal start event for a query {@link org.plasma.query.model.Expression expression}
	 * creating a new HBase {@link org.apache.hadoop.hbase.filter.FilterList filter list} with a
	 * default {@link Operator#MUST_PASS_ALL AND} operator and pushes it onto the
	 * stack. Any subsequent {@link org.plasma.query.model.Literal literals} encountered then cause
	 * a new {@link org.apache.hadoop.hbase.filter.RowFilter row filter} to be created
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
        PlasmaType targetType = (PlasmaType)this.contextType;                
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
		
        byte[] colKey = this.columnKeyFac.createColumnKey(
                this.contextType, this.contextProperty);

		FilterList list = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        QualifierFilter qualFilter = new QualifierFilter(
            	CompareFilter.CompareOp.EQUAL,
            	new BinaryPrefixComparator(colKey)); 
        list.addFilter(qualFilter);
        ValueFilter valueFilter = new ValueFilter(
        		this.contextOp,
            	new BinaryComparator(Bytes.toBytes(content)));
        list.addFilter(valueFilter);		
    	this.filterStack.peek().addFilter(list);
		
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
}
