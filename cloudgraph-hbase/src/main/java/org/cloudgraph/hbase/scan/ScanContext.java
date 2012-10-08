package org.cloudgraph.hbase.scan;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.filter.GraphFilterException;
import org.cloudgraph.common.key.KeyValue;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.UserDefinedFieldConfig;
import org.plasma.query.QueryException;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Where;
import org.plasma.query.model.WildcardOperator;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;

/**
 * Creates context
 * information useful for determining an HBase scan strategy
 */
public class ScanContext extends DefaultQueryVisitor {
    
	private static Log log = LogFactory.getLog(ScanContext.class);
	
    protected PlasmaType contextType;
	protected PlasmaProperty contextProperty;
	protected String contextPropertyPath;
	protected PlasmaType rootType;
	protected DataGraphConfig graph;
	protected List<KeyValue> pairs = new ArrayList<KeyValue>();
	protected boolean hasWildcardOperators;
	protected boolean hasContiguousFieldValues;
	protected boolean hasOnlyPartialKeyScanSupportedLogicalOperators = true;
	protected boolean hasOnlyPartialKeyScanSupportedRelationalOperators = true;
	
    @SuppressWarnings("unused")
	private ScanContext() {}
    public ScanContext(PlasmaType rootType, Where where)
    {
    	this.rootType = rootType;
		QName rootTypeQname = this.rootType.getQualifiedName();
		this.graph = CloudGraphConfig.getInstance().getDataGraph(
				rootTypeQname);
    	if (log.isDebugEnabled())
    		log.debug("begin traverse");
    	
    	where.accept(this); // traverse
    	
    	if (log.isDebugEnabled())
    		log.debug("end traverse");  
    	
    	construct();
    }
    
	private void construct() {
    	if (this.pairs.size() == 0)
    		throw new IllegalStateException("no literals found in predicate");
    	this.hasContiguousFieldValues = true;
    	int size = this.graph.getUserDefinedRowKeyFields().size();
    	KeyValue[] keyValues = new KeyValue[size];
    	
    	for (int i = 0; i < size; i++) {
    		UserDefinedFieldConfig fieldConfig = this.graph.getUserDefinedRowKeyFields().get(i); 
    		KeyValue keyValue = findKeyValue(fieldConfig);
    		keyValues[i] = keyValue;
    	}
    	
    	for (int i = 0; i < size-1; i++)
    		if (keyValues[i] == null && keyValues[i+1] != null)
    			this.hasContiguousFieldValues = false;    	
    }
    
	public List<KeyValue> getKeyValues() {
		return pairs;
	}
	
	public boolean canUsePartialKeyScan() {
		return this.hasWildcardOperators == false &&
			this.hasContiguousFieldValues == true &&
			this.hasOnlyPartialKeyScanSupportedLogicalOperators == true &&
			this.hasOnlyPartialKeyScanSupportedRelationalOperators == true;		
	}
    
    public boolean hasWildcardOperators() {
		return hasWildcardOperators;
	}
    
	public boolean hasContiguousFieldValues() {
		return hasContiguousFieldValues;
	}
	
	public boolean hasOnlyPartialKeyScanSupportedLogicalOperators() {
		return hasOnlyPartialKeyScanSupportedLogicalOperators;
	} 
	
	public boolean hasOnlyPartialKeyScanSupportedRelationalOperators() {
		return hasOnlyPartialKeyScanSupportedRelationalOperators;
	}
	
    private KeyValue findKeyValue(UserDefinedFieldConfig fieldConfig) {
    	
    	commonj.sdo.Property fieldProperty = fieldConfig.getEndpointProperty();
    	commonj.sdo.Type fieldPropertyType = fieldProperty.getContainingType();
    	
		for (KeyValue keyValue : this.pairs) {
			if (keyValue.getProp().getName().equals(fieldConfig.getEndpointProperty().getName())) {
				if (keyValue.getProp().getContainingType().getName().equals(fieldPropertyType.getName())) {
					if (keyValue.getProp().getContainingType().getURI().equals(fieldPropertyType.getURI())) {
					    if (fieldConfig.getPropertyPath() != null) {
					    	if (keyValue.getPropertyPath() != null && 
					    		keyValue.getPropertyPath().equals(fieldConfig.getPropertyPath())) {
					    		return keyValue;
					    	}
					    }
					}					
				}
			}
		}
		return null;
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
            throw new GraphServiceException("aggregate functions only supported in subqueries not primary queries");
          
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
			this.hasWildcardOperators = true;
			break;
		default:
			throw new GraphServiceException("unknown operator '"
					+ operator.getValue().toString() + "'");
		}
		super.start(operator);
	}	
	
    /**
     * Process the traversal start event for a query {@link org.plasma.query.model.Literal literal}
     * within an {@link org.plasma.query.model.Expression expression} creating context
     * information useful for determining an HBase scan strategy.
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

		// Match the current property to a user defined 
		// row key token, if found we can process
		if (this.graph.getUserDefinedRowKeyField(this.contextPropertyPath) != null) 
		{
			KeyValue pair = new KeyValue(
					this.contextProperty, 
					content);			
			pair.setPropertyPath(this.contextPropertyPath);			
			pairs.add(pair);
		}
		else
	        throw new GraphServiceException("no user defined row-key field for query path '"
			    	+ this.contextPropertyPath + "'");
		
		super.start(literal);
	}

	/**
	 * Process a {@link org.plasma.query.model.LogicalOperator logical operator} query traversal
	 * start event. 
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
