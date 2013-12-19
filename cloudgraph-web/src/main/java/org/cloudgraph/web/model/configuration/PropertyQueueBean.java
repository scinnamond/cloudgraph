package org.cloudgraph.web.model.configuration;

// java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.common.PaginatedQueueBean;
import org.cloudgraph.web.model.search.SearchBean;
import org.cloudgraph.web.query.PropertyViewQuery;
import org.cloudgraph.web.sdo.adapter.PropertyViewAdapter;
import org.cloudgraph.web.sdo.adapter.QueueAdapter;
import org.cloudgraph.web.sdo.core.PropertyView;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.primefaces.model.SortOrder;

import commonj.sdo.DataGraph;


/**
 */
@ManagedBean(name="PropertyQueueBean")
@SessionScoped
public class PropertyQueueBean extends PaginatedQueueBean {
    private static Log log =LogFactory.getLog(PropertyQueueBean.class);

    private static final long serialVersionUID = 1L;
    
    private BeanFinder beanFinder = new BeanFinder();
    protected Map<String, Property.PROPERTY> orderingMap = new HashMap<String, Property.PROPERTY>();
    protected SDODataAccessClient service;
    

    public PropertyQueueBean() {
       	this.service = new SDODataAccessClient();
    }
    
    
    public String refresh() {   
    	this.clear();
    	return null; // AJAX action method
    }

    
    public void refresh(javax.faces.event.ActionEvent event) {
    	this.clear();
    }


	public Query getQuery() {
    	SearchBean searchBean = this.beanFinder.findSearchBean();
        //Query query = PropertyViewQuery.createQuery(
        //		searchBean.getDeputyArea(),
        //		searchBean.getBusinessUnit(),
        //		searchBean.getApplicationName());
    	Query query = null;
    	if (searchBean.getClazzId() != null && searchBean.getClazzId().longValue() != -1)
    		query = PropertyViewQuery.createQueryByClassId(searchBean.getClazzId());
    	else
            query = PropertyViewQuery.createQuery();
    	
         return query; 
    }
	
    public List<PropertyViewAdapter> getData() {
    	List<PropertyViewAdapter> data = new ArrayList<PropertyViewAdapter>();
	    try {
	    	Query qry = getQuery();
	    			    	
	    	SDODataAccessClient service = new SDODataAccessClient();
	    	DataGraph[] results = service.find(qry);
	    	
	        for (int i = 0; i < results.length; i++) {
	        	PropertyViewAdapter adapter = new PropertyViewAdapter(
	        			(PropertyView)results[i].getRootObject());
	        	data.add(adapter);
	        }
	    }   
	    catch (Throwable t) {
	    	log.error(t.getMessage(), t);
	    }
    	return data;
    }


	@Override
	public List<QueueAdapter> findResults(int startRow, int endRow,
			String sortField, SortOrder sortOrder, Map<String, String> filters) {
		// TODO Auto-generated method stub
		Property.PROPERTY ordering = null;
		if (this.currentSortField != null)
		{
			ordering = this.orderingMap.get(this.currentSortField);
			if (ordering == null)
				log.error("no ordering feild found for '"
					+ this.currentSortField + "' - ignoring");
		}
		boolean asc = sortOrder != null && sortOrder.ordinal() != sortOrder.DESCENDING.ordinal();
    	Query qry = getQuery();
    	
    	DataGraph[] graphs = service.find(qry);
    	
    	List<QueueAdapter> results = new ArrayList<QueueAdapter>();
        for (int i = 0; i < graphs.length; i++) {
        	PropertyViewAdapter adapter = new PropertyViewAdapter(
        			(PropertyView)graphs[i].getRootObject());
        	adapter.setIndex(i);
        	results.add(adapter);
        }
        return results;
	}


	@Override
	public int countResults() {
    	Query qry = getQuery();
    	SDODataAccessClient service = new SDODataAccessClient();
    	return service.count(qry);
	}

    
 }
