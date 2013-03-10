package org.cloudgraph.web.model.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.ComponentShape;
import org.cloudgraph.web.config.web.PropertyName;
import org.cloudgraph.web.model.profile.UserBean;
import org.cloudgraph.web.sdo.personalization.ElementType;
import org.cloudgraph.web.sdo.personalization.Setting;
import org.cloudgraph.web.util.BeanFinder;

public class Container extends AbstractComponent {

	private static Log log = LogFactory.getLog(Container.class);
	public static String COMPONENTS_DELIM = ",";
	
	private List<Component> components = new ArrayList<Component>();
	private boolean fetch = true;
	private int width;

	public Container(ComponentName name, ComponentShape shape, int width,
			Dashboard dashboard,
			Container homeContainer) {
		super(name, shape, ElementType.CONTAINER, dashboard, homeContainer);
		this.width = width;
	}
	
	public void removeAll() {
		components.clear();	
		this.fetch = true;
	}
	
	public void clear() {
		for (Component comp : components)
			comp.clear();
	}
	
	public String close() {
		return null;
	}
		
	public boolean getHasComponents() {
		return this.components.size() > 0;
	}
	
	public List<Component> getComponents() {
		if (this.fetch) {
			components.clear();
			fetch();
		}		
		return components;
	} 
	
	private void fetch() {
		try {
			UserBean user = (new BeanFinder()).findUserBean();
		    Setting componentsSetting = user.findComponentSetting(this.name,  
		        PropertyName.COMPONENTS);
		    if (componentsSetting != null) {
		    	String names = componentsSetting.getValue();
		    	if (names != null && names.length() > 0) {
			    	StringTokenizer st = new StringTokenizer(names, COMPONENTS_DELIM);
			    	while (st.hasMoreElements()) {
			    		String token = st.nextToken();
			    		ComponentName name = ComponentName.fromValue(token);
			    		Component comp = this.dashboard.getComponent(name);
			    		components.add(comp);
			    		if (comp.getContainer() == null)
			    		    comp.setContainer(this);
			    	}
		    	}
		    	else
		    		log.warn("found null or empty value for "
		    				+ PropertyName.COMPONENTS.name() 
		    				+ " property");
		    }
		    this.fetch = false;
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}		
	}

    public void addComponent(Component comp) {
    	if (add(comp)) {
    	    this.fetch = true;
    	    comp.setContainer(this);
    	    components.add(comp);
    	}
    }

    public void addComponent(Component comp, int position) {
    	if (add(comp)) {
    	    this.fetch = true;
    	    comp.setContainer(this);
    	    components.add(position, comp);
    	}
    }
    
	void addInitialComponent(Component comp) {
    	comp.setContainer(this);
    	components.add(comp);
	}

    private boolean add(Component comp) {
    	String names = "";
    	for (Component existing : components)
    	{
    		if (existing.getComponentName().ordinal() == comp.getComponentName().ordinal())
    			return false; // we have it in the profile
    		names += existing.getComponentName().value();
    		names += COMPONENTS_DELIM;
    	}
    	names += comp.getComponentName().value();
    	
		UserBean user = (new BeanFinder()).findUserBean();
        user.updateProfileSetting(this.name, this.type, 
        	PropertyName.COMPONENTS, 
        	names); 	
        // Note: do not commit the profile changes here, only commit 
        // from a JSF action method
        return true;
    }

    private void remove(Component comp) {
    	String names = "";
    	int i = 0;
    	for (Component existing : components)
    	{
    		if (existing.getComponentName().ordinal() == comp.getComponentName().ordinal())
    			continue; // exclude given comp
    		if (i > 0)
    		    names += COMPONENTS_DELIM;               		
    		names += existing.getComponentName().value();
    		i++;
    	}
    	
		UserBean user = (new BeanFinder()).findUserBean();
        user.updateProfileSetting(this.name, this.type, 
        	PropertyName.COMPONENTS, 
        	names); 
        // Note: do not commit the profile changes here, only commit 
        // from a JSF action method
    }
    
    public int getPosition(Component comp) {
    	return components.indexOf(comp);
    }
    
    public void removeComponent(Component comp) {
    	
    	remove(comp);
    	this.fetch = true;
    	
    	if (!components.remove(comp))
    		log.warn("could not remove, " + comp.getTypeName());
    }
    
    public int size() {
    	return this.getComponents().size();
    }

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
    
    
}





