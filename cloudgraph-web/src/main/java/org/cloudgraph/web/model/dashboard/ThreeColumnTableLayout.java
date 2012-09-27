package org.cloudgraph.web.model.dashboard;

import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.ComponentShape;

public class ThreeColumnTableLayout extends AbstractTableLayout {

		
	public ThreeColumnTableLayout(ComponentName name, 
			Dashboard dashboard, Container homeContainer) {
		super(name, dashboard, homeContainer);
		
		Container leftContainer = new Container(ComponentName.CONTAINER___LEFT, 
				ComponentShape.TALL,
				300, dashboard, null);
		Container middleContainer = new Container(ComponentName.CONTAINER___MIDDLE, 
				ComponentShape.TALL,
				300, dashboard, null);
		Container rightContainer = new Container(ComponentName.CONTAINER___RIGHT, 
				ComponentShape.TALL,
				300, dashboard, null);
		
		this.columnContainers.add(leftContainer);
		this.columnContainers.add(middleContainer);
		this.columnContainers.add(rightContainer);
		this.containers.add(leftContainer);
		this.containers.add(middleContainer);
		this.containers.add(rightContainer);
		
		this.headerContainer = new Container(ComponentName.CONTAINER___HEADER, 
				ComponentShape.WIDE,
				910, dashboard, null);
		this.footerContainer = new Container(ComponentName.CONTAINER___FOOTER, 
				ComponentShape.WIDE,
				910, dashboard, null);		
		this.containers.add(headerContainer);
		this.containers.add(footerContainer);
	}



}
