package org.cloudgraph.web.component;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

/**
 * Tag class for the component
 */

public class ChartManagerTag extends UIComponentTag {
	

	private String manager = null;

    
    
	public void release() {
		super.release();
		manager = null;
	}

	protected void setProperties(UIComponent component) {
		super.setProperties(component);


		if (manager != null) {
			if (isValueReference(manager)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(manager);
				component.setValueBinding("manager", vb);
			} else {
				component.getAttributes().put("manager", manager);
			}
		}

    }

	public String getComponentType() {
		return ChartManagerComponent.COMPONENT_TYPE;
	}

	public String getRendererType() {
		return null;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}
}