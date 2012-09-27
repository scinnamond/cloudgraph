package org.cloudgraph.web.model.dashboard;

import org.cloudgraph.web.config.web.ComponentName;

import org.cloudgraph.web.sdo.personalization.ElementType;


public interface Dashboard {
	public Layout getLayout();
	public void setLayout(Layout layout);
	public ComponentName getComponentName();
	public ElementType getType();
	public String getResourceContext();
	public Component getComponent(ComponentName name);
	
}
