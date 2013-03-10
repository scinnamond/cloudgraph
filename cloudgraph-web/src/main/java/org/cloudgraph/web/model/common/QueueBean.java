package org.cloudgraph.web.model.common;

// java imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;


/**
 */
public abstract class QueueBean extends SerializableDataModel {
    private static Log log =LogFactory.getLog(QueueBean.class);

    private static final long serialVersionUID = 1L;
    protected int scrollerPage = 1;
    protected Integer currentPk = null;
    protected Map<Integer, Object> wrappedData = new HashMap<Integer, Object>();
    protected int maxRows = 10;
    protected List<Object> data = new ArrayList<Object>();
    
    protected BeanFinder beanFinder = new BeanFinder();
    

    public QueueBean() {
    }
    
    public abstract void clear();
    
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
    
	public abstract Query getQuery();
	
    public abstract List<Object> getData();

	public int getScrollerPage() {return scrollerPage;}
	public void setScrollerPage(int scrollerPage) {this.scrollerPage = scrollerPage;}

    /**
     * This method never called from framework.
     * (non-Javadoc)
     * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
     */
    public Object getRowKey() {
    	if (currentPk != null) log.debug("getRowKey: " + currentPk.toString());
    	if (currentPk == null) log.debug("getRowKey: null");
        return currentPk;
    }    
    
    /**
     * This method normally called by Visitor before request Data Row.
     */
    public void setRowKey(Object key) {
        this.currentPk = (Integer) key;
		if (currentPk != null) log.debug("setRowKey: " + currentPk.toString());
		if (currentPk == null) log.debug("setRowKey: null");
    }
	
	
    /**
     * This is main part of Visitor pattern. Method called by framework many times
     * during request processing. 
     */
    public abstract void walk(FacesContext context, DataVisitor visitor, Range range, Object argument)
       throws IOException;
	
    /**
     * This method must return actual data rows count from the Data Provider. It is used by
     * pagination control to determine total number of data items.
     */
    private Integer rowCount; // better to buffer row count locally
    public int getRowCount() {
        if (rowCount==null) {
    		Query qry = getQuery();
	    	SDODataAccessClient service = new SDODataAccessClient();
	    	rowCount = new Integer(service.count(qry));
        	log.debug("getRowCount DB Read: " + rowCount.toString());
            return rowCount.intValue();
        } else {
        	log.debug("getRowCount Cached: " + rowCount.toString());
            return rowCount.intValue();
        }
    }	
	
    /**
     * This is main way to obtain data row. It is intensively used by framework. 
     * We strongly recommend use of local cache in that method. 
     */
    public Object getRowData() {
        if (currentPk==null) {
        	log.debug("getRowData - currentPk: null");
            return null;
        } else {
        	Object ret = wrappedData.get(currentPk);
        	log.debug("getRowData - currentPk: " + currentPk.toString());
        	if (ret == null)
	        	throw new IllegalStateException("cannot fetch wrapped data with pk of " + currentPk.toString());
            return ret;
        }
    }
	
    /**
     * Unused rudiment from old JSF staff.
     */
    public int getRowIndex() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Unused rudiment from old JSF staff.
     */
    public Object getWrappedData() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Never called by framework.
     */
    public boolean isRowAvailable() {
        if (currentPk==null) {
        	log.debug("isRowAvailable: false (currentPk is null)");
            return false;
        } else {
        	boolean isAvail = wrappedData.get(currentPk) != null;
            log.debug("isRowAvailable: " + isAvail + " (currentPk is " + currentPk.toString() + ")");
        	return isAvail;
        }
    }
    
    /**
     * Unused rudiment from old JSF staff.
     */
    public void setRowIndex(int rowIndex) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Unused rudiment from old JSF staff.
     */
    public void setWrappedData(Object data) {
        throw new UnsupportedOperationException();
    }
	
    /**
     * This method suppose to produce SerializableDataModel that will be serialized into View State
     * and used on a post-back. In current implementation we just mark current model as serialized.
     * In more complicated cases we may need to transform data to actually serialized form.
     */
    public  SerializableDataModel getSerializableModel(Range range) {
        if (!wrappedData.isEmpty()) {
            int firstRow = ((SequenceRange)range).getFirstRow();
            int numberOfRows = ((SequenceRange)range).getRows();
        	log.debug("getSerializableModel - range: " + firstRow + " - " + (firstRow + numberOfRows));
            return this; 
        } else {
        	log.debug("getSerializableModel - null");
            return null;
        }
    }	
	
    /**
     * This is helper method that is called by framework after model update. In must delegate actual
     * database update to Data Provider.
     */
    public void update() {
//        AuctionDataModel auctionDataModel = lookupInContext(auctionDataModelExpressionString, AuctionDataModel.class);
//        Object savedKey = getRowKey();
//        for (Integer key : wrappedKeys) {
//            auctionDataModel.setRowKey(key);
//            auctionDataModel.getRowData().setBid(wrappedData.get(key).getBid());
//        }
//        setRowKey(savedKey);
//        //getDataProvider().update();
        
//        this.wrappedData.clear();
//        this.wrappedKeys.clear();
//        resetDataProvider();
    }
    
 }
