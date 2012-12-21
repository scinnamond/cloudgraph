package org.cloudgraph.web.model.profile;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.AppMessageUtils;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.component.ChartType;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.PropertyName;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaCopyHelper;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import org.cloudgraph.web.sdo.personalization.Element;
import org.cloudgraph.web.sdo.personalization.ElementType;
import org.cloudgraph.web.sdo.personalization.Profile;
import org.cloudgraph.web.sdo.personalization.ProfileElementSetting;
import org.cloudgraph.web.sdo.personalization.Role;
import org.cloudgraph.web.sdo.personalization.RoleName;
import org.cloudgraph.web.sdo.personalization.Setting;
import org.cloudgraph.web.sdo.personalization.User;
import org.cloudgraph.web.sdo.personalization.UserRole;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class UserBean implements Serializable {
	 
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(UserBean.class);
	
	private String name = "sbear"; // fallback user for Tomcat or other non-auth testing
    private RoleName roleName = RoleName.SUPERUSER; // fallback role name in the event we cannot even lookup a Role from DB
    private Role role;
    private Profile profile;
    private User user;
    private Element applicationDefaultSettings;
    private Map<String, Element> elements;
    
    /**
     * Maps elements (component names) to a map of
     * name/value setting structures mapped by the 
     * setting name. Setting names are restricted on the
     * client side only using an enum. See (web dashboard 
     * PropertyName enum defined in ApplicationConfig.xsd)
     */
    private Map<String, Map<String, Setting>> settings;
	
	public UserBean() {
		
		try {
		    AppMessageUtils.setMessageBundle(this.getBundleName());		
	    } catch (Throwable t) {
	        log.error(t.getMessage(), t);
	        throw new RuntimeException("could not load bundle");
	    }       		    	    
	    
        try {
        	FacesContext context = FacesContext.getCurrentInstance();
        	Principal principal = context.getExternalContext().getUserPrincipal();
        	if (principal != null && 
        		principal.getName() != null && 
        		principal.getName().length() > 0)
        	    name = principal.getName();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
	        throw new RuntimeException("could not create principal");
        }       		    
	    
	    SDODataAccessClient service = new SDODataAccessClient();	
        try {
        	this.profile = findProfile(service);        	
        	this.user = this.getUser(service);
        	this.role = this.findRole(service);
        	if (this.role == null)
        		this.role = createDefaultRole(this.user, service);
        	
        	this.roleName = findRoleNameEnum(this.role.getName());
        	this.applicationDefaultSettings = this.getDefaultSettings(service);        	
        	SettingCollector collector = new SettingCollector(this.role.getName());
        	((PlasmaDataObject)this.applicationDefaultSettings).accept(collector);
        	((PlasmaDataObject)this.applicationDefaultSettings).accept(new GraphRemover());
        	
        	// merge in any profile specific settings overwriting
        	// defaults
        	if (this.profile != null)
        		((PlasmaDataObject)this.profile).accept(collector);
        	
           	this.settings = collector.getSettings();
           	this.elements = collector.getElements();
               	
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }       	
	}
	
	private RoleName findRoleNameEnum(String roleName)
	{
		for (RoleName name : RoleName.values())
			if (name.getInstanceName().equals(roleName))
				return name;
		return null;
	}
	
	public String getRoleName() {
		return this.roleName.name();
	}
	
	private Profile findProfile(SDODataAccessClient service) {
	    Profile result = null;
	    DataGraph[] results = service.find(ProfileQuery.createProfileQuery(this.getName()));           
	    if (results != null && results.length > 0) {
            if (results.length > 1)
        	    log.warn("found multiple profile trees for user, "
        			+ this.getName());
            result = (Profile)results[0].getRootObject();
            if (log.isDebugEnabled())            	
        	    log.debug("profile: " + ((PlasmaDataObject)result).dump());               		    	
	    }
		return result;
	}
	
	private User getUser(SDODataAccessClient service) {
	    DataGraph[] results = service.find(UserQuery.createQuery(this.getName())); 
	    if (results == null || results.length == 0)
	        throw new IllegalStateException("cannot find person information for username, "
			    + this.getName());
	    if (results.length > 1)
	        throw new IllegalStateException("multiple person information found for username, "
			    + this.getName());
	    User result = (User)results[0].getRootObject();
    	// disonnect it to use as a reference in another graph
        ((PlasmaDataGraph)result.getDataGraph()).removeRootObject();        
	    return result;
	}

	private Role findRole(SDODataAccessClient service) {
	    DataGraph[] results = service.find(RoleQuery.createQueryByUserName(this.getName())); 
	    if (results == null || results.length == 0)
	        return null;
	    if (results.length > 1)
	        log.warn("multiple role information found for user, "
			    + this.getName());
	    Role result = (Role)results[0].getRootObject();
    	// disonnect it to use as a reference in another graph
        ((PlasmaDataGraph)result.getDataGraph()).removeRootObject();        
	    return result;
	}

	private Role getRole(RoleName roleName, 
			SDODataAccessClient service) {
	    DataGraph[] results = service.find(RoleQuery.createQueryByRoleName(
	    		roleName)); 
	    if (results == null || results.length == 0)
	        throw new IllegalStateException("no role information found for role, "
				    + roleName.getInstanceName());
		if (results.length > 1)
	        log.warn("multiple role information found for role, "
			    + roleName.getInstanceName());
	    Role result = (Role)results[0].getRootObject();
    	// disonnect it to use as a reference in another graph
        ((PlasmaDataGraph)result.getDataGraph()).removeRootObject();        
	    return result;
	}
	
	private Role createDefaultRole(User user,
			SDODataAccessClient service) {
		
		DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
		dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(UserRole.class);
    	UserRole userRole = (UserRole)dataGraph.createRootObject(rootType);  	
		
		Role defaultRole = getRole(RoleName.USER, service);
		userRole.setRole(defaultRole);
		User userCopy = (User)PlasmaCopyHelper.INSTANCE.copyShallow(user);
		userRole.setUser(userCopy);
		service.commit(dataGraph, this.getName());
		
		((PlasmaDataObject)defaultRole).setDataGraph(null); // so we can use it in other graphs
	    return defaultRole;
	}
	
	private Element getDefaultSettings(SDODataAccessClient service) {
	    Element result = null;
	    DataGraph[] results = service.find(ProfileQuery.createDefaultSettingQuery(roleName)); 
		if (results == null || results.length == 0)
		    throw new IllegalStateException("cannot find default setting information for role, "
				+ this.roleName.toString());
		result = (Element)results[0].getRootObject();
		if (log.isDebugEnabled())            	
    	    log.debug(((PlasmaDataObject)result).dump());
		return result;
	}
		
    public String getBundleName() {
    	return WebConstants.BUNDLE_BASENAME;
    }

	public String getName() {
		return name;
	}

	public Profile getProfile() {
		return profile;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public Profile initializeProfile() {
		
		if (this.profile != null)
			throw new IllegalStateException("profile already exists");
		
		PlasmaDataGraph profileDataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
		profileDataGraph.getChangeSummary().beginLogging(); // log changes from this point
        Type rootType = PlasmaTypeHelper.INSTANCE.getType(Profile.class);
        this.profile = (Profile)profileDataGraph.createRootObject(rootType); 
        this.profile.setUser(this.user);
        
		return this.profile;
	}
	
	public String commitProfile() {
		if (this.profile == null)
			throw new IllegalStateException("no profile found");
        try {
		    SDODataAccessClient service = new SDODataAccessClient();
		    service.commit(profile.getDataGraph(), this.name);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return null;
	}	

	public Element getAppSettings() {
		return applicationDefaultSettings;
	}

	public Setting findComponentSetting(ComponentName componentName, 
			PropertyName propertyName) {
		Map<String, Setting> compSettings = this.settings.get(componentName.value());
		if (compSettings != null)
			return compSettings.get(propertyName.value());
		else
			return null;
	}

	/**
	 * Sets the profile setting for the given property, for the component to the
	 * name of another component. 
	 * @param componentName the component who's setting to update
	 * @param elementType the element type for the setting
	 * @param propertyName the property name for the setting
	 * @param component the target componet name value
	 */
	public void updateProfileSetting(ComponentName componentName,
			ElementType elementType,
			PropertyName propertyName, ComponentName component) {
		
		updateProfileSetting(componentName, elementType, propertyName,
				component.value()); 
		// note use the 'value()' from these JAXB unums as the 
		// Enum.valueOf(String) only works with these
	}
	
	/**
	 * Sets the profile setting for the given property, for the component to the
	 * given value. 
	 * @param componentName the component who's setting to update
	 * @param elementType the element type for the setting
	 * @param propertyName the property name for the setting
	 * @param value the value
	 */
	public void updateProfileSetting(ComponentName componentName,
			ElementType elementType,
			PropertyName propertyName, String value) {
		
		if (this.profile == null)
			initializeProfile();	
		
		Map<String, Setting> compSettings = this.settings.get(componentName.value());
		if (compSettings == null) {
			ProfileElementSetting profileSetting = this.profile.createProfileElementSetting();
			profileSetting.setRole(this.role);
			profileSetting.setProfile(this.profile);
			
			// Create a new element to "mirror" this component
			// on the back end. Make it a child of the app element. 
			// FIXME - what's its real parent
			Element elem = profileSetting.createElement();
			elem.setParent(applicationDefaultSettings);
			elem.setName(componentName.value());
			elem.setElementType(elementType.getInstanceName());
			
			Setting setting = profileSetting.createSetting();
			setting.setName(propertyName.value());
			setting.setValue(String.valueOf(value));
			
			// map the new setting
			compSettings = new HashMap<String, Setting>();
			this.settings.put(componentName.value(), compSettings);
			
			compSettings.put(propertyName.value(), setting);
		}
		else {
			Setting compSetting = compSettings.get(propertyName.value());
			if (compSetting != null) {
				if (compSetting.getProfileElementSettingCount() == 1) {
					// profile setting exists update it
					ProfileElementSetting profileSetting = compSetting.getProfileElementSetting(0);
					if (profileSetting.getProfile().getSeqId() != this.profile.getSeqId())
						throw new IllegalStateException("found profile setting(s) for profile seqId:"
								+ profileSetting.getProfile().getSeqId() + " within profile seqId:"
								+ this.profile.getSeqId());					
				    compSetting.setValue(String.valueOf(value));					
				}
				else if (compSetting.getProfileElementSettingCount() == 0) { 
					// there's a default role setting, add a profile specific one
					Element elem = this.elements.get(componentName.value());
					
					ProfileElementSetting profileSetting = this.profile.createProfileElementSetting();
					profileSetting.setRole(this.role);
					profileSetting.setProfile(this.profile);
					profileSetting.setElement(elem);
					Setting setting = profileSetting.createSetting();
					setting.setName(propertyName.value());
					setting.setValue(String.valueOf(value));
					
					compSettings.put(propertyName.value(), setting);				
				}
				else
					throw new IllegalStateException("found multiple profile element settings for property, "
							+ propertyName.value());
			}
			else {
				
				ProfileElementSetting profileSetting = this.profile.createProfileElementSetting();
				profileSetting.setRole(this.role);
				profileSetting.setProfile(this.profile);
				Setting setting = profileSetting.createSetting();
				setting.setName(propertyName.value());
				setting.setValue(String.valueOf(value));
				
				Element elem = this.elements.get(componentName.value());
				if (elem == null) {
					elem = profileSetting.createElement(); // auto links it
				    elem.setParent(applicationDefaultSettings);
				    elem.setName(componentName.value());
				    elem.setElementType(elementType.getInstanceName());
				}
				else
				    profileSetting.setElement(elem);
				
				compSettings.put(propertyName.value(), setting);				
			}
		}
	}
	
	public List<SelectItem> getChartTypeItems() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		result.add(new SelectItem(-1, "--none selected--"));
		for (ChartType type : ChartType.values()) {
			result.add(new SelectItem(type.ordinal(), type.name()));
		}
		return result;
	}
	
	public int getChartType() {
		
		Setting setting = findComponentSetting(ComponentName.PAGE___DASHBOARD,
				PropertyName.CHART___TYPE);
		if (setting != null) {
			return ChartType.valueOf(setting.getValue()).ordinal();
		}
		else
			return -1;
	}
	
	public void setChartType(int type) {
		
		if (type != -1) {
			
			ChartType selected = null;
			for (ChartType chartType : ChartType.values())
				if (chartType.ordinal() == type)
					selected = chartType;
		    updateProfileSetting(ComponentName.PAGE___DASHBOARD,
				ElementType.PAGE, 
				PropertyName.CHART___TYPE,
				selected.name());
		}
	}
	
	class GraphRemover implements PlasmaDataGraphVisitor {
		public void visit(DataObject target, DataObject source,
				String sourceKey, int level) {
			((PlasmaDataObject)target).setDataGraph(null);
			
		}
	}
	
}
