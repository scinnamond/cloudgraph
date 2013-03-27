package org.cloudgraph.web.model.profile;

import org.cloudgraph.web.sdo.personalization.User;
import org.cloudgraph.web.sdo.personalization.query.QUser;
import org.plasma.query.model.From;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.query.model.Where;

public class UserQuery {

	
	public static Query createQuery(String username) {
        Select select = new Select(new String[] { 
                "*",
            });         
                
    	From from = new From(User.TYPE_NAME_USER, User.NAMESPACE_URI);        
        Where where = new Where();
        where.addExpression(Property.forName(User.PROPERTY.username.name()).eq(
        						username));
        
        Query query = new Query(select, from, where);
        return query;		
	}
	
	public static org.plasma.query.Query createProfileGraphQuery(String username) {
		QUser user = QUser.newQuery();
		
		user.select(user.wildcard())
		    .select(user.person().wildcard())
		    .select(user.profile().wildcard())
		    .select(user.profile().profileElementSetting().wildcard());
		
		user.where(user.username().eq(username));
		return user;
	}	
}
