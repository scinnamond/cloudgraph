package org.cloudgraph.web.model.profile;

import java.io.Serializable;
import java.security.AccessController;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.AppMessageUtils;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.component.ChartType;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.PropertyName;
import org.cloudgraph.web.jaas.LoginCallbackHandler;
import org.cloudgraph.web.jaas.RolePrincipal;
import org.cloudgraph.web.jaas.UserPrincipal;
import org.cloudgraph.web.sdo.personalization.Element;
import org.cloudgraph.web.sdo.personalization.ElementType;
import org.cloudgraph.web.sdo.personalization.Person;
import org.cloudgraph.web.sdo.personalization.Profile;
import org.cloudgraph.web.sdo.personalization.ProfileElementSetting;
import org.cloudgraph.web.sdo.personalization.Role;
import org.cloudgraph.web.sdo.personalization.RoleName;
import org.cloudgraph.web.sdo.personalization.Setting;
import org.cloudgraph.web.sdo.personalization.User;
import org.cloudgraph.web.sdo.personalization.UserRole;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaCopyHelper;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;

@ManagedBean(name="UserBean")
@SessionScoped
public class UserBean implements Serializable {
	 
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(UserBean.class);
	
	private String defaultUserName = "anonymous"; // fallback user for Tomcat or other non-auth testing	
	private String name = defaultUserName;
    private RoleName roleName = RoleName.ANONYMOUS; // fallback role name in the event we cannot even lookup a Role from DB
    private Role role;
    private Profile profile;
    private User user;
    private Element applicationDefaultSettings;
    private Map<String, Element> elements;
    private String stagingUsername;
    private String stagingPassword;
	boolean authenticated = false;
    
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
	        //throw new RuntimeException("could not load bundle");
	    }  
		
	    SDODataAccessClient service = new SDODataAccessClient();	
	    
		// if we have an authenticated principal, override default name
	    FacesContext context = FacesContext.getCurrentInstance();
    	Principal principal = context.getExternalContext().getUserPrincipal();
    	if (principal != null && 
    		principal.getName() != null && 
    		principal.getName().length() > 0) {
    	    name = principal.getName();
    	    this.authenticated = true;
    	}
    	
    	if (this.authenticated) {
    		loadAuthenticatedUser();
        }
    	else {    		
    		DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
    		dataGraph.getChangeSummary().beginLogging(); // log changes from this point
        	Type rootType = PlasmaTypeHelper.INSTANCE.getType(User.class);
        	this.user = (User)dataGraph.createRootObject(rootType);  	
    		String ip = getClientIpAddr();
    		this.user.setIpAddress(ip);  
    		this.user.setUsername(this.defaultUserName);
    		this.user.setPassword(this.defaultUserName);
    		this.user.setExternalId(UUID.randomUUID().toString());
        	UserRole userRole = this.user.createUserRole();  
        	
        	Role role = getRole(RoleName.ANONYMOUS, service);
        	Role roleCopy = (Role)PlasmaCopyHelper.INSTANCE.copyShallow(role);
        	
        	this.role = roleCopy;
    		userRole.setRole(this.role);
    		this.profile = user.createProfile();
    		 
    		
    		try {
    		    service.commit(dataGraph, ip);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }       	
    	}    	
	}
	
	public boolean getIsAuthenticated() {
		return authenticated;
	}

	public String getPrincipalName()
	{
	    FacesContext context = FacesContext.getCurrentInstance();
    	Principal principal = context.getExternalContext().getUserPrincipal();
    	if (principal != null && 
    		principal.getName() != null && 
    		principal.getName().length() > 0) {
    	    this.name = principal.getName();
    		loadAuthenticatedUser();
    	    return this.name;
    	}
    	return null;
	}
	
	private void loadAuthenticatedUser() {
	    SDODataAccessClient service = new SDODataAccessClient();	
        try {
        	this.user = this.getUser(service);
        	this.profile = this.user.getProfile(0);
        	this.role = this.findRole(service);
        	if (this.role == null)
        		this.role = createDefaultRole(this.user, service);
        	
        	this.roleName = findRoleNameEnum(this.role.getName());
        	
        	/*
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
           	*/
               	
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        } 		
	}
	
	private String getIpAddress() 
	{
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
		    ipAddress = request.getRemoteAddr();
		    return ipAddress;
		}
		return null;
	}
	
	private String getClientIpAddr() {  
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ip = request.getHeader("X-Forwarded-For");  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
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
	
	public String getStagingUsername() {
		return stagingUsername;
	}

	public void setStagingUsername(String stagingUsername) {
		this.stagingUsername = stagingUsername;
	}
	
    public void validateStagingUsername(FacesContext facesContext,
            UIComponent component, Object value) {
    	String name = null;
    	if (value == null || ((String)value).trim().length() == 0) {
    		return;
    	}
    	else
    		name = ((String)value).trim();
    	
    	this.stagingUsername = name;
    	// don't validate, just wait around for the password validator
    }
    
	public String getStagingPassword() {
		return stagingPassword;
	}

	public void setStagingPassword(String stagingPassword) {
		this.stagingPassword = stagingPassword;
	}
	
    public void validateStagingPassword(FacesContext facesContext,
            UIComponent component, Object value) {
    	String pwd = null;
    	if (value == null || ((String)value).trim().length() == 0) {
    		return;
    	}
    	else
    		pwd = ((String)value).trim();
    	
    	this.stagingPassword = pwd;
    	LoginContext lc = null;
		try {
		    lc = new LoginContext("Jaas", 
                new LoginCallbackHandler(this.stagingUsername, 
                    this.stagingPassword));
		} catch (LoginException le) {
			log.error(le.getMessage(), le);
		}
		
		try {
		    lc.login();
		} catch (LoginException le) {
            String msg = "Invalid username or password";
            throw new ValidatorException(
                		new FacesMessage(msg, msg));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
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
	    DataGraph[] results = service.find(UserQuery.createProfileGraphQuery(this.getName())); 
	    if (results == null || results.length == 0)
	        throw new IllegalStateException("cannot find person information for username, "
			    + this.getName());
	    if (results.length == 0)
	        throw new IllegalStateException("no person information found for username, "
			    + this.getName());
	    if (results.length > 1)
	        throw new IllegalStateException("multiple person information found for username, "
			    + this.getName());
	    User result = (User)results[0].getRootObject();
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

	public String getUsername() {
		return this.user.getUsername();
	}
	
	public void setUsername(String name) {
		if (name == null) {
		    if (this.user.isSetUsername())
		    	this.user.unsetUsername();
		}
		else
		    this.user.setUsername(name);
	}
	
	public String getPassword() {
		return this.user.getPassword();
	}
	
	public void setPassword(String passwd) {
		if (passwd == null) {
		    if (this.user.isSetPassword())
		    	this.user.unsetPassword();
		}
		else
		    this.user.setPassword(passwd);
	}	
	
	public Person getPerson() {
		return this.user.getPerson(0);
	}
	
	public String getFirstName() {
		if (this.user.getPersonCount() > 0)
		    return this.user.getPerson(0).getFirstName();
		else
			return null;
	}
	
	public void setFirstName(String firstName) {
		if (this.user.getPersonCount() == 0)
			this.user.createPerson();			
		if (firstName == null) {
		    if (this.user.getPerson(0).isSetFirstName())
		    	this.user.getPerson(0).unsetFirstName();
		}
		else
		    this.user.getPerson(0).setFirstName(firstName);
	}
	
	public String getLastName() {
		if (this.user.getPersonCount() > 0)
		    return this.user.getPerson(0).getLastName();
		else
			return null;
	}
	
	public void setLastName(String lastName) {
		if (this.user.getPersonCount() == 0)
			this.user.createPerson();			
		if (lastName == null) {
		    if (this.user.getPerson(0).isSetLastName())
		    	this.user.getPerson(0).unsetLastName();
		}
		else
		    this.user.getPerson(0).setLastName(lastName);
	}
	
	public String getEmailAddress() {
		if (this.user.getPersonCount() > 0)
		    return this.user.getPerson(0).getEmailAddress();
		else
			return null;
	}
	
	public void setEmailAddress(String addr) {
		if (this.user.getPersonCount() == 0)
			this.user.createPerson();			
		if (addr == null) {
		    if (this.user.getPerson(0).isSetEmailAddress())
		    	this.user.getPerson(0).unsetEmailAddress();
		}
		else
		    this.user.getPerson(0).setEmailAddress(addr);
	}
	
	public void initializeProfile(ActionEvent event) {
		initializeProfile();
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
	
	public void commitProfile(ActionEvent event) {
		commitProfile();
	}
	
	public String commitProfile() {
        try {
    		if (this.profile == null)
    			throw new IllegalStateException("no profile found");
        	
		    SDODataAccessClient service = new SDODataAccessClient();
    		this.role = getRole(RoleName.USER, service);
		    this.roleName = RoleName.USER;
    		this.name = this.user.getUsername();
    		Role roleCopy = (Role)PlasmaCopyHelper.INSTANCE.copyShallow(this.role);
    		this.user.getUserRole(0).setRole(roleCopy);
    		
		    service.commit(profile.getDataGraph(), this.name);		    
		    
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return null;
	}	
	
	public String cancelCommitProfile() {
        return null;
	}	
	
	public void login(ActionEvent event) {
		login();
	}

	public String login() {
		if (this.profile == null)
			throw new IllegalStateException("no profile found");

		// set new subject into session
		FacesContext context = FacesContext.getCurrentInstance();
	    HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();  
	    HttpSession httpSession = request.getSession(false);  
	    Subject subject = (Subject)httpSession.getAttribute("javax.security.auth.subject");
	    
	    Subject acSubject = Subject.getSubject(AccessController.getContext());
	    String remoteUser = request.getRemoteUser();
		
	    if (subject == null) {
	    	subject = new Subject();
	    	httpSession.setAttribute("javax.security.auth.subject", subject);
	    }
	    
		LoginContext lc = null;
		try {
		    lc = new LoginContext("Jaas", 
                new LoginCallbackHandler(this.stagingUsername, 
                    this.stagingPassword));
		} catch (LoginException le) {
			log.error(le.getMessage(), le);
		}
		
		try {
		    lc.login();
		} catch (LoginException le) {
			log.error(le.getMessage(), le);
            String msg = "Invalid username or password";
	        FacesContext.getCurrentInstance().addMessage(null, 
	        		new FacesMessage(msg));  
	        return null;
		} finally {
		    this.stagingUsername = null;
		    this.stagingPassword = null;   
		}

		try {
			subject = lc.getSubject();
		    
			// set new subject into session
		    httpSession.setAttribute("javax.security.auth.subject", subject);
			
			for (Principal principal : subject.getPrincipals()) {
				if (principal instanceof UserPrincipal) {
	        	    this.name = principal.getName();
	        		loadAuthenticatedUser();
	    		    SDODataAccessClient service = new SDODataAccessClient();
	    		    service.commit(profile.getDataGraph(), this.name);
	    		    this.authenticated = true;
				}
				else if (principal instanceof RolePrincipal) {
					// do something
				}
			}
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return null;
	}	
	
	public String cancelLogin() {
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
