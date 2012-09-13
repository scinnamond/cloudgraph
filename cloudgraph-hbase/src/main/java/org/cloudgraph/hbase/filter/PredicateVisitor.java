package org.cloudgraph.hbase.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.plasma.query.QueryException;
import org.plasma.query.model.Expression;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.Literal;
import org.plasma.query.model.QueryConstants;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Term;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;

/**
 * Processes visitor events for query model elements common to both row and column
 * filters, such as relational and group operators, within 
 * the context of HBase filter hierarchy assembly and maintains
 * various context information useful to subclasses.  
 * <p>
 * HBase filters may be collected into 
 * lists using <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.html" target="#">FilterList</a>
 * each with a 
 * <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ALL" target="#">MUST_PASS_ALL</a> or <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ONE" target="#">MUST_PASS_ONE</a>
 *  (logical) operator. Lists may then be assembled into hierarchies 
 * used to represent complex expression trees filtering either rows
 * or columns in HBase.
 * </p> 
 */
public abstract class PredicateVisitor extends FilterHierarchyAssembler {
    private static Log log = LogFactory.getLog(PredicateVisitor.class);
	protected PlasmaType contextType;
	protected PlasmaProperty contextProperty;
	protected CompareFilter.CompareOp contextOp;
	protected boolean contextOpWildcard;

	protected PredicateVisitor(PlasmaType rootType) {
		super(rootType);
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
