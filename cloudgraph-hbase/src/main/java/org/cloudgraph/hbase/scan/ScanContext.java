package org.cloudgraph.hbase.scan;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.UserDefinedFieldConfig;
import org.plasma.query.QueryException;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Where;
import org.plasma.query.model.WildcardOperator;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.sdo.PlasmaType;

/**
 * Conducts an initial traversal while capturing and 
 * analyzing the characteristics of a query in order to 
 * leverage the important HBase partial row-key scan
 * capability for every possible predicate expression. 
 * <p>
 * Based on the various access methods, if a client determines that
 * an HBase partial row-key scan is possible based, a {@link PartialRowKeyScanAssembler} may 
 * be invoked using the current scan context resulting is a precise set of 
 * composite start/stop row keys. These are used in HBase client <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Scan.html">scan</a> API
 * start and stop row.
 * </p>
 */
public class ScanContext extends DefaultQueryVisitor {
    
	private static Log log = LogFactory.getLog(ScanContext.class);
	
    //protected PlasmaType contextType;
	//protected PlasmaProperty contextProperty;
	//protected String contextPropertyPath;
	protected PlasmaType rootType;
	protected DataGraphConfig graph;
	protected List<ScanLiteral> literals = new ArrayList<ScanLiteral>();
	//protected RelationalOperator contextRelationalOperator;
	//protected LogicalOperator contextLogicalOperator;
	protected boolean hasWildcardOperators;
	protected boolean hasContiguousFieldValues;
	protected boolean hasOnlyPartialKeyScanSupportedLogicalOperators = true;
	protected boolean hasOnlyPartialKeyScanSupportedRelationalOperators = true;
	
    @SuppressWarnings("unused")
	private ScanContext() {}

    /**
     * Conducts an initial traversal while capturing and 
     * analyzing the characteristics of a query in order to 
     * leverage the important HBase partial row-key scan
     * capability for every possible predicate expression. 
     * @param rootType the root type
     * @param where the predicates
     */
    public ScanContext(PlasmaType rootType, Where where)
    {
    	this.rootType = rootType;
		QName rootTypeQname = this.rootType.getQualifiedName();
		this.graph = CloudGraphConfig.getInstance().getDataGraph(
				rootTypeQname);
    	if (log.isDebugEnabled())
    		log.debug("begin traverse");
    	
		ScanLiteralAssembler literalAssembler = 
				new ScanLiteralAssembler(this.rootType);
    	where.accept(literalAssembler); // traverse
    	this.literals.addAll(literalAssembler.getLiteralList());
    	
    	where.accept(this); // traverse
    	
    	if (log.isDebugEnabled())
    		log.debug("end traverse");  
    	
    	construct();
    }
    
	private void construct() {
    	if (this.literals.size() == 0)
    		throw new IllegalStateException("no literals found in predicate");
    	this.hasContiguousFieldValues = true;
    	int size = this.graph.getUserDefinedRowKeyFields().size();
    	ScanLiteral[] scanLiterals = new ScanLiteral[size];
    	
    	for (int i = 0; i < size; i++) {
    		UserDefinedFieldConfig fieldConfig = this.graph.getUserDefinedRowKeyFields().get(i); 
    		ScanLiteral literal = findScanLiteral(fieldConfig);
    		scanLiterals[i] = literal;
    	}
    	
    	for (int i = 0; i < size-1; i++)
    		if (scanLiterals[i] == null && scanLiterals[i+1] != null)
    			this.hasContiguousFieldValues = false;    	
    }
    
	/**
	 * Return the current scan literals.
	 * @return the current scan literals.
	 */
	public List<ScanLiteral> getLiterals() {
		return literals;
	}
	
	/**
	 * Returns whether an HBase partial row-key scan is possible
	 * under the current scan context. 
	 * @return whether an HBase partial row-key scan is possible
	 * under the current scan context.
	 */
	public boolean canUsePartialKeyScan() {
		return this.hasWildcardOperators == false &&
			this.hasContiguousFieldValues == true &&
			this.hasOnlyPartialKeyScanSupportedLogicalOperators == true &&
			this.hasOnlyPartialKeyScanSupportedRelationalOperators == true;		
	}
    
	/**
	 * Returns whether underlying query contains wildcard operators.
	 * @return whether underlying query contains wildcard operators.
	 */
    public boolean hasWildcardOperators() {
		return hasWildcardOperators;
	}
    
    /**
     * Returns whether the underlying query predicates represent
     * a contiguous set of composite row-key fields making a partial
     * row-key scan possible. 
     * @return whether the underlying query predicates represent
     * a contiguous set of composite row-key fields making a partial
     * row-key scan possible. 
     */
	public boolean hasContiguousFieldValues() {
		return hasContiguousFieldValues;
	}
	
	/**
	 * Returns whether the underlying query contains only
	 * logical operators supportable for under a partial
     * row-key scan.   
	 * @return whether the underlying query contains only
	 * logical operators supportable for under a partial
     * row-key scan.
	 */
	public boolean hasOnlyPartialKeyScanSupportedLogicalOperators() {
		return hasOnlyPartialKeyScanSupportedLogicalOperators;
	} 
	
	/**
	 * Returns whether the underlying query contains only
	 * relational operators supportable for under a partial
     * row-key scan.   
	 * @return whether the underlying query contains only
	 * relational operators supportable for under a partial
     * row-key scan.
	 */
	public boolean hasOnlyPartialKeyScanSupportedRelationalOperators() {
		return hasOnlyPartialKeyScanSupportedRelationalOperators;
	}
	
    private ScanLiteral findScanLiteral(UserDefinedFieldConfig fieldConfig) {
		for (ScanLiteral literal : this.literals) {
			if (literal.getFieldConfig().getSequenceNum() == fieldConfig.getSequenceNum())
				return literal;
		}
		return null;
    }    
    
    /**
     * Process the traversal start event for a query {@link org.plasma.query.model.WildcardOperator WildcardOperator}
     * within an {@link org.plasma.query.model.Expression expression} creating context
     * information useful for determining an HBase scan strategy.
     * @param literal the expression literal
     * @throws GraphServiceException if an unknown wild card operator
     * is encountered.
     */
	public void start(WildcardOperator operator) {
		switch (operator.getValue()) {
		case LIKE:
			this.hasWildcardOperators = true;
			break;
		default:
			throw new GraphServiceException("unknown operator '"
					+ operator.getValue().toString() + "'");
		}
		super.start(operator);
	}	
	
    /**
     * Process the traversal start event for a query {@link org.plasma.query.model.LogicalOperator LogicalOperator}
     * within an {@link org.plasma.query.model.Expression expression} creating context
     * information useful for determining an HBase scan strategy.
     * @param literal the expression literal
     * @throws GraphServiceException if an unknown logical operator
     * is encountered.
     */
	public void start(LogicalOperator operator) {
		
		switch (operator.getValue()) {
		case AND:
			break; 
		case OR:	
			this.hasOnlyPartialKeyScanSupportedLogicalOperators = false;
			break;
		}
		super.start(operator);
	}
    
    /**
     * Process the traversal start event for a query {@link org.plasma.query.model.RelationalOperator RelationalOperator}
     * within an {@link org.plasma.query.model.Expression expression} creating context
     * information useful for determining an HBase scan strategy.
     * @param literal the expression literal
     * @throws GraphServiceException if an unknown relational operator
     * is encountered.
     */
	public void start(RelationalOperator operator) {
		switch (operator.getValue()) {
		case EQUALS:
		case GREATER_THAN:
		case GREATER_THAN_EQUALS:
		case LESS_THAN:
		case LESS_THAN_EQUALS:
			break;
		case NOT_EQUALS:
			this.hasOnlyPartialKeyScanSupportedRelationalOperators = false;
			break;
		default:
			throw new QueryException("unknown operator '"
					+ operator.getValue().toString() + "'");
		}
		super.start(operator);
	}
	
    /**
     * Process the traversal start event for a query {@link org.plasma.query.model.GroupOperator GroupOperator}
     * within an {@link org.plasma.query.model.Expression expression} creating context
     * information useful for determining an HBase scan strategy.
     * @param literal the expression literal
     * @throws GraphServiceException if an unknown group operator
     * is encountered.
     */
	public void start(GroupOperator operator) {
		switch (operator.getValue()) {
		case RP_1:  		
	        break;
		case RP_2:  		
		    break;
		case RP_3:  			
			break;
		case LP_1:  
			break;
		case LP_2:  			
			break;
		case LP_3:  
			break;
		default:
			throw new QueryException("unknown group operator, "
						+ operator.getValue().name());
		}
		super.start(operator);
	}
}