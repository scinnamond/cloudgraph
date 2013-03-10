package org.cloudgraph.web.model.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.ComponentShape;
import org.cloudgraph.web.config.web.PropertyName;
import org.cloudgraph.web.model.profile.UserBean;
import org.cloudgraph.web.sdo.personalization.ElementType;
import org.cloudgraph.web.util.BeanFinder;

public abstract class AbstractLayout implements Layout {
	private static Log log = LogFactory.getLog(AbstractLayout.class);
	
	protected List<Container> containers = new ArrayList<Container>();
	protected Container expandedComponentsContainer;
	protected ComponentName name;
	protected Container homeContainer;
	protected Container container;
	protected ElementType type;
	private String caption;
	private String title;
	private String description;
    protected Dashboard dashboard;

	@SuppressWarnings("unused")
	private AbstractLayout() {}
	
	public AbstractLayout(ComponentName name,
			ElementType type, Dashboard dashboard,
			Container homeContainer) {
		this.expandedComponentsContainer = new Container(ComponentName.CONTAINER___EXPANDED, 
				ComponentShape.SQUARE, 700, dashboard, null);
		this.name = name;
		this.type = type;
		this.dashboard = dashboard;
		this.homeContainer = homeContainer;
	}
	
	public List<Container> getContainers() {
		return containers;
	}
	
	public int getContainerCount() {
		return this.containers.size();
	}
		
	public Container getExpandedComponentsContainer() {
		return this.expandedComponentsContainer;
	}

	public Container getTargetContainer(AbstractComponent comp) {
		Container target = null; 
		switch (comp.getShape()) {
		case TALL:
		case SQUARE:
			target = getLeastContainer(ComponentShape.TALL);
			break;
		case WIDE:	
			target = getLeastContainer(ComponentShape.WIDE);
			break;
		}
		return target;
	}
	
	private Container getLeastContainer(ComponentShape shape) {
	    Container least = null;
	    for (Container c : this.containers) {
            if (c.getShape().ordinal() != shape.ordinal())
            	continue;
		    if (least == null || c.size() < least.size()) {
			    least = c;
	        }
	    }
		return least;
    }
	
	public String select() {
		try {
						
			Layout oldLayout = this.dashboard.getLayout();
			// clean out all containers from old layout
			List<Component> selectedComponents = new ArrayList<Component>();
			Container[] containers = new Container[oldLayout.getContainers().size()];
			oldLayout.getContainers().toArray(containers);
			for (Container container : containers) {
				Component[] components = new Component[container.getComponents().size()];
				container.getComponents().toArray(components);
				for (Component comp : components)
				{
					selectedComponents.add(comp);
					comp.removeContainer();
				}
			}	
            // re-add "cleaned" components into this layout
			for (Component comp : selectedComponents) {
			    Container target = this.getTargetContainer((AbstractComponent)comp);
			    target.addComponent(comp);
			}
			
			
			//oldLayout.getHomeContainer().addComponent(oldLayout);
			this.dashboard.setLayout(this);
			
			UserBean user = (new BeanFinder()).findUserBean();
	        user.updateProfileSetting(this.dashboard.getComponentName(), 
	        		this.dashboard.getType(), 
	            	PropertyName.LAYOUT___NAME, 
	            	this.name); 	
						
			
	        user.commitProfile();		
			
			return null;
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			return null;
		}
	}	
	
	public String clearData() {
		for (Container cnr : this.containers) {
			for (Component comp : cnr.getComponents()) {
				comp.clear();
			}
		}
		for (Component comp : expandedComponentsContainer.getComponents()) {
			comp.clear();
		}		
		
		return null;
	}

	public void clear() {
		clearData();
	}
	
	public Container removeContainer() {
		this.container = null;
		return this.container;
	}
	
	public Container getHomeContainer() {
		return this.homeContainer;
	}
	
	public void setContainer(Container container) {
		this.container = container;
	}
	
	public ComponentName getComponentName() {
		return name;
	}

	public void setName(ComponentName name) {
		this.name = name;
	}

	public ElementType getType() {
		return type;
	}

	public void setType(ElementType type) {
		this.type = type;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Container getContainer() {
		return container;
	}

	public String getTypeName() {
		return type.name();
	}    
}
