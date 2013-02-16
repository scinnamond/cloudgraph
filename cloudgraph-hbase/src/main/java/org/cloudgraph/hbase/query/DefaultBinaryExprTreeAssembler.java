package org.cloudgraph.hbase.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.filter.GraphFilterException;
import org.cloudgraph.hbase.filter.ExpresionVisitorSupport;
import org.cloudgraph.hbase.key.CompositeColumnKeyFactory;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.Expression;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.GroupOperatorValues;
import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.LogicalOperatorValues;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.RelationalOperatorValues;
import org.plasma.query.model.Term;
import org.plasma.query.model.Where;
import org.plasma.query.model.WildcardOperator;
import org.plasma.query.model.WildcardOperatorValues;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.xml.sax.SAXException;

/**
 * A binary expression tree assembler which constructs an operator 
 * precedence map, then {@link org.cloudgraph.hbase.filter.ExpresionVisitorSupport visits} (traverses) 
 * the given predicate expression syntax tree depth-first 
 * using an adapted shunting-yard algorithm and assembles a 
 * resulting binary tree structure. In typical usage scenarios, a single 
 * expression tree is assembled once, and then used to evaluate any 
 * number of graph edge or other results based on a given context.
 * <p>
 * The adapted shunting-yard algorithm in general uses a stack of 
 * operators and operands, and as new binary tree nodes are detected and 
 * created they are pushed onto the operand stack based on operator precedence.
 * The resulting binary expression tree reflects the syntax of the
 * underlying query expression including the precedence of its operators.
 * </p>
 * <p>
 * The use of binary expression tree evaluation for post processing 
 * of graph edge results is necessary in columnar data stores, as an 
 * entity with multiple properties is necessarily persisted across multiple
 * columns. And while these data stores provide many useful column oriented
 * filters, the capability to select an entity based on complex criteria
 * which spans several columns is generally not supported, as such filters are
 * column oriented. Yet even for simple queries (e.g. "where entity.c1 = 'foo' 
 * and entity.c2 = 'bar'") column c1 and its value exists in one cell and
 * column c2 exists in another table cell. Since columnar data store
 * filters cannot generally span columns, both cells must be returned
 * and the results post processed within the context of the binary 
 * expression tree.      
 * </p>
 * <p>
 * Subclasses may provide alternate implementations of {@link ExprAssembler}
 * which create binary expression tree nodes with specific evaluation
 * behavior.  
 * </p>
 *   
 * @author Scott Cinnamond
 * @since 0.5.2
 * 
 * @see Expr
 * @see CompositeColumnKeyFactory
 * 
 */
public abstract class DefaultBinaryExprTreeAssembler extends ExpresionVisitorSupport 
    implements ExprAssembler
{
    private static Log log = LogFactory.getLog(DefaultBinaryExprTreeAssembler.class);
    private Stack<Operator> operators = new Stack<Operator>();
    private Stack<org.plasma.query.Term> operands = new Stack<org.plasma.query.Term>();
	private Map<Object, Integer> precedenceMap = new HashMap<Object, Integer>();
	private Map<Expression, Expr> exprMap = new HashMap<Expression, Expr>();
    private CompositeColumnKeyFactory columnKeyFactory;
	
	protected Where predicate;
	protected PlasmaType rootType;
	protected PlasmaType edgeType;
	protected PlasmaType contextType;
	protected PlasmaProperty contextProperty;
	protected Property contextQueryProperty;
	protected Expression contextExpression;
	
	@SuppressWarnings("unused")
	private DefaultBinaryExprTreeAssembler() {}
	
	/**
	 * Constructs an assembler based on the given predicate
	 * and graph edge type.  
	 * @param predicate the predicate
	 * @param edgeType the graph edge type which is the type for the
	 * reference property within the graph which represents an edge
	 * @param rootType the graph root type
	 */
	public DefaultBinaryExprTreeAssembler(Where predicate,
			PlasmaType edgeType, PlasmaType rootType) {
		this.edgeType = edgeType;
		this.rootType = rootType;
		this.predicate = predicate;

		this.columnKeyFactory = 
        		new CompositeColumnKeyFactory(this.rootType);

		precedenceMap.put(LogicalOperatorValues.OR, 0);
		precedenceMap.put(LogicalOperatorValues.AND, 1);
		precedenceMap.put(WildcardOperatorValues.LIKE, 2);
		precedenceMap.put(RelationalOperatorValues.EQUALS, 2);
		precedenceMap.put(RelationalOperatorValues.NOT_EQUALS, 2);
		precedenceMap.put(RelationalOperatorValues.GREATER_THAN, 2);
		precedenceMap.put(RelationalOperatorValues.GREATER_THAN_EQUALS, 2);
		precedenceMap.put(RelationalOperatorValues.LESS_THAN, 2);
		precedenceMap.put(RelationalOperatorValues.LESS_THAN_EQUALS, 2);
		precedenceMap.put(GroupOperatorValues.LP_1, 3);
		precedenceMap.put(GroupOperatorValues.RP_1, 4);
	}
	
	/**
	 * Returns the binary expression tree result
	 * @return the binary expression tree result
	 */
	public Expr getResult() {
    	if (log.isDebugEnabled())
    		log.debug("begin traverse");
    	
    	this.predicate.accept(this); // traverse
    	
    	if (log.isDebugEnabled())
    		log.debug("end traverse"); 
    	
    	Expression root = this.predicate.getExpressions().get(0);
    	Expr result = this.exprMap.get(root);
    	
    	if (log.isDebugEnabled()) {
    	    BinaryExpr binaryExpr = (BinaryExpr)result;
    	    ExprPrinter printer = new ExprPrinter();
    	    binaryExpr.accept(printer);
    		log.debug("expr: " + printer.toString());
    	}
    	
		return result;
	}
	    
	/**
	 * Process the traversal end event for a query 
     * {@link org.plasma.query.model.Expression expression} consuming
     * each term in the {@link org.plasma.query.model.Expression expression}.  
	 * @see org.plasma.query.visitor.DefaultQueryVisitor#end(org.plasma.query.model.Expression)
	 */
	@Override
    public void end(Expression expression) {
    	this.contextExpression = expression;
    	assemble(expression);
    }
	
	/**
	 * Consumes each term in the given {@link org.plasma.query.model.Expression expression}
	 * then assembles any remaining terms, mapping the resulting
	 * {@link Expr expression}. 
	 * @param expression the expression
	 */
	private void assemble(Expression expression) {
    	for (int i = 0; i < expression.getTerms().size(); i++) {
    		Term term = expression.getTerms().get(i);
			consume(term);
    	} // for
    	Expr expr = assemble();
    	exprMap.put(expression, expr);
    	if (log.isDebugEnabled())
    		log.debug("mapped: " + expr);    	
	}
	
	/**
	 * Processes operators and operands currently staged returning
	 * an appropriate expression.
	 * @return the expression
	 */
	//FIXME: does not support arithmetic or unary expressions
	private Expr assemble() {
		Expr expr = null;
	    if (log.isDebugEnabled())
	    	log.debug("assembling " + this.operands.size() + " operands "
	    			+ this.operators.size() + " operators");
		
		if (this.operands.peek() instanceof Literal) {
			Literal literal = (Literal)this.operands.pop(); 
			Property prop = (Property)this.operands.pop();  
			Operator oper = this.operators.pop();
			if (oper.getOperator() instanceof RelationalOperator)
			    expr = createRelationalBinaryExpr(
					prop, literal, 
					(RelationalOperator)oper.getOperator());
			else if (oper.getOperator() instanceof WildcardOperator) {
			    expr = createWildcardBinaryExpr(
					prop, literal, 
					(WildcardOperator)oper.getOperator());
			}
			else
				throw new IllegalStateException("unknown operator, "
					+ oper.toString());
		}
		else if (this.operands.peek() instanceof Expr) {
			Expr right = (Expr)this.operands.pop();  
            if (this.operands.peek() instanceof Expr) {
			    Expr left = (Expr)this.operands.pop();
				Operator oper = this.operators.pop();
				LogicalOperator logicalOper = (LogicalOperator)oper.getOperator();
				expr = createLogicalBinaryExpr(left, 
						right, logicalOper);
			}
			else
			    throw new IllegalStateException("unknown operand, "
					+ this.operands.peek().toString());
		}
		else
			throw new IllegalStateException("unknown opearand, " + this.operands.peek());
		
		if (this.operators.size() > 0) {
		    Operator oper = this.operators.peek();
		    if (oper.getOperator() instanceof GroupOperator) {
		    	GroupOperator group = (GroupOperator)oper.getOperator();
		    	if (group.getValue().ordinal() == GroupOperatorValues.RP_1.ordinal())
		    		this.operators.pop();
		    }
		}
		
		return expr;
	}
	
	/**
	 * Consumes the given term, pushing operators and operands
	 * onto their respective stack and assembling
	 * binary tree nodes based on operator precedence. 
	 * @param term the term
	 */
	private void consume(Term term) {
		if (term.getExpression() != null) {
			Expr expr = this.exprMap.get(term.getExpression());
			this.operands.push(expr);
		    if (log.isDebugEnabled())
		    	log.debug("pushed " + expr);
		}
		else if (term.getProperty() != null) {
			this.operands.push(term.getProperty());
		    if (log.isDebugEnabled())
		    	log.debug("pushed " + term.getProperty().getClass().getSimpleName());
		}
		else if (term.getLiteral() != null) {
			this.operands.push(term.getLiteral());
		    if (log.isDebugEnabled())
		    	log.debug("pushed " + term.getLiteral().getClass().getSimpleName());
		}
		// assemble a node based on operator precedence
		else if (term.getGroupOperator() != null || term.getLogicalOperator() != null || term.getRelationalOperator() != null || term.getWildcardOperator() != null) {
			Operator oper = null;
			if (term.getGroupOperator() != null)
			    oper = new Operator(term.getGroupOperator(), this.precedenceMap);
			else if (term.getLogicalOperator() != null)
		        oper = new Operator(term.getLogicalOperator(), this.precedenceMap); 	
			else if (term.getWildcardOperator() != null)
		        oper = new Operator(term.getWildcardOperator(), this.precedenceMap); 	
			else	
		        oper = new Operator(term.getRelationalOperator(), this.precedenceMap); 	
				
			if (this.operators.size() > 0) {
				Operator existing = this.operators.peek();
			    if (log.isDebugEnabled())
			    	log.debug("comparing " + existing + " and " + oper);
				if (existing.compareTo(oper) <= 0) {
					Expr expr = assemble();
				    this.operands.push(expr);
				    if (log.isDebugEnabled())
				    	log.debug("pushed expr node: " + expr);
				}
				else {
				    this.operators.push(oper);
			        if (log.isDebugEnabled())
			    	    log.debug("pushed " + oper);
				}
			}
			else {
			    this.operators.push(oper);
		        if (log.isDebugEnabled())
		    	    log.debug("pushed " + oper);
			}
		}
		else
			throw new IllegalStateException("unexpected term"  
				+ getTermClassName(term));
	}
	
	/**
	 * Creates and returns a relational binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a>
	 * operator.
	 * @param property the property term
	 * @param literal the literal term
	 * @param operator the <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a> operator
	 * @return a relational binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/RelationalOperator.html">relational</a>
	 * operator.
	 */
	@Override
	public RelationalBinaryExpr createRelationalBinaryExpr(Property property,
			Literal literal, RelationalOperator operator) {
	    return new DefaultRelationalBinaryExpr(
	    		property, literal, operator);
	}
	
    /**
	 * Creates and returns a wildcard binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/WildcardOperator.html">wildcard</a>
	 * operator.
	 * @param property the property term
	 * @param literal the literal term
	 * @param operator the <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/WildcardOperator.html">wildcard</a> operator
	 * @return a wildcard binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/WildcardOperator.html">wildcard</a>
	 * operator.
	 */
	@Override
	public WildcardBinaryExpr createWildcardBinaryExpr(Property property,
			Literal literal, WildcardOperator operator) {
	    return new DefaultWildcardBinaryExpr(
	    		property, literal, operator);
	}	
	
    /**
	 * Creates and returns a logical binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/LogicalOperator.html">logical</a>
	 * operator.
	 * @param property the property term
	 * @param literal the literal term
	 * @param operator the <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/LogicalOperator.html">logical</a> operator
	 * @return a wildcard binary expression based on the
	 * given terms and <a href="http://docs.plasma-sdo.org/api/org/plasma/query/model/LogicalOperator.html">logical</a>
	 * operator.
	 */
	@Override
	public LogicalBinaryExpr createLogicalBinaryExpr(Expr left, Expr right,
			LogicalOperator operator) {
		return new DefaultLogicalBinaryExpr(left, 
				right, operator);
	}
	
		
	/**
	 * Process the traversal end event for a query {@link org.plasma.query.model.Property property}
     * within an {@link org.plasma.query.model.Expression expression} setting up
     * context information for the endpoint property and its type, as well as
     * physical column qualifier name bytes which are set into the {@link #contextQueryProperty}
     * physical name bytes. 
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
        PlasmaType targetType = (PlasmaType)this.edgeType;                
        if (path != null)
            throw new GraphFilterException("property paths not supported within path predicate expressions");
        	
        PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
        this.contextProperty = endpointProp;
        this.contextType = targetType;
        this.contextQueryProperty = property;
        byte[] columnKey = this.columnKeyFactory.createColumnKey(this.edgeType, 
        		this.contextProperty);
        this.contextQueryProperty.setPhysicalNameBytes(columnKey);                
        
        super.start(property);
    }     
    
	private String getTermClassName(Term term) {
		if (term.getWildcardProperty() != null)
			return term.getWildcardProperty().getClass().getSimpleName();
		else if (term.getSubqueryOperator() != null)
			return term.getSubqueryOperator().getClass().getSimpleName();
		else if (term.getWildcardOperator() != null)
			return term.getWildcardOperator().getClass().getSimpleName();
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
