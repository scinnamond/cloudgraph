package org.cloudgraph.web.model.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.cloudgraph.web.config.web.ComponentName;

import org.cloudgraph.web.sdo.personalization.ElementType;

public abstract class AbstractTableLayout extends AbstractLayout 
    implements TableLayout {

	protected List<Container> columnContainers = new ArrayList<Container>();
	protected Container headerContainer;
	protected Container footerContainer;

	public AbstractTableLayout(ComponentName name,
			Dashboard dashboard, Container homeContainer) {
		super(name, ElementType.LAYOUT, dashboard, homeContainer);
	}

	public List<Container> getColumnContainers() {
		return columnContainers;
	}
	
	public int getColumnContainerCount() {
		return this.columnContainers.size();
	}
	
	public Container getHeaderContainer() {
		return this.headerContainer;
	}

	public Container getFooterContainer() {
		return this.footerContainer;
	}

}
