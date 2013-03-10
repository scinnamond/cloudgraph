package org.cloudgraph.web.model.profile;

import org.cloudgraph.web.sdo.personalization.Role;
import org.cloudgraph.web.sdo.personalization.RoleName;
import org.cloudgraph.web.sdo.personalization.User;
import org.cloudgraph.web.sdo.personalization.UserRole;
import org.plasma.query.model.From;
import org.plasma.query.model.Path;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.query.model.Where;


public class RoleQuery {
	
	public static Query createQueryByRoleName(RoleName roleName) {
        Select select = new Select(new String[] { 
                "*",
            });         
                
    	From from = new From(Role.TYPE_NAME_ROLE, Role.NAMESPACE_URI);        
        Where where = new Where();
        where.addExpression(Property.forName(Role.PROPERTY.name.name()).eq(
        		roleName.getInstanceName()));
        
        Query query = new Query(select, from, where);
        return query;		
	}
	
	public static Query createQueryByUserName(String username) {
        Select select = new Select(new String[] { 
                "*",
            });         
                
    	From from = new From(Role.TYPE_NAME_ROLE, Role.NAMESPACE_URI);        
        Where where = new Where();
        where.addExpression(Property.forName(User.PROPERTY.username.name(),
        		new Path(Role.PROPERTY.userRole.name(),
        				UserRole.PROPERTY.user.name())).eq(
        						username));
        
        Query query = new Query(select, from, where);
        return query;		
	}	
}
