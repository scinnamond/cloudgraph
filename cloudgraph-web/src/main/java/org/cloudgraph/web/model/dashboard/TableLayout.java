package org.cloudgraph.web.model.dashboard;

import java.util.List;

public interface TableLayout {
	public List<Container> getColumnContainers();
	public int getColumnContainerCount();
	public Container getHeaderContainer();
	public Container getFooterContainer();
}
