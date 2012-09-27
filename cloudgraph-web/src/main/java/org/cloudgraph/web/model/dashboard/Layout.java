package org.cloudgraph.web.model.dashboard;

import java.util.List;


public interface Layout extends Component {
	public List<Container> getContainers();
	public int getContainerCount();
	public Container getTargetContainer(AbstractComponent comp);
	public Container getExpandedComponentsContainer();
	public String clearData();
}
