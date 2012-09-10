package org.cloudgraph.rdb.jdbc;

// java imports
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.query.QueryException;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.Expression;
import org.plasma.query.model.From;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.NullLiteral;
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
import org.plasma.sdo.access.model.EntityConstants;
import org.plasma.sdo.access.provider.common.SQLQueryFilterAssembler;
import org.plasma.sdo.access.provider.jdbc.AliasMap;
import org.plasma.sdo.helper.DataConverter;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.profile.KeyType;

import commonj.sdo.Type;

public class JDBCFilterAssembler extends SQLQueryFilterAssembler
    implements QueryConstants, EntityConstants
{
    private static Log log = LogFactory.getLog(JDBCFilterAssembler.class);

    private Map variableMap;
    private StringBuffer variableDecls;
    private String importDecls;
    private String parameterDecls;
    private int variableDeclCount = 0;
    private int subqueryCount = 0;
    
    private AliasMap aliasMap;

    public JDBCFilterAssembler(Class candidate, Where where,
        Type contextType, AliasMap aliasMap)
    {
        super(contextType);
        this.aliasMap = aliasMap;
        
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

	public void start(Expression expression)
    {
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
    }                               
    
    protected void assembleSubquery(Property property, SubqueryOperator oper, Query query)
    {
        From from = query.getFromClause();
        Type type = PlasmaTypeHelper.INSTANCE.getType(from.getEntity().getNamespaceURI(), 
        		from.getEntity().getName());
        String alias = ALIAS_PREFIX + String.valueOf(subqueryCount);
        JDBCSubqueryFilterAssembler assembler = new JDBCSubqueryFilterAssembler(alias, 
            query, params, type);
                
        if (property.getPath() != null)
            throw new QueryException("properties with paths (" 
                + property.getName()
                + ") not allowed as subquery target");  
              
        commonj.sdo.Property endpointProperty = contextType.getProperty(property.getName());
        
        if (endpointProperty.isMany())
            throw new QueryException("multi-valued properties (" 
                + contextType.getName() + "." + endpointProperty.getName()
                + ") not allowed as subquery target");
        contextProperty = endpointProperty;
        
        switch (oper.getValue())
        {
            case IN:
                filter.append("("); 
                filter.append(assembler.getFilter());       
                filter.append(").contains(");
                filter.append(DATA_ACCESS_CLASS_MEMBER_PREFIX + endpointProperty.getName());
                filter.append(")");
                break;
            case NOT_IN:
                filter.append("!("); 
                filter.append(assembler.getFilter());       
                filter.append(").contains(");
                filter.append(DATA_ACCESS_CLASS_MEMBER_PREFIX + endpointProperty.getName());
                filter.append(")");
                break;
            case EXISTS:
                filter.append("!("); // negate it
                filter.append(assembler.getFilter());       
                filter.append(").isEmpty()");
                break;
            case NOT_EXISTS:
                filter.append("("); 
                filter.append(assembler.getFilter());       
                filter.append(").isEmpty()");
                break;
        }
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

    public void start(Property property)
    {                
        org.plasma.query.model.FunctionValues function = property.getFunction();
        if (function != null)
            throw new DataAccessException("aggregate functions only supported in subqueries not primary queries");
          
        Path path = property.getPath();

        if (filter.length() > 0)
            filter.append(" ");

        PlasmaType targetType = (PlasmaType)contextType;
        String targetAlias = this.aliasMap.getAlias(targetType);
        if (targetAlias == null)
        	targetAlias = this.aliasMap.addAlias(targetType);
                
        if (path != null)
        {

            String pathKey = "";
            for (int i = 0 ; i < path.getPathNodes().size(); i++)
            {    
            	PlasmaType prevTargetType = targetType;
            	String prevTargetAlias = targetAlias;
            	

            	AbstractPathElement pathElem = path.getPathNodes().get(i).getPathElement();
                if (pathElem instanceof WildcardPathElement)
                    throw new DataAccessException("wildcard path elements applicable for 'Select' clause paths only, not 'Where' clause paths");
                String elem = ((PathElement)pathElem).getValue();
                PlasmaProperty prop = (PlasmaProperty)targetType.getProperty(elem);
                
                targetType = (PlasmaType)prop.getType(); // traverse
                targetAlias = this.aliasMap.getAlias(targetType);
                if (targetAlias == null)
                	targetAlias = this.aliasMap.addAlias(targetType);
                
                pathKey += "/" + elem;

                if (!prop.isMany())
                {
                    filter.append(prevTargetAlias + "." + prop.getPhysicalName());
                    filter.append(" = ");                                
                    PlasmaProperty priKeyProp = (PlasmaProperty)targetType.findProperty(KeyType.primary);
                    filter.append(targetAlias + "." + priKeyProp.getPhysicalName());
                }
                else
                {
                	PlasmaProperty opposite = (PlasmaProperty)prop.getOpposite();
                	if (opposite.isMany())
                        throw new DataAccessException("expected singular opposite for property, "
                        		+ prop.getContainingType().getURI() + "#"
                        		+ prop.getContainingType().getName() + "."
                        		+ prop.getName());
                    filter.append(targetAlias + "." + opposite.getPhysicalName());
                    filter.append(" = ");                                
                    PlasmaProperty priKeyProp = (PlasmaProperty)prevTargetType.findProperty(KeyType.primary);
                    filter.append(prevTargetAlias + "." + priKeyProp.getPhysicalName());
               }
                
               filter.append(" AND ");
            }
        }
        PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
        contextProperty = endpointProp;
        filter.append(targetAlias + "." + endpointProp.getPhysicalName());
        super.start(property);
    }
    
	public void start(WildcardOperator operator) {
		if (filter.length() > 0)
			filter.append(" ");

		switch (operator.getValue()) {
		case LIKE:
			filter.append("LIKE");
			break;
		default:
			throw new DataAccessException("unknown operator '"
					+ operator.getValue().toString() + "'");
		}
	}

	public void start(LogicalOperator operator) {
		if (filter.length() > 0)
			filter.append(" ");

		switch (operator.getValue()) {
		case AND:
			filter.append("AND");
			break;
		case OR:
			filter.append("OR");
			break;
		default:
			throw new DataAccessException("unknown operator '"
					+ operator.getValue().toString() + "'");
		}
		super.start(operator);
	}

  /*
    * Resets variable hash map on every term grouping so as
    * not to preserve variables across groups. 
    * @param oper the group operator
    */
    public void start(GroupOperator oper)
    {
		switch (oper.getValue()) {
		case LP_1: filter.append(")"); break;			
		case LP_2: filter.append("))"); break;			
		case LP_3: filter.append(")))"); break;			
		case RP_1: filter.append("("); 			
            if (variableMap != null)
                variableMap.clear();
            break;
		case RP_2: filter.append("(("); break;			
		case RP_3: filter.append("((("); break;			
		default:
			throw new QueryException("unknown group operator, "
						+ oper.getValue().name());
		}

        super.start(oper);
    }

	public void start(Literal literal) {
		if (filter.length() > 0)
			filter.append(" ");
		String content = literal.getValue().trim();
		content = content.replace(WILDCARD, "%");
		params.add(
			DataConverter.INSTANCE.convert(
				this.contextProperty.getType(), 
				this.stringType,
				content));
		filter.append("?");
	}

	public void start(NullLiteral nullLiteral) {
		if (filter.length() > 0)
			filter.append(" ");
		params.add(nullLiteral);
		filter.append("?");
	}
}