package org.cloudgraph.web.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
  */
public class ChartManagerComponent extends UIComponentBase {

    private static Log log =LogFactory.getLog(ChartManagerComponent.class);

    public static final String COMPONENT_TYPE = "org.cloudgraph.web.component.ChartManagerComponent";
    public static final String COMPONENT_FAMILY = "org.cloudgraph.web.component.ChartManagerComponent";

    private Object datasourceManager;
    
    public ChartManagerComponent() {
        super();
    }

    public void encodeChildren(FacesContext context) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("encodeChildren(): " + this);
		}    	
		super.encodeChildren(context);
    }   
    
    public void encodeBegin(FacesContext context) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("encodeBegin(): " + this);
		}    	
		super.encodeBegin(context);
    	
        ChartDataSourceManager dataSourceManager = (ChartDataSourceManager)this.getDatasourceManager();
    	List list = new ArrayList();
    	ChartComponent[] charts = findChartComponents();
        for (int i = 0; i < charts.length; i++)
        {
        	ChartDataSource ds = (ChartDataSource)charts[i].getDatasource();
        	if (ds instanceof PagedDataSource)
        	{
        		PagedDataSource pds = (PagedDataSource)ds;
        		pds.setRows(charts[i].getRows());
        	}
    		list.add(ds);
        }
        ChartDataSource[] dataSources = new ChartDataSource[list.size()];
        list.toArray(dataSources);
        dataSourceManager.refresh(dataSources);        
    }

    private ChartComponent[] findChartComponents()
    {
    	List list = new ArrayList();
    	findComponents(this, ChartComponent.class, list);
    	ChartComponent[] results = new ChartComponent[list.size()];
    	list.toArray(results);
    	return results;
    }

    private void findComponents(UIComponent parent, Class toFind, List results)
    {
    	Iterator children = parent.getChildren().iterator();
    	while (children.hasNext())
    	{
    		UIComponent child = (UIComponent) children.next();
    		//log.debug("child: " + child.getClass().getName());
    		if (child.getClass().isAssignableFrom(toFind))
    			results.add(child); 
    		findComponents(child, toFind, results);
    	}
    }
    
    public void encodeEnd(FacesContext context) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("encodeEnd(): " + this);
		}    	
		super.encodeEnd(context);
        // esponseWriter writer = context.getResponseWriter();
    }

    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public Object getDatasourceManager() {
        if (datasourceManager != null)
            return datasourceManager;

        ValueBinding vb = getValueBinding("manager");
        Object v = vb != null ? vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setDatasourceManager(Object manager) {
        this.datasourceManager = manager;
    }
    
    public Object saveState(FacesContext context) {
        Object values[] = new Object[2];
        values[0] = super.saveState(context);
        //values[1] = datasourceManager;
        return values;
    }

    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        //this.datasourceManager = values[1];
    }
}
