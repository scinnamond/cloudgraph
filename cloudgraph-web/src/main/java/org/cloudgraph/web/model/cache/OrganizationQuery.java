package org.cloudgraph.web.model.cache;

import org.plasma.query.model.From;
import org.plasma.query.model.OrderBy;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.query.model.Where;

import org.cloudgraph.web.sdo.core.Organization;


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

    	From from = new From(Organization.ETY_ORGANIZATION,
    			Organization.NAMESPACE_URI);        
 		Where where = new Where();
 		where.addExpression(Property.forName(
 				Organization.PTY_CODE).eq(rootCode));
    	
    	OrderBy orderBy = new OrderBy();
		orderBy.addProperty(Property.forName(Organization.PTY_NAME));
        
		
        Query query = new Query(select, from, where, orderBy);
        return query;
    }	
    
}
