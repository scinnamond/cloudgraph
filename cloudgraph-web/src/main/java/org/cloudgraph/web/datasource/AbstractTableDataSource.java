package org.cloudgraph.web.datasource;

import org.cloudgraph.web.component.ChartResourceFinder;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.model.dashboard.AbstractComponent;


public class AbstractTableDataSource {
    protected AbstractComponent component;
	protected ChartResourceFinder resourceFinder;
    
    protected AbstractTableDataSource(AbstractComponent component) {
    	this.component = component;
    }
    
    protected AbstractTableDataSource(AbstractComponent component, 
    		ChartResourceFinder resourceFinder) {
    	this.component = component;
    	this.resourceFinder = resourceFinder;
    }
    
    public final ComponentName getComponentName() {
    	return this.component.getComponentName();
    }

}
