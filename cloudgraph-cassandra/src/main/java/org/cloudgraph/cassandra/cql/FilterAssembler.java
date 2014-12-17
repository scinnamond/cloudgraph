/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.cassandra.cql;

// java imports
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.service.GraphServiceException;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.QueryException;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.Expression;
import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.QueryConstants;
import org.plasma.query.model.SubqueryOperator;
import org.plasma.query.model.Where;
import org.plasma.query.model.WildcardOperator;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.query.visitor.Traversal;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.provider.common.SQLQueryFilterAssembler;
import org.plasma.sdo.helper.DataConverter;
import org.plasma.sdo.profile.KeyType;
import org.xml.sax.SAXException;

import commonj.sdo.Type;

public class FilterAssembler extends SQLQueryFilterAssembler
    implements QueryConstants 
{
    private static Log log = LogFactory.getLog(FilterAssembler.class);

    private Map variableMap;
    private StringBuffer variableDecls;
    private String importDecls;
    private String parameterDecls;
    private int variableDeclCount = 0;
    private int subqueryCount = 0;
	protected Expression contextExpression;
	protected LogicalOperator contextLogicalOperator;
	protected boolean contextPropertyIsPath;
    

    public FilterAssembler(Where where,
        Type contextType)
    {
        super(contextType);
         
        
        if (where.getTextContent() == null && where.getFilterId() == null)
        {
        	if (where.getImportDeclaration() != null)
        		throw new DataAccessException("import declaration allowed only for 'free-text' Where clause");
        	if (where.getParameters().size() > 0)
        		throw new DataAccessException("parameters allowed only for 'free-text' Where clause");
        	if (where.getParameterDeclaration() != null)
        		throw new DataAccessException("parameter declarations allowed only for 'free-text' Where clause");
        	if (where.getVariableDeclaration() != null)
        		throw new DataAccessException("import declarations allowed only for 'free-text' Where clause");
                 
        	if (log.isDebugEnabled())
        		log(where);
        	this.filter.append(" WHERE ");
        	where.accept(this); // traverse        	
        }
        else
        {
        	for (int i = 0; i < where.getParameters().size(); i++)
        		params.add(where.getParameters().get(i).getValue());
        	
        	if (where.getImportDeclaration() != null)
        		importDecls = where.getImportDeclaration().getValue();
        	if (where.getParameterDeclaration() != null)
        		parameterDecls = where.getParameterDeclaration().getValue();
        	if (where.getVariableDeclaration() != null)
        	{	
        		if (variableDecls == null)
        			variableDecls = new StringBuffer();
        		variableDecls.append(where.getVariableDeclaration().getValue());
        	}
         	if (where.getTextContent() != null)
         	{
        	    filter.append(where.getTextContent().getValue());
        	}
        	else
        	    throw new QueryException("expected free-text content or filter id");
        }
    }

    public String getVariableDeclarations() { return variableDecls.toString(); }
    public boolean hasVariableDeclarations() { 
    	return variableDecls != null && variableDecls.length() > 0; 
    }
    public String getImportDeclarations() { return importDecls; }
    public boolean hasImportDeclarations() { 
    	return importDecls != null && importDecls.length() > 0; 
    }
    public String getParameterDeclarations() { return parameterDecls; }
    public boolean hasParameterDeclarations() { 
    	return parameterDecls != null && parameterDecls.length() > 0; 
    }    
    
    @Override
	public void start(Expression expression)
    {
		this.contextExpression = expression;
		
        //log.trace("visit Expression");
        // THIS NEEDS REFACTOING
        for (int i = 0; i < expression.getTerms().size(); i++)
        {
            SubqueryOperator subqueryOper = expression.getTerms().get(i).getSubqueryOperator();
            if (subqueryOper != null)
            {
                //log.info("found subquery expression");
                Property property = expression.getTerms().get(i-1).getProperty();
                Query query = (Query)expression.getTerms().get(i+1).getQuery(); 
                assembleSubquery(property, subqueryOper, query);
                subqueryCount++;
                this.getContext().setTraversal(Traversal.ABORT);
                // abort traversal as vanilla expression    
            }
        } 
		super.start(expression);
    }                               
        
	protected void processWildcardExpression(Property property,
			WildcardOperator oper, Literal literal) {
		String content = literal.getValue().trim();
		content = content.replace(WILDCARD, "%");
		start(property);                                                                                        
		filter.append("'");                                                                  
		filter.append(content);                                                                  
		filter.append("'");                                                                  
	}

	/**
	 * Handles a property query node, traversing the property path appending 
	 * SQL 'AND' expressions based on key relationships until the
	 * property endpoint is reached. Superclass handlers deal with other query nodes such as
	 * operators and literals.  
	 */
	@Override
    public void start(Property property)
    {                         
        Path path = property.getPath();

        if (filter.length() > 0)
            filter.append(" ");

        PlasmaType targetType = (PlasmaType)contextType;
                
        String pathKey = "";
        if (path != null) {
        	this.contextPropertyIsPath = true;
            for (int i = 0 ; i < path.getPathNodes().size(); i++)
            {    
            	PlasmaType prevTargetType = targetType;
            	AbstractPathElement pathElem = path.getPathNodes().get(i).getPathElement();
                if (pathElem instanceof WildcardPathElement)
                    throw new DataAccessException("wildcard path elements applicable for 'Select' clause paths only, not 'Where' clause paths");
                String elem = ((PathElement)pathElem).getValue();
                PlasmaProperty prop = (PlasmaProperty)targetType.getProperty(elem);
                
                targetType = (PlasmaType)prop.getType(); // traverse
                
                pathKey += "/" + elem;

                if (!prop.isMany())
                {
                    PlasmaProperty priKeyProp = (PlasmaProperty)targetType.findProperty(KeyType.primary);
                }
                else
                {
                	PlasmaProperty opposite = (PlasmaProperty)prop.getOpposite();
                	if (opposite.isMany())
                        throw new DataAccessException("expected singular opposite for property, "
                        		+ prop.getContainingType().getURI() + "#"
                        		+ prop.getContainingType().getName() + "."
                        		+ prop.getName());
                    PlasmaProperty priKeyProp = (PlasmaProperty)prevTargetType.findProperty(KeyType.primary);
                }
            }    
        }
        else {
        	this.contextPropertyIsPath = false;
        }

        // process endpoint
        PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
        contextProperty = endpointProp;
        if (path != null) {
     	    log.warn("deferring to graph recognizer for path, " + pathKey + "/" + contextProperty.getName());
    	    return;
        }
        
        super.start(property);
    }
	
	@Override
	public void start(LogicalOperator operator) {
		this.contextLogicalOperator = operator; // process in literal
	}
	
	@Override
	public void start(Literal literal) {
		if (this.contextPropertyIsPath)
			return;
		
		if (filter.length() > 0)
			filter.append(" ");
		
		if (this.contextLogicalOperator != null) {
			switch (this.contextLogicalOperator.getValue()) {
			case AND:
				filter.append("AND");
				break;
			case OR:
				filter.append("OR");
				break;
			default:
				throw new DataAccessException("unknown operator '"
						+ this.contextLogicalOperator.getValue().toString() + "'");
			}
			filter.append(" ");
		}
		
		filter.append(((PlasmaProperty)this.contextProperty).getPhysicalName());
		filter.append(" ");
		
		String content = literal.getValue();
		if (this.contextWildcardOperator == null) {
			if (this.contextRelationalOperator == null)
				throw new IllegalStateException("expected context relational operator");
		    filter.append(toString(this.contextRelationalOperator));
		}
		else {
			content = content.replace(WILDCARD, this.wildcardChar);
		    filter.append(toString(contextWildcardOperator));
		}				
		Object sdoValue = DataConverter.INSTANCE.convert(
				this.contextProperty.getType(), 
				this.stringType,
				content);
		Object cqlValue = CQLDataConverter.INSTANCE.toCQLDataValue(
						(PlasmaProperty)this.contextProperty, sdoValue);
		params.add(cqlValue);
		filter.append(" ");
		filter.append(this.parameterChar);
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
        log.debug("where: " + xml);
    }

	@Override
	protected void assembleSubquery(Property property, SubqueryOperator oper,
			Query query) {
		throw new GraphServiceException("sub-queries not supported");		
	}
}