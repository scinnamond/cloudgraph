package org.cloudgraph.web.model.profile;

import org.plasma.query.model.Expression;
import org.plasma.query.model.From;
import org.plasma.query.model.Path;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.query.model.Where;

import org.cloudgraph.web.sdo.personalization.DefaultElementSetting;
import org.cloudgraph.web.sdo.personalization.Element;
import org.cloudgraph.web.sdo.personalization.Profile;
import org.cloudgraph.web.sdo.personalization.RoleName;
import org.cloudgraph.web.sdo.personalization.User;

public class ProfileQuery {

	public static Query createDefaultSettingQuery(RoleName role) {
        
		// Note: do NOT traverse into profile-element-setting here
		// or we will get settings for (potentially) every user
		Select select = new Select(new String[] { 
                "*",
                "defaultElementSetting/*",
                "defaultElementSetting/role/*",
                "defaultElementSetting/setting/*",
                "child/*",
                "child/defaultElementSetting/*",
                "child/defaultElementSetting/role/*",
                "child/defaultElementSetting/setting/*",
                "child/child/*",
                "child/child/defaultElementSetting/*",
                "child/child/defaultElementSetting/role/*",
                "child/child/defaultElementSetting/setting/*",
                "child/child/child/*",
                "child/child/child/defaultElementSetting/*",
                "child/child/child/defaultElementSetting/role/*",
                "child/child/child/defaultElementSetting/setting/*",
            });         
        
    	From from = new From(Element.ETY_ELEMENT, Element.NAMESPACE_URI);        
        Where where = new Where();
        where.addExpression(Property.forName(Element.PTY_NAME,
        		new Path(Element.PTY_DEFAULT_ELEMENT_SETTING,
        				DefaultElementSetting.PTY_ROLE)).eq(
        				role.getInstanceName()));
        where.addExpression(Expression.and());
        where.addExpression(Property.forName(
        		Element.PTY_PARENT).isNull());
        
        
        Query query = new Query(select, from, where);
        return query;		
	}
	
	public static Query createProfileQuery(String username) {
        Select select = new Select(new String[] { 
                "*",
                "user/*",
                "user/person/*",
                "profileElementSetting/*",
                "profileElementSetting/profile/*",
                "profileElementSetting/setting/*",
                "profileElementSetting/element/*",
            });         
                
    	From from = new From(Profile.ETY_PROFILE, Profile.NAMESPACE_URI);        
        Where where = new Where();
        where.addExpression(Property.forName(User.PTY_USERNAME,
        		new Path(Profile.PTY_USER)).eq(
        						username));
        
        Query query = new Query(select, from, where);
        return query;		
	}		
}
