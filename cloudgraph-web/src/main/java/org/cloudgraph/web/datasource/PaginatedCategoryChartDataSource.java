package org.cloudgraph.web.datasource;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.component.ChartResourceFinder;
import org.cloudgraph.web.component.PagedDataSource;
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



public abstract class PaginatedCategoryChartDataSource extends AbstractCategoryChartDataSource 
    implements WebConstants, PagedDataSource
{

	private static Log log =LogFactory.getLog(
			PaginatedCategoryChartDataSource.class);
	protected int first = 0;
	protected int rows = -1;
	protected int rowcount = -1;

	protected PaginatedCategoryChartDataSource() {
    }
	
	protected PaginatedCategoryChartDataSource(AbstractComponent component, ChartResourceFinder resourceFinder) {
        super(component, resourceFinder);
    }
    
    protected abstract Query createQuery();
   
    public Query getQuery()
    {
    	Query query = createQuery();
        query.setStartRange(this.first);
        query.setEndRange(this.first + this.rows);
    	return query;
    }
    
    public void loadTestDataSet()
    {
    	this.currentDataSet = this.getCategoryTestDataSet(this.first, this.first + this.rows);
    }

    public void setFirst(int first)
    {
    	int oldFirst = this.first;
    	
        this.first = first;
        if (this.first <= 0)
        	this.first = 0;
    	if (this.first != oldFirst) // first index has changed. Dataset obsolete
    	{	
    		if (log.isDebugEnabled())
    		    log.debug("purging dataset");
    		this.purgeCurrentDataSet();
    	}
    }
        
    public void setRows(int rows)
    {
        this.rows = rows;    	
    }
        
    /**
     * Implements ChartDataSource. Since we are a paged
     * data source, for this method, supply the results of the paged operation only.
     * Otherwise clients just wanting the datasource just for the purpose
     * of getting "metadata" from it will end up causing a new query/fetch.
     */
    public Dataset getDataSet() {
    	if (this.currentDataSet != null)
            return this.currentDataSet;
    	else
    	{	
    		return this.getDataSet(this.first, this.first + this.rows);
    	}
    }

    /**
     * Implements PagedDataSource.
     */
    public Dataset getDataSet(int startIndex, int endIndex) {
		if (log.isDebugEnabled()) {
			log.debug("getDataSet: " + startIndex + "," + endIndex);
		}        
		ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {                       
            currentDataSet = getCategoryDataSet(startIndex, endIndex);
            return currentDataSet;
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
    
    /**
     * Implements PagedDataSource. Just executes a count query
     * for the current query and handles errors. 
     */
    public int getRowCount() {
        int result = 0; 

        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
        	SDODataAccessClient serviceProxy = new SDODataAccessClient();
            result = serviceProxy.count(this.createQuery());
            this.rowcount = result;
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            HttpServletResponse response = (HttpServletResponse) FacesContext
                .getCurrentInstance().getExternalContext().getResponse();
            try {
				if (log.isDebugEnabled()) {
					log.debug("redirecting to: "
							+ WebConstants.URL_ERROR_FORWARD);
				}                    
				response.sendRedirect(WebConstants.URL_ERROR_FORWARD);
            } catch (java.io.IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * General category dataset loader based on config data.
     * @param startIndex
     * @param endIndex
     * @return the data set
     */
    protected Dataset getCategoryDataSet(int startIndex, int endIndex) {
    	SDODataAccessClient serviceProxy = new SDODataAccessClient();
        Query query = createQuery();
        query.setStartRange(startIndex);
        query.setEndRange(endIndex);

        DataGraph[] results = serviceProxy.find(query);
        DataObject[] roots = new DataObject[results.length];
        for (int i = 0; i < results.length; i++)
        	roots[i] = results[i].getRootObject(); // assumes flat results set
        
        loadDataSet(roots);
        return this.currentDataSet;
    }

    
    
    /**
     * Creates a randomized data set based on configured status
     * and other values.
     */
    protected Dataset getCategoryTestDataSet(int startIndex, int endIndex) {
        DefaultCategoryDataset result = new DefaultCategoryDataset();

        CategoryChartDef chartDef = ApplicationConfig.getInstance().getCategoryChartDef(this.getComponentName());
        CategoryDef categoryDef = chartDef.getCategoryDef();
        SimpleDateFormat seriesFormat = 
            new SimpleDateFormat(categoryDef.getFormat()); 
        GregorianCalendar cal = new GregorianCalendar();

        Random rand = new Random();

        for (int i = startIndex; i < endIndex; i++) {
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
        }    
        
        return result;
    } 
    
    public String getColorMap()    
    {
        CategoryChartDef chartDef = ApplicationConfig.getInstance().getCategoryChartDef(this.getComponentName());
    	return this.createColorMap(chartDef.getStatusDef());
    }
    
}
