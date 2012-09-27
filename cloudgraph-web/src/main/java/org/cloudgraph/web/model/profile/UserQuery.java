package org.cloudgraph.web.model.profile;

import org.plasma.query.model.From;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.query.model.Where;

import org.cloudgraph.web.sdo.personalization.User;

public class UserQuery {

	
	public static Query createQuery(String username) {
        Select select = new Select(new String[] { 
                "*",
            });         
                
    	From from = new From(User.ETY_USER, User.NAMESPACE_URI);        
        Where where = new Where();
        where.addExpression(Property.forName(User.PTY_USERNAME).eq(
        						username));
        
        Query query = new Query(select, from, where);
        return query;		
	}		
}
