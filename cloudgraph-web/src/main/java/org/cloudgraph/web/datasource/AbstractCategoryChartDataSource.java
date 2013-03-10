package org.cloudgraph.web.datasource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.component.ChartDataSource;
import org.cloudgraph.web.component.ChartResourceFinder;
import org.cloudgraph.web.config.ApplicationConfig;
import org.cloudgraph.web.config.web.CategoryChartDef;
import org.cloudgraph.web.config.web.CategoryDef;
import org.cloudgraph.web.config.web.StatusDef;
import org.cloudgraph.web.model.dashboard.AbstractComponent;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.plasma.query.model.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;



public abstract class AbstractCategoryChartDataSource extends AbstractDataSource 
    implements ChartDataSource
{

	private static Log log =LogFactory.getLog(
			AbstractCategoryChartDataSource.class);

	protected AbstractCategoryChartDataSource() {
    }
	
	protected AbstractCategoryChartDataSource(AbstractComponent component, ChartResourceFinder resourceFinder) {
        super(component, resourceFinder);
    }
    
    protected abstract Query createQuery();
   
    public Query getQuery()
    {
    	return createQuery();
    }
    
    public void loadTestDataSet()
    {
    	this.currentDataSet = this.getCategoryTestDataSet();
    }
        
    /**
     * Implements ChartDataSource. 
     */
    public Dataset getDataSet() {
    	if (this.currentDataSet != null)
            return this.currentDataSet;
    	else
    	{	
    		return this.getCategoryDataSet();
    	}
    }
    
    /**
     * General category dataset loader based on config data.
     * @return the data set
     */
    protected Dataset getCategoryDataSet() {
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
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        CategoryChartDef chartDef = ApplicationConfig.getInstance().getCategoryChartDef(this.getComponentName());
        CategoryDef categoryDef = chartDef.getCategoryDef();
        SimpleDateFormat seriesFormat = null;
        if (results != null && results.length > 0)
        	if (!results[0].getType().getName().startsWith(chartDef.getTypeName()))
        		throw new IllegalArgumentException("query results entity '" 
        				+ results[0].getType().getName() + "' does not 'resemble' defined chart entity '"
        				+ chartDef.getTypeName() + "'");

        for (int i = 0; i < results.length; i++) {
    		Object categoryValue = results[i].get(categoryDef.getValuePropertyName());
    		String formattedCategoryValue = String.valueOf(categoryValue);
    		if (categoryValue instanceof Date) {
    			if (categoryDef.getFormat() != null && seriesFormat == null)
    				seriesFormat = new SimpleDateFormat(categoryDef.getFormat()); 
    			if (seriesFormat != null)
    		        formattedCategoryValue = seriesFormat.format((Date)categoryValue);
    			else
    				formattedCategoryValue = String.valueOf(categoryValue);
    		}
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
        		    dataset.addValue(statusNumber, statusKey, formattedCategoryValue);
        		}
        		else
        		    throw new RuntimeException("Could not find status key using SDO property '"
        		    		+ chartDef.getTypeName() + "." + chartDef.getStatusKeyPropertyName() 
        		    		+ "' for chart definition '" + chartDef.getName() + "'.");
    		}
    		else {
    			// Assume a columnar data organization where each status definition 
    			// contains a key, value and resource SDO property name. So for each result
    			// we iterate through the status definitions, pulling data from 
    			// a different SDO property. 
	        	for (StatusDef statusDef : chartDef.getStatusDef())
	        	{        	
	        		if (statusDef.getValuePropertyName() == null)
	        			throw new RuntimeException("Since status value property "
	        					+ "name is null, expected non-null status value "
	        					+ "property name for chart definition.");
	        		Object statusValue = results[i].get(statusDef.getValuePropertyName());
	        		if (statusValue == null)
	        			statusValue = new Integer(0);
	        		
	        		if (!(statusValue instanceof Number))
	        		    throw new RuntimeException("status property '"
	        		    		+ chartDef.getTypeName() + "." + statusDef.getValuePropertyName() + "' for chart definition '"
	        		    		+ chartDef.getName() + "' is not a datatype which can be cast to a java.lang.Number "
	        		    		+ "as required for inclusion in a category chart dataset");
	        		Number statusNumber = (Number)statusValue;
	        		if (statusNumber.intValue() == 0 && chartDef.isIgnoreZeroValues())
	        			continue;
	        		String statusKey = null;
	        		if (resourceFinder != null)
	        			statusKey = resourceFinder.getLabel(statusDef.getName());
	        		else
	        			statusKey = statusDef.getName();
	        		dataset.addValue(statusNumber, statusKey, formattedCategoryValue);
	        	}
    		}
        }    
    	this.currentDataSet = dataset;
    }
    
    
    /**
     * Creates a randomized data set based on configured status
     * and other values.
     */
    protected Dataset getCategoryTestDataSet() {
        DefaultCategoryDataset result = new DefaultCategoryDataset();

        CategoryChartDef chartDef = ApplicationConfig.getInstance().getCategoryChartDef(this.getComponentName());
        CategoryDef categoryDef = chartDef.getCategoryDef();
        SimpleDateFormat seriesFormat = 
            new SimpleDateFormat(categoryDef.getFormat()); 
        GregorianCalendar cal = new GregorianCalendar();

        Random rand = new Random();

		String formattedDate = seriesFormat.format(cal.getTime());
    	for (StatusDef statusDef : chartDef.getStatusDef())
    	{
    		//if (statusDef.getAccess().getType() == TrackingAccess.ADMIN_TYPE // FIXME HACK
    		//		&& !this.user.getIsAdmin())
    		//	continue;
    		Integer value = new Integer(rand.nextInt(1000));
    		if (value.intValue() == 0 && chartDef.isIgnoreZeroValues())
    			continue;
    		String status = null;
    		if (resourceFinder != null)
    		    status = resourceFinder.getLabel(statusDef.getName());
    		else
    			status = statusDef.getName();
    		result.addValue(value, status, formattedDate);
    	}
    	cal.set(GregorianCalendar.DAY_OF_YEAR, cal.get(GregorianCalendar.DAY_OF_YEAR)-1);
        
        return result;
    } 
    
    public String getColorMap()    
    {
        CategoryChartDef chartDef = ApplicationConfig.getInstance().getCategoryChartDef(this.getComponentName());
    	return this.createColorMap(chartDef.getStatusDef());
    }
    
}
