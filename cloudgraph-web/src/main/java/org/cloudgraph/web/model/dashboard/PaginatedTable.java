package org.cloudgraph.web.model.dashboard;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.ComponentShape;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;


public class PaginatedTable extends Table
{
	
    private static final long serialVersionUID = 1L;
    
	private static Log log = LogFactory.getLog(PaginatedTable.class);
	
    private int scrollerPage = 1;
    private Integer currentPk = null;
    private Map<Integer,Row> wrappedData = new HashMap<Integer,Row>();
	
	public PaginatedTable(ComponentName name, ComponentShape shape,
			Class<?> sdoClass, String[] propertyNames, 
			Dashboard dashboard,
			Container homeContainer) {
		
		super(name, shape, sdoClass, propertyNames, 
				dashboard, homeContainer);
		this.isPaginated = true;
	} // PaginatedTable	
	
	public int getScrollerPage() {return scrollerPage;}
	public void setScrollerPage(int scrollerPage) {this.scrollerPage = scrollerPage;}

    /**
     * This method never called from framework.
     * (non-Javadoc)
     * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
     */
    @Override
    public Object getRowKey() {
    	if (currentPk != null) log.debug("getRowKey: " + currentPk.toString());
    	if (currentPk == null) log.debug("getRowKey: null");
        return currentPk;
    }
    
    
    /**
     * This method normally called by Visitor before request Data Row.
     */
    @Override
    public void setRowKey(Object key) {
        this.currentPk = (Integer) key;
		if (currentPk != null) log.debug("setRowKey: " + currentPk.toString());
		if (currentPk == null) log.debug("setRowKey: null");
    }
	
	
    /**
     * This is main part of Visitor pattern. Method called by framework many times
     * during request processing. 
     */
    @Override
    public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument)
       throws IOException {
        int firstRow = ((SequenceRange)range).getFirstRow();
        int numberOfRows = ((SequenceRange)range).getRows();
		int lastRow = Math.min(firstRow + numberOfRows, getRowCount());
		log.debug("walk from: " + firstRow + " to " + lastRow);
		
		boolean alreadyRead = true;
		for (int i = firstRow; i < lastRow; i++)
		{
			if (wrappedData.get(new Integer(i)) == null)
			{
				alreadyRead = false;
				break;
			}
		}
		
		if (alreadyRead)
		{
			log.debug("Rows " + firstRow + " Thru " + lastRow + " Found In Cache");
		}
		else
//		if (!alreadyRead)
		{
			log.debug("Read DB For Rows " + firstRow + " Thru " + lastRow);
		    try {
		    	Query qry = getQuery();
		    	if (qry == null && getDataSource() != null)
		    		qry = getDataSource().createQuery();
		    	if (qry == null)
		        	throw new IllegalStateException("cannot fetch data with no query or datasource set - please add/set a query or datasource");
		    	
		    	qry.setStartRange(firstRow);
		    	qry.setEndRange(firstRow + numberOfRows);
		    	
		    	SDODataAccessClient service = new SDODataAccessClient();
		    	DataGraph[] results = service.find(qry);
		    	
		        for (int i = 0; i < results.length; i++) {
					Object[] rowData = new Object[getColumns().size()];
		        	DataObject rowDataObject = results[i].getRootObject(); // assumes flat results set
					for (int j = 0; j < getColumns().size(); j++) {
						Column column = getColumns().get(j);
						Object value = rowDataObject.get(column.getDelegate());
						if (value != null)
						    rowData[j] = column.format(value);
						else
							rowData[j] = null;
					}
					Row row = createRow(rowDataObject, rowData);
		        	wrappedData.put(new Integer(i+firstRow), row); // assumes flat results set
		        }
		    }   
		    catch (Throwable t) {
		    	log.error(t.getMessage(), t);
		    }
		}
		
		for (int i = firstRow; i < lastRow; i++)
			visitor.process(context, new Integer(i), argument);
    }
	
	
    /**
     * This method must return actual data rows count from the Data Provider. It is used by
     * pagination control to determine total number of data items.
     */
    private Integer rowCount; // better to buffer row count locally
    @Override
    public int getRowCount() {
        if (rowCount==null) {
    		Query qry = getDataSource().countQuery();
	    	if (qry == null)
	        	throw new IllegalStateException("cannot fetch data with no query or datasource set - please add/set a query or datasource");
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
    @Override
    public Row getRowData() {
        if (currentPk==null) {
        	log.debug("getRowData - currentPk: null");
            return null;
        } else {
            Row ret = wrappedData.get(currentPk);
        	log.debug("getRowData - currentPk: " + currentPk.toString());
        	if (ret == null)
	        	throw new IllegalStateException("cannot fetch wrapped data with pk of " + currentPk.toString());
            return ret;
        }
    }

	
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public int getRowIndex() {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public Object getWrappedData() {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Never called by framework.
     */
    @Override
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
    @Override
    public void setRowIndex(int rowIndex) {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
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
    @Override
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
	
} // class PaginatedTable
