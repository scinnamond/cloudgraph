package org.cloudgraph.web.model.cache;

import org.cloudgraph.web.sdo.core.Organization;
import org.plasma.query.model.From;
import org.plasma.query.model.OrderBy;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.query.model.Where;


public class OrganizationQuery {
	
    public static Query createHierarchyQuery(String rootCode) {
    	
        Select select = new Select(new String[] { 
                "*",
                "child/*",
                "child/child/*",
                "child/child/child/*",
                "child/child/child/child/*",
                "child/child/child/child/child/*",
                "child/child/child/child/child/child/*",
            });    	

    	From from = new From(Organization.TYPE_NAME_ORGANIZATION,
    			Organization.NAMESPACE_URI);        
 		Where where = new Where();
 		where.addExpression(Property.forName(
 				Organization.PROPERTY.code.name()).eq(rootCode));
    	
    	OrderBy orderBy = new OrderBy();
		orderBy.addProperty(Property.forName(Organization.PROPERTY.name.name()));
        
		
        Query query = new Query(select, from, where, orderBy);
        return query;
    }	
    
}
