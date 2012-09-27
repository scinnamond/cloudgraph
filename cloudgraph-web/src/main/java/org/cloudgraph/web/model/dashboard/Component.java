package org.cloudgraph.web.model.dashboard;

import org.cloudgraph.web.config.web.ComponentName;

import org.cloudgraph.web.sdo.personalization.ElementType;

public interface Component {
	public ComponentName getComponentName();
	public ElementType getType();	
	public String getTypeName();
	public String getTitle();
	public String getCaption();
	public String getDescription();
	public String select();
	public void clear();
	public Container getContainer();
	public Container removeContainer();
	public Container getHomeContainer();
	public void setContainer(Container container);

}
