package org.cloudgraph.web.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.component.ChartDataSource;
import org.cloudgraph.web.component.ChartDataSourceManager;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.model.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;


import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;



/**
 * Manager class to execute all necessary queries in one round
 * trip at the appropriate lifecycle phase when all registered
 * charts have applied their values to the model.
 */
public class DataSourceManager
    implements ChartDataSourceManager 
{

    private static Log log =LogFactory.getLog(DataSourceManager.class);
    protected BeanFinder beanFinder = new BeanFinder();
    private Map dataSourceMap = new HashMap();
        
    public DataSourceManager() {
		if (log.isDebugEnabled()) {
			log.debug("DataSourceManager(): " + this);
		}        
		try {
            //user = beanFinder.findUserBean();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

	public void refresh(ChartDataSource[] dataSources)
	{
        try {
        	List list = new ArrayList();
        	for (int i = 0; i < dataSources.length; i++)
        	{
        		AbstractDataSource dataSource = (AbstractDataSource)dataSources[i];
        		//if (dataSource.getCurrentDataSet() == null) load always for now
        			list.add(dataSource);
        	}
        	if (list.size() == 0)
        		return;
        	ChartDataSource[] toUpdate = new ChartDataSource[list.size()];
        	list.toArray(toUpdate);
        	Query[] queries = new Query[toUpdate.length];
        	for (int i = 0; i < toUpdate.length; i++)
        	{
        		AbstractDataSource dataSource = (AbstractDataSource)toUpdate[i];
        		queries[i] = dataSource.getQuery();
        		log.debug("adding query ");
        	}
        	
        	SDODataAccessClient serviceProxy = new SDODataAccessClient();
            List<DataGraph[]> results = serviceProxy.find(queries);
            for (int i = 0; i < results.size(); i++)
            {
            	DataGraph[] graphs = results.get(i);
            	DataObject[] dataObjects = new DataObject[graphs.length];
            	for (int j = 0; j < graphs.length; j++)
            		dataObjects[j] = graphs[j].getRootObject();
            	AbstractDataSource dataSource = (AbstractDataSource)toUpdate[i];
            	dataSource.loadDataSet(dataObjects);
            }
        }
        catch (Throwable t) {
        	log.error(t.getMessage(), t);
        }		
	}    

/*	
	private ChartDataSource getDataSource(String id)
	{
		ChartCategory cat = ChartCategory.valueOf(id);
    	AppActions action = AppActions.UNDER_CONSTRUCTION; //this.beanFinder.findLeftNavBean().getSelectedAction();        	
		
		String key = action.toString() + "." + cat.toString();
        ChartDataSource result = (ChartDataSource)dataSourceMap.get(key);
    	if (result == null)
    	{	
     		result = this.getChartDataSource(action, cat);
    		dataSourceMap.put(key, result);
    	}
        return result;
	}

	
    private ChartDataSource getChartDataSource(AppActions action, ChartCategory cat)
    {
    	
        switch (action) {
        default: throw new IllegalArgumentException("huh? " + action.toString());
        }
    }
*/
}
