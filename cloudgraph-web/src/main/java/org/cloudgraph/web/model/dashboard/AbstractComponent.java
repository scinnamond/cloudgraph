package org.cloudgraph.web.model.dashboard;

import java.io.IOException;
import java.util.Stack;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SerializableDataModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.ComponentShape;
import org.cloudgraph.web.model.profile.UserBean;
import org.cloudgraph.web.util.BeanFinder;

import org.cloudgraph.web.sdo.personalization.ElementType;

public abstract class AbstractComponent extends SerializableDataModel implements Component {
	
    private static final long serialVersionUID = 1L;
    
	private static Log log = LogFactory.getLog(AbstractComponent.class);
	protected ComponentName name;
	protected ComponentShape shape;
	protected Stack<String> categories = new Stack<String>();
	/** 
	 * The element type is an SDO datastore side enumeration because
	 * we store a lightweight generic element hierarchy of the presentation
	 * elements we want to personalize in the datastore, and minimally we want
	 * to store what type for each element. 
	 */
	protected ElementType type;
	private String caption;
	private String title;
	private String description;
	private boolean expanded;
	protected Container container;
	protected Container homeContainer;
	protected Container previousContainer;
	protected int previousContainerPosition;
    protected Dashboard dashboard;
    
    private int scrollerPage = 1;
	
	@SuppressWarnings("unused")
	private AbstractComponent() {}
	
	public AbstractComponent(ComponentName name, ComponentShape shape,
			ElementType type, Dashboard dashboard,
			Container homeContainer) {
		this.name = name;
		this.shape = shape;
		this.type = type;
		this.dashboard = dashboard;
		this.homeContainer = homeContainer;
	}
	
	public ComponentName getComponentName()
    {
    	return this.name;
    }

	public ComponentShape getShape() {
		return shape;
	}

	public ElementType getType() {
		return type;
	}
	
	public String getTypeName() {
		return type.name();
	}
	
	public void setType(ElementType type) {
		this.type = type;
	}
	
	public abstract void clear();

	// Note: JSF calls this 'Table' method on 'Chart' components even
	// though rendered is being property set. 
	// E.g (rendered="#{component.typeName == 'TABLE'}")
	public Object[] getData() {
		Object[] data = new Object[0];
		return data;
	}	
		
	public String close() {
	    this.setExpanded(false);
		this.removeContainer();
		this.homeContainer.addComponent(this);
		UserBean user = (new BeanFinder()).findUserBean();
        user.commitProfile();		
		return null;
	}
	
	public String refresh() {
		this.clear();
		return null;
	}
	
	public String select() {
		try {
			this.removeContainer();
			this.dashboard.getLayout().getTargetContainer(this).addComponent(this);
			UserBean user = (new BeanFinder()).findUserBean();
	        user.commitProfile();		
			return null;
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			return null;
		}
	}
	
	public String expand() {
		try {
			this.setExpanded(true);
			this.removeContainer();
			this.dashboard.getLayout().getExpandedComponentsContainer().addComponent(this);
			UserBean user = (new BeanFinder()).findUserBean();
	        user.commitProfile();		
			return null;
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			return null;
		}
	}
	
	public String collapse() {
		try {
			Container previousContainer = this.getPreviousContainer();
			int previousContainerPosition = this.getPreviousContainerPosition();		
			this.removeContainer();
			previousContainer.addComponent(this, previousContainerPosition);
			this.setExpanded(false);
			UserBean user = (new BeanFinder()).findUserBean();
	        user.commitProfile();		
			return null;
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			return null;
		}
	}
	
	public Container getContainer() {
		return container;
	}
	
	public int getPosition() {
		return container.getPosition(this);
	}

	public Container getPreviousContainer() {
		return previousContainer;
	}

	public int getPreviousContainerPosition() {
		return previousContainerPosition;
	}
	
	public Container getHomeContainer() {
		return this.homeContainer;
	}

	public void setContainer(Container container) {
		if (this.container != null)
			throw new IllegalStateException("Component is already linked to a container - please first remove the container");
		this.container = container;
	}

	public Container removeContainer() {
	    this.previousContainer = this.container;
	    this.previousContainerPosition = this.previousContainer.getPosition(this);
	    this.previousContainer.removeComponent(this);
		this.container = null;
		return this.previousContainer;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	
	public String getCategoryName() {
		if (categories.size() > 0)
		    return categories.peek();
		else
			return null;
	}

	public void setCategoryName(String categoryName) {
		categories.clear();
		categories.push(categoryName);
	}
	
	
    /**
     * This method never called from framework.
     * (non-Javadoc)
     * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
     */
    @Override
    public Object getRowKey() {
        throw new UnsupportedOperationException();
    }
    
    
    /**
     * This method normally called by Visitor before request Data Row.
     */
    @Override
    public void setRowKey(Object key) {
        throw new UnsupportedOperationException();
    }
	
	
    /**
     * This is main part of Visitor pattern. Method called by framework many times
     * during request processing. 
     */
    @Override
    public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument)
       throws IOException {
        throw new UnsupportedOperationException();
    }
	
	
    /**
     * This method must return actual data rows count from the Data Provider. It is used by
     * pagination control to determine total number of data items.
     */
    @Override
    public int getRowCount() {
        throw new UnsupportedOperationException();
    }
	
	
    /**
     * This is main way to obtain data row. It is intensively used by framework. 
     * We strongly recommend use of local cache in that method. 
     */
    @Override
    public Row getRowData() {
        throw new UnsupportedOperationException();
    }

	
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public int getRowIndex() {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public Object getWrappedData() {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Never called by framework.
     */
    @Override
    public boolean isRowAvailable() {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public void setRowIndex(int rowIndex) {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public void setWrappedData(Object data) {
        throw new UnsupportedOperationException();
    }

	
    /**
     * This method suppose to produce SerializableDataModel that will be serialized into View State
     * and used on a post-back. In current implementation we just mark current model as serialized.
     * In more complicated cases we may need to transform data to actually serialized form.
     */
    public  SerializableDataModel getSerializableModel(Range range) {
        throw new UnsupportedOperationException();
    }
	
	
    /**
     * This is helper method that is called by framework after model update. In must delegate actual
     * database update to Data Provider.
     */
    @Override
    public void update() {
        throw new UnsupportedOperationException();
    }
	
}
