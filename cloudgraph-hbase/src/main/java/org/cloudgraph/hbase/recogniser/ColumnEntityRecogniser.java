package org.cloudgraph.hbase.recogniser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.filter.GraphFilterException;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.hbase.filter.ExpresionVisitorSupport;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.Expression;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Term;
import org.plasma.query.model.Where;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.xml.sax.SAXException;

/**
 * Traverses the given predicate expression syntax tree depth-first 
 * maintaining an internal expression truth table.  
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public class ColumnEntityRecogniser extends ExpresionVisitorSupport 
    implements Recogniser 
{
    private static Log log = LogFactory.getLog(ColumnEntityRecogniser.class);
	private Map<Integer, Map<String, KeyValue>> buckets;
	private Where predicate;
	private boolean rootTruthResult = true;
    private Integer sequence;
    private DataGraphConfig graphConfig;
	private Map<Expression, Boolean> truthMap = new HashMap<Expression, Boolean>();
    private Stack<GroupOperator> groups = new Stack<GroupOperator>();
    private Stack<Expr> expressions = new Stack<Expr>();
    private List<Term> termCache = new ArrayList<Term>();
	
	protected PlasmaType contextType;
	protected PlasmaProperty contextProperty;
	protected Property contextQueryProperty;
	protected Expression contextExpression;
	protected RelationalOperator contextRelationalOperator;	
	
	@SuppressWarnings("unused")
	private ColumnEntityRecogniser() {}
	public ColumnEntityRecogniser(Where predicate,
			DataGraphConfig graphConfig, 
			PlasmaType rootType,
			Map<Integer, Map<String, KeyValue>> buckets) {
		this.graphConfig = graphConfig;
		this.contextType = rootType;
		this.buckets = buckets;
		this.predicate = predicate;
	}
	
	/**
	 * Initiates a depth-first traversal of the {@link #predicate} syntax tree for
	 * the given sequence and returns whether the sequence is part
	 * of the results according to the resulting expression truth table. 
	 * @param sequence the sequence
	 * @return whether the sequence is part
	 * of the results according to the resulting expression truth table. 
	 */
	public boolean recognise(Integer sequence) {
		this.sequence = sequence;
		
    	if (log.isDebugEnabled())
    		log.debug("begin traverse, sequence "
    				+ this.sequence);
    	
    	this.predicate.accept(this); // traverse
    	
    	if (log.isDebugEnabled())
    		log.debug("end traverse, sequence "
    				+ this.sequence);  
    	
    	return this.rootTruthResult;
	}
	
	public void clear() {
		this.truthMap.clear();
		this.groups.clear();
		this.expressions.clear();
		this.termCache.clear();
		this.sequence = null;
	}
	
	@Override
    public void end(org.plasma.query.Where where) {
		Where whereImpl = (Where)where;
		
    	if (whereImpl.getExpressionCount() == 0)
			throw new IllegalStateException("expected root expression");
    	if (whereImpl.getExpressionCount() > 1)
			throw new IllegalStateException("expected single root expression");
    	
    	Boolean result = this.truthMap.get(whereImpl.getExpressions().get(0));
    	if (result == null)
			throw new IllegalStateException("expected truth table result for root expression");
    	
    	this.rootTruthResult = result.booleanValue();
    	
    	if (log.isDebugEnabled())
    		log.debug("traversal result: " + this.rootTruthResult);
    }
    
	/**
	 * Process the traversal end event for a query 
     * {@link org.plasma.query.model.Expression expression} just
     * traversing the property path if exists and capturing context information
     * for the current {@link org.plasma.query.model.Expression expression}.  
	 * @see org.plasma.query.visitor.DefaultQueryVisitor#end(org.plasma.query.model.Expression)
	 */
	@Override
    public void end(Expression expression) {
    	this.contextExpression = expression;
    	process(expression);
    }
		
	private void process(Expression expression) {
    	for (int i = 0; i < expression.getTerms().size(); i++) {
    		Term term = expression.getTerms().get(i);
    		if (term.getGroupOperator() != null) {
    			switch (term.getGroupOperator().getValue()) {
    			case RP_1:
    				this.groups.push(term.getGroupOperator());	
    				break;
    			case LP_1:
    				this.groups.pop();	
    				evaluate();
    				break;
    			case LP_2:
    			case LP_3:
    			case RP_2:
    			case RP_3:
    			default:
    				throw new IllegalStateException("unknown group operator, "
    					+ term.getGroupOperator().getValue());
    			}
    		}
    		else {
    			appendTerm(term);
    		}
    	} // for
    	
    	boolean result = evaluate();    	
		this.truthMap.put(expression, result);
		this.expressions.clear();
		this.termCache.clear();		
	}
	
	private void appendTerm(Term term) {
		this.termCache.add(term);
		if (this.termCache.size() == 3) {
			Term term1 = this.termCache.get(0);
			Term term2 = this.termCache.get(1);
			Term term3 = this.termCache.get(2);	
			if (term1.getExpression() != null && term3.getExpression() != null) {
			    Expr expr = new DefaultLogicalExprPair(term1, term3, 
			    		term2.getLogicalOperator(), truthMap);
			    this.expressions.push(expr);
			    if (log.isDebugEnabled())
			    	log.debug("pushed " + DefaultLogicalExprPair.class.getSimpleName());
			}
			else if (term1.getProperty() != null && term3.getLiteral() != null) {
				String qual = Bytes.toString(this.contextQueryProperty.getPhysicalNameBytes());
				String delim = this.graphConfig.getColumnKeySectionDelimiter();				
				String key = qual + delim + String.valueOf(this.sequence);
				Map<String, KeyValue> seqMap = this.buckets.get(this.sequence);

				Expr expr = new DefaultRelationalBinarylExpr(
						term1.getProperty(), key, term3.getLiteral(), 
						term2.getRelationalOperator(),  
						seqMap);
			    this.expressions.push(expr);
			    if (log.isDebugEnabled())
			    	log.debug("pushed " + DefaultRelationalBinarylExpr.class.getSimpleName());
			}
			else
				throw new IllegalStateException("expected relational binary or logical binary expression pair");
		
			this.termCache.clear();
		}
		else if (this.termCache.size() == 2) {
			Term term1 = this.termCache.get(0);
			Term term2 = this.termCache.get(1);
			if (term1.getLogicalOperator() != null) {
				if (this.expressions.size() == 0)
					throw new IllegalStateException("expected existing left-hand expression");
			    Expr left = this.expressions.peek();
				if (term2.getExpression() == null)
					throw new IllegalStateException("expected right-hand expression, not "
							+ getTermClassName(term2));
				Expr expr = new LeftLogicalExprPair(left, 
					term2.getExpression(), 
			    	term1.getLogicalOperator(), truthMap);
			    this.expressions.push(expr);
			    if (log.isDebugEnabled())
			    	log.debug("pushed " + LeftLogicalExprPair.class.getSimpleName());
				this.termCache.clear();
			}
			//else
				// else keep processing
		}
		// else keep processing
	}
	
	private boolean evaluate() {
		// causes sub-expression evaluation
		return this.expressions.peek().evaluate();
	}
	
	/**
	 * Process the traversal end event for a query {@link org.plasma.query.model.Property property}
     * within an {@link org.plasma.query.model.Expression expression} just
     * traversing the property path if exists and capturing context information
     * for the current {@link org.plasma.query.model.Expression expression}.  
	 * @see org.plasma.query.visitor.DefaultQueryVisitor#end(org.plasma.query.model.Property)
	 */
	@Override
    public void end(Property property)
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
                PathElement namedPathElem = ((PathElement)pathElem);
                PlasmaProperty prop = (PlasmaProperty)targetType.getProperty(namedPathElem.getValue());                
                namedPathElem.setPhysicalNameBytes(prop.getPhysicalNameBytes());                
                targetType = (PlasmaType)prop.getType(); // traverse
            }
        }
        PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
        this.contextProperty = endpointProp;
        this.contextType = targetType;
        this.contextQueryProperty = property;
        
        super.start(property);
    }     
    
	@Override
	public void end(RelationalOperator operator) {
		this.contextRelationalOperator = operator;
	}
	
	private String getTermClassName(Term term) {
		if (term.getWildcardProperty() != null)
			return term.getWildcardProperty().getClass().getSimpleName();
		else if (term.getSubqueryOperator() != null)
			return term.getSubqueryOperator().getClass().getSimpleName();
		else if (term.getWildcardOperator() != null)
			return term.getWildcardProperty().getClass().getSimpleName();
		else if (term.getEntity() != null)
			return term.getEntity().getClass().getSimpleName();
		else if (term.getExpression() != null)
			return term.getExpression().getClass().getSimpleName();
		else if (term.getVariable() != null)
			return term.getVariable().getClass().getSimpleName();
		else if (term.getNullLiteral() != null)
			return term.getNullLiteral().getClass().getSimpleName();
		else if (term.getLiteral() != null)
			return term.getLiteral().getClass().getSimpleName();
		else if (term.getGroupOperator() != null)
			return term.getGroupOperator().getClass().getSimpleName();
		else if (term.getArithmeticOperator() != null)
			return term.getArithmeticOperator().getClass().getSimpleName();
		else if (term.getRelationalOperator() != null)
			return term.getRelationalOperator().getClass().getSimpleName();
		else if (term.getLogicalOperator() != null)
			return term.getLogicalOperator().getClass().getSimpleName();
		else if (term.getProperty() != null)
			return term.getProperty().getClass().getSimpleName();
		else  
			return term.getQuery().getClass().getSimpleName();
	}
	
    protected void log(Expression expr)
    {
        log.debug("expr: " + serialize(expr));
    }
    
    protected String serialize(Expression expr)
    {
    	String xml = "";
        PlasmaQueryDataBinding binding;
		try {
			binding = new PlasmaQueryDataBinding(
			    new DefaultValidationEventHandler());
	        xml = binding.marshal(expr);
		} catch (JAXBException e) {
			log.debug(e);
		} catch (SAXException e) {
			log.debug(e);
		}
		return xml;
    }
}
