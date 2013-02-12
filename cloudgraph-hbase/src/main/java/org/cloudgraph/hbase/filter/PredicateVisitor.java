package org.cloudgraph.hbase.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.plasma.query.QueryException;
import org.plasma.query.model.Expression;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Term;
import org.plasma.query.model.WildcardOperator;
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
 * @author Scott Cinnamond
 * @since 0.5
 */
public abstract class PredicateVisitor extends FilterHierarchyAssembler {
    private static Log log = LogFactory.getLog(PredicateVisitor.class);
	protected PlasmaType contextType;
	protected PlasmaProperty contextProperty;
	protected CompareFilter.CompareOp contextHBaseCompareOp;
	protected boolean contextOpWildcard;
	protected WildcardOperator contextWildcardOperator;

	protected PredicateVisitor(PlasmaType rootType) {
		super(rootType);
	}

	public void clear() {
		super.clear();
		this.contextType = null;
		this.contextProperty = null;
		this.contextHBaseCompareOp = null;
		this.contextOpWildcard = false;
		this.contextWildcardOperator = null;
	}	
    
	/**
	 * Returns true if the given expression has any immediate
	 * child property-expressions where the properties are 
	 * heterogeneous i.e. more than one distinct property.
	 * @param expression the expression
	 * @return true if the given expression has any immediate
	 * child property-expressions where the properties are 
	 * heterogeneous.
	 */
    //FIXME: does not address paths
    protected boolean hasHeterogeneousChildProperties(Expression expression) {
    	String firstName = null;
    	
    	for (Term term : expression.getTerms())
			if (term.getExpression() != null) {
				Expression childExpr = term.getExpression();
		    	for (Term childTerm : childExpr.getTerms())
					if (childTerm.getProperty() != null) {
						Property childProperty = childTerm.getProperty();
						if (firstName == null) {
							firstName = childProperty.getName();
						}
						else {
							if (!firstName.equals(childProperty.getName()))
								return true;
						}
					}
			}
    	
    	return false;
	}
    
    protected boolean hasHeterogeneousDescendantProperties(Expression expression) {
    	String firstName = null;
    	Property[] props = findProperties(expression);
    	for (Property prop : props) {
			if (firstName == null) {
				firstName = prop.getName();
			}
			else {
				if (!firstName.equals(prop.getName()))
					return true;
			}
    	}
    	return false;
    }
    
    protected Property[] findProperties(Expression expression) {
    	List<Property> list = new ArrayList<Property>();
    	collectProperties(expression, list);
    	Property[] result = new Property[list.size()];
    	list.toArray(result);
    	return result;
    }
    
	protected void collectProperties(Expression expression, List<Property> list) {
		for (Term term : expression.getTerms()) {
			if (term.getExpression() != null) 
				collectProperties(term.getExpression(), list);
			else if (term.getProperty() != null) 
				list.add(term.getProperty());
		}
	}

	public void start(RelationalOperator operator) {

		this.contextOpWildcard = false;
		this.contextWildcardOperator = null;
		
		switch (operator.getValue()) {
		case EQUALS:
			this.contextHBaseCompareOp = CompareFilter.CompareOp.EQUAL;
			break;
		case NOT_EQUALS:
			this.contextHBaseCompareOp = CompareFilter.CompareOp.NOT_EQUAL;
			break;
		case GREATER_THAN:
			this.contextHBaseCompareOp = CompareFilter.CompareOp.GREATER;
			break;
		case GREATER_THAN_EQUALS:
			this.contextHBaseCompareOp = CompareFilter.CompareOp.GREATER_OR_EQUAL;
			break;
		case LESS_THAN:
			this.contextHBaseCompareOp = CompareFilter.CompareOp.LESS;
			break;
		case LESS_THAN_EQUALS:
			this.contextHBaseCompareOp = CompareFilter.CompareOp.LESS_OR_EQUAL;
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

}
