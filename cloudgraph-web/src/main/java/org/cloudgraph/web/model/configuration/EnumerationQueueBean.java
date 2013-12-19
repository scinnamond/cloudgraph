package org.cloudgraph.web.model.configuration;

// java imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.common.PaginatedQueueBean;
import org.cloudgraph.web.model.search.SearchBean;
import org.cloudgraph.web.query.EnumerationViewQuery;
import org.cloudgraph.web.sdo.adapter.EnumerationViewAdapter;
import org.cloudgraph.web.sdo.adapter.QueueAdapter;
import org.cloudgraph.web.sdo.core.EnumerationView;
import org.cloudgraph.web.sdo.meta.Enumeration;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.primefaces.model.SortOrder;

import commonj.sdo.DataGraph;


/**
 */
@ManagedBean(name="EnumerationQueueBean")
@SessionScoped
public class EnumerationQueueBean extends PaginatedQueueBean {
	
    private static Log log =LogFactory.getLog(EnumerationQueueBean.class);

    private static final long serialVersionUID = 1L;
    
    private BeanFinder beanFinder = new BeanFinder();
    protected Map<String, Enumeration.PROPERTY> orderingMap = new HashMap<String, Enumeration.PROPERTY>();
    protected SDODataAccessClient service;


    public EnumerationQueueBean() {
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
        Query query = EnumerationViewQuery.createQuery();
    	
         return query; 
    }
	
    public List<EnumerationViewAdapter> getData() {
    	ArrayList<EnumerationViewAdapter> data = new ArrayList<EnumerationViewAdapter>();
	    try {
	    	Query qry = getQuery();
	    			    	
	    	SDODataAccessClient service = new SDODataAccessClient();
	    	DataGraph[] results = service.find(qry);
	    	
	        for (int i = 0; i < results.length; i++) {
	        	EnumerationViewAdapter adapter = new EnumerationViewAdapter(
	        			(EnumerationView)results[i].getRootObject());
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
		Enumeration.PROPERTY ordering = null;
		if (this.currentSortField != null)
		{
			ordering = this.orderingMap.get(this.currentSortField);
			if (ordering == null)
				log.error("no ordering feild found for '"
					+ this.currentSortField + "' - ignoring");
		}
		boolean asc = sortOrder != null && sortOrder.ordinal() != sortOrder.DESCENDING.ordinal();
    	Query qry = getQuery();
    	
    	SDODataAccessClient service = new SDODataAccessClient();
    	DataGraph[] graphs = service.find(qry);
    	
    	List<QueueAdapter> results = new ArrayList<QueueAdapter>();
        for (int i = 0; i < graphs.length; i++) {
        	EnumerationViewAdapter adapter = new EnumerationViewAdapter(
        			(EnumerationView)graphs[i].getRootObject());
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
