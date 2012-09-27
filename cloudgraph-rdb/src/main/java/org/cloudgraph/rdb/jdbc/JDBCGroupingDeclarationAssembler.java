package org.cloudgraph.rdb.jdbc;

// java imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.query.model.GroupBy;
import org.plasma.query.model.Path;
import org.plasma.query.model.Property;
import org.plasma.query.model.QueryConstants;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.query.visitor.Traversal;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.model.EntityConstants;
import org.plasma.sdo.access.provider.jdbc.AliasMap;

public class JDBCGroupingDeclarationAssembler extends DefaultQueryVisitor
    implements QueryConstants, EntityConstants
{
    private static Log log = LogFactory.getLog(JDBCGroupingDeclarationAssembler.class);

    private PlasmaType contextType;
    private commonj.sdo.Property contextProp;
    private StringBuilder groupingDeclaration = new StringBuilder();
    private AliasMap aliasMap;

    @SuppressWarnings("unused")
	private JDBCGroupingDeclarationAssembler() {}

    public JDBCGroupingDeclarationAssembler(GroupBy groupby,
    		PlasmaType contextType, AliasMap aliasMap)
    {
        this.contextType = contextType;
        this.aliasMap = aliasMap;
        
        if (groupby.getTextContent() == null)
        	groupby.accept(this); // traverse
        else
            groupingDeclaration.append(groupby.getTextContent().getValue());            
    }

    public String getGroupingDeclaration() { return groupingDeclaration.toString(); }

    public void start(Property property)                  
    {                
        if (groupingDeclaration.length() == 0)
        	groupingDeclaration.append("GROUP BY ");
        	
    	if (groupingDeclaration.length() > "GROUP BY ".length())
            groupingDeclaration.append(", ");
    	PlasmaType targetType = contextType;
        if (property.getPath() != null)
        {
            Path path = property.getPath();
            for (int i = 0 ; i < path.getPathNodes().size(); i++)
            {
                PlasmaProperty prop = (PlasmaProperty)targetType.getProperty(
                	path.getPathNodes().get(i).getPathElement().getValue());
                targetType = (PlasmaType)prop.getType();
            }
        }
        PlasmaProperty endpoint = (PlasmaProperty)targetType.getProperty(property.getName());        
        contextProp = endpoint;
        
        String alias = this.aliasMap.getAlias(targetType);
        if (alias == null)
        	alias = this.aliasMap.addAlias(targetType);
        groupingDeclaration.append(alias);
        groupingDeclaration.append(".");
        groupingDeclaration.append(endpoint.getPhysicalName());
        this.getContext().setTraversal(Traversal.ABORT);
    } 
}