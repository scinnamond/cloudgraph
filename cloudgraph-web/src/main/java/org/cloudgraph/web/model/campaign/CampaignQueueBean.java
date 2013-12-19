package org.cloudgraph.web.model.campaign;

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
import org.cloudgraph.web.query.CampaignQuery;
import org.cloudgraph.web.sdo.adapter.CampaignAdapter;
import org.cloudgraph.web.sdo.adapter.QueueAdapter;
import org.cloudgraph.web.sdo.campaign.Campaign;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.primefaces.model.SortOrder;

import commonj.sdo.DataGraph;


/**
 */
@ManagedBean(name="CampaignQueueBean")
@SessionScoped
public class CampaignQueueBean extends PaginatedQueueBean {
    private static Log log =LogFactory.getLog(CampaignQueueBean.class);

    private static final long serialVersionUID = 1L;
    private int scrollerPage = 1;
    private Integer currentPk = null;
    private Map<Integer, CampaignAdapter> wrappedData = new HashMap<Integer, CampaignAdapter>();
    private int maxRows = 15;
    private List<CampaignAdapter> data = new ArrayList<CampaignAdapter>();
    
    private BeanFinder beanFinder = new BeanFinder();
    

    public CampaignQueueBean() {
    }
    
    public void clear() {
    	data.clear();
    }
    
    public String refresh() {   
    	this.clear();
    	return null; // AJAX action method
    }

    
    public void refresh(javax.faces.event.ActionEvent event) {
    	this.clear();
    }

    public int getMaxRows() {
		return maxRows;
	}

	public Query getQuery() {
    	SearchBean searchBean = this.beanFinder.findSearchBean();
        Query query = CampaignQuery.createQuery();
    	
        return query; 
    }
	
    public List<CampaignAdapter> getData() {
		this.data = new ArrayList<CampaignAdapter>();
	    try {
	    	Query qry = getQuery();
	    			    	
	    	SDODataAccessClient service = new SDODataAccessClient();
	    	DataGraph[] results = service.find(qry);
	    	
	        for (int i = 0; i < results.length; i++) {
	        	CampaignAdapter adapter = new CampaignAdapter(
	        			(Campaign)results[i].getRootObject());
	        	data.add(adapter);
	        	wrappedData.put(new Integer(i), adapter); // assumes flat results set
	        }
	    }   
	    catch (Throwable t) {
	    	log.error(t.getMessage(), t);
	    }
    	return this.data;
    }

	@Override
	public List<QueueAdapter> findResults(int startRow, int endRow,
			String sortField, SortOrder sortOrder, Map<String, String> filters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int countResults() {
		// TODO Auto-generated method stub
		return 0;
	}

	
   /* 
    try {
		    	Query qry = getQuery();
		    	
		    	qry.setStartRange(firstRow);
		    	qry.setEndRange(firstRow + numberOfRows);
		    	
		    	SDODataAccessClient service = new SDODataAccessClient();
		    	DataGraph[] results = service.find(qry);
		    	
		        for (int i = 0; i < results.length; i++) {
		        	CampaignAdapter adapter = new CampaignAdapter(
		        			(Campaign)results[i].getRootObject());
		        	data.add(adapter);
		        	wrappedData.put(new Integer(i+firstRow), adapter); // assumes flat results set
		        }
		    }   
		    catch (Throwable t) {
		    	log.error(t.getMessage(), t);
		    }
		}
		
		for (int i = firstRow; i < lastRow; i++)
			visitor.process(context, new Integer(i), argument);
    }
    */
    
	
 }
