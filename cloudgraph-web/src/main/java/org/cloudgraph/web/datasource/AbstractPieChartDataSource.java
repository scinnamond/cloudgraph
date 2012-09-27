package org.cloudgraph.web.datasource;

import java.util.Random;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.component.ChartDataSource;
import org.cloudgraph.web.component.ChartResourceFinder;
import org.cloudgraph.web.config.ApplicationConfig;
import org.cloudgraph.web.config.web.PieChartDef;
import org.cloudgraph.web.config.web.StatusDef;
import org.cloudgraph.web.model.dashboard.AbstractComponent;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.plasma.query.model.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;


import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;



public abstract class AbstractPieChartDataSource extends AbstractDataSource 
    implements WebConstants, ChartDataSource {

	private static Log log =LogFactory.getLog(
			PaginatedCategoryChartDataSource.class);

	protected AbstractPieChartDataSource() {
    }
	
    protected AbstractPieChartDataSource(AbstractComponent component, ChartResourceFinder resourceFinder) {
        super(component, resourceFinder);
    }
    
    protected abstract Query createQuery();

    public Query getQuery()
    {
    	Query query = createQuery();
    	return query;
    }

    public void loadTestDataSet()
    {
        this.currentDataSet = this.getTestDataSet();	
    }
    
    public int getRowCount() {
        int result = 1;
        return result;
    }
    
    public Dataset getDataSet() {
    	if (currentDataSet != null)
    		return currentDataSet;
    	
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
            return this.getPieDataSet();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();
            try {
				if (log.isWarnEnabled()) {
					log.warn("redirecting to: " + URL_ERROR_FORWARD);
				}                
				response.sendRedirect(URL_ERROR_FORWARD);
            } catch (java.io.IOException e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    protected Dataset getPieDataSet()
    {
    	SDODataAccessClient serviceProxy = new SDODataAccessClient();
        Query query = createQuery();
        DataGraph[] results = serviceProxy.find(query);
        DataObject[] roots = new DataObject[results.length];
        for (int i = 0; i < results.length; i++)
        	roots[i] = results[i].getRootObject(); // assumes flat results set
        loadDataSet(roots);
        return this.currentDataSet;
    }

    public void loadDataSet(DataObject[] results)
    {
        DefaultPieDataset dataset = new DefaultPieDataset();                                    
        PieChartDef chartDef = ApplicationConfig.getInstance().getPieChartDef(this.getComponentName());
        if (results != null && results.length > 0)
        	if (!results[0].getType().getName().startsWith(chartDef.getTypeName()))
        		throw new IllegalArgumentException("query results entity '" 
        				+ results[0].getType().getName() + "' does not 'resemble' defined chart entity '"
        				+ chartDef.getTypeName() + "'");
    	
        for (int i = 0; i < results.length; i++) {
        	
    		// Assume a generic approach where data is organized "by rows" 
    		// and all keys, values and resources are pulled using the same respective 
    		// SDO properties defined at the chart level. 
    		if (chartDef.getStatusValuePropertyName() != null) {
        		Object statusValue = results[i].get(chartDef.getStatusValuePropertyName());
        		if (statusValue == null)
        			statusValue = new Integer(0);
        		
        		if (!(statusValue instanceof Number))
        		    throw new RuntimeException("status property '"
        		    		+ chartDef.getTypeName() + "." + chartDef.getStatusValuePropertyName() + "' for chart definition '"
        		    		+ chartDef.getName() + "' is not a datatype which can be cast to a java.lang.Number "
        		    		+ "as required for inclusion in a category chart dataset");
        		Number statusNumber = (Number)statusValue;
        		if (statusNumber.intValue() == 0 && chartDef.isIgnoreZeroValues())
        			continue;
        		
        		if (chartDef.getStatusKeyPropertyName() == null)
        			throw new RuntimeException("Since a status value property name "
        				+ "is specified in the chart definition, a status key property name is " 
        				+ "also expected. Otherwise no status key can be gotten from the results data.");
        		
        		String statusKey = results[i].getString(chartDef.getStatusKeyPropertyName());
        		if (statusKey != null) {       		
	        		dataset.setValue(statusKey, statusNumber);
        		}
        		else
        		    throw new RuntimeException("Could not find status key using SDO property '"
        		    		+ chartDef.getTypeName() + "." + chartDef.getStatusKeyPropertyName() 
        		    		+ "' for chart definition '" + chartDef.getName() + "'.");
    		}
    		else
    		{	
	        	for (StatusDef statusDef : chartDef.getStatusDef())
	        	{
	        		Object statusValue = results[i].get(statusDef.getValuePropertyName());
	        		if (statusValue == null)
	        			statusValue = new Integer(0);
	        		
	        		if (!(statusValue instanceof Number))
	        		    throw new RuntimeException("status property '"
	        		    		+ chartDef.getTypeName() + "." + statusDef.getValuePropertyName() + "' for chart definition '"
	        		    		+ chartDef.getName() + "' is not a datatype which can be cast to a java.lang.Number "
	        		    		+ "as required for inclusion in a category chart dataset");
	        		Number statusNumberValue = (Number)statusValue;
	        		if (statusNumberValue.intValue() == 0 && chartDef.isIgnoreZeroValues())
	        			continue;
	        		String statusKey = null;
	        		if (resourceFinder != null)
	        			statusKey = resourceFinder.getLabel(statusDef.getName());
	        		else
	        			statusKey = statusDef.getName();
	        		dataset.setValue(statusKey, statusNumberValue);
	        	}
    		}
        } 
        this.currentDataSet = dataset;    	
    }
    
    protected Dataset getTestDataSet()
    {
        DefaultPieDataset dataset = new DefaultPieDataset();                                    
        PieChartDef chartDef = ApplicationConfig.getInstance().getPieChartDef(this.getComponentName());
    	
        Random rand = new Random();
        for (int i = 0; i < rand.nextInt(10000); i++) {
        	for (StatusDef statusDef : chartDef.getStatusDef())
        	{
        		//if (statusDef.getAccess().getType() == TrackingAccess.ADMIN_TYPE
        		//		&& !this.user.getIsAdmin())
        		//	continue;
        		Integer value = new Integer(rand.nextInt(5000));
        		if (value.intValue() == 0 && chartDef.isIgnoreZeroValues())
        			continue;
        		String statusKey = null;
        		if (resourceFinder != null)
        			statusKey = resourceFinder.getLabel(statusDef.getName());
        		else
        			statusKey = statusDef.getName();
        		dataset.setValue(statusKey, value);
        	}
        } 
        return dataset;
    }
    
    public String getColorMap()    
    {
    	PieChartDef chartDef = ApplicationConfig.getInstance().getPieChartDef(this.getComponentName());
    	return this.createColorMap(chartDef.getStatusDef());
    }
    
}
