package org.cloudgraph.web.datasource;



import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.component.ChartResourceFinder;
import org.cloudgraph.web.config.ApplicationConfig;
import org.cloudgraph.web.config.web.ColorDef;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.StatusDef;
import org.cloudgraph.web.model.dashboard.AbstractComponent;
import org.cloudgraph.web.util.BeanFinder;
import org.jfree.data.general.Dataset;
import org.plasma.query.model.Query;

import commonj.sdo.DataObject;


public abstract class AbstractDataSource {

	private static Log log =LogFactory.getLog(
			AbstractDataSource.class);

	protected BeanFinder beanFinder = new BeanFinder();
	protected ChartResourceFinder resourceFinder;
    
    protected Dataset currentDataSet;
    protected AbstractComponent component;
    

    protected AbstractDataSource() {
    }
    
    protected AbstractDataSource(AbstractComponent component, ChartResourceFinder resourceFinder) {
    	this.component = component;
    	this.resourceFinder = resourceFinder;
    }
    
    public final ComponentName getComponentName() {
    	return this.component.getComponentName();
    }
    
    public abstract String getColorMap();
    public abstract Query getQuery();
    public abstract void loadTestDataSet();
    public abstract void loadDataSet(DataObject[] beans);
     
    protected String createColorMap(List<StatusDef> statusDefs)
    {
    	StringBuffer colors = new StringBuffer();
    	ColorDef colorDef = null;
    	String status = null;
    	int i = 0;
    	for (StatusDef statusDef : statusDefs)
    	{
    		if (resourceFinder != null)
    		    status = resourceFinder.getLabel(statusDef.getName());
    		else
    			status = statusDef.getName();
    		colorDef = ApplicationConfig.getInstance().getColorDef(statusDef.getName());
    		if (colorDef == null)
    			throw new RuntimeException("no color defined for status, " 
    					+ statusDef.getName().toString());
    		if (i > 0)
    			colors.append(",");
    		colors.append(status); 
    		colors.append(":");
    		colors.append(colorDef.getColor());
    		i++;
    	}
    	return colors.toString();   	
    }

	public Dataset getCurrentDataSet() {
		return currentDataSet;
	}

	public void purgeCurrentDataSet() {
		currentDataSet = null;
	}

	public ChartResourceFinder getResourceFinder() {
		return resourceFinder;
	}

	public void setResourceFinder(ChartResourceFinder resourceFinder) {
		this.resourceFinder = resourceFinder;
	}

	
}
