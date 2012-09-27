package org.cloudgraph.web.pagination;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.query.model.Property;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;


/**
 */
public abstract class AbstractPaginatedResultsProcessor {

    private static Log log = LogFactory.getLog(AbstractPaginatedResultsProcessor.class);

    private PagedList pagedList = new PagedList();
    protected Type sdoType;
    
    public AbstractPaginatedResultsProcessor() {}
    
    public AbstractPaginatedResultsProcessor(Type sdoType) {
    	this.sdoType = sdoType;
    }
    
	public String getSortColumnString(String column) {
		return null;
	}  
	
    public Property getSortColumnProperty(String column) {
    	if (columnNameIsSDOPropertyName(column)) {
    		return Property.forName(column);
    	}
    	return null;
    }

    protected boolean columnNameIsSDOPropertyName(String column) {
		
        if (this.sdoType != null) {
        	try {
        	    return this.sdoType.getProperty(column) != null;
        	}
        	catch (IllegalArgumentException e) {
        		// 
        	}
        }
        return false;
	}

    /**
     * Processes the results returned by the query before feeding into the JSF
     * datatable
     * 
     * @param results -
     *            results returned from the database
     * @return searchResults - List of objects.
     */
    public List processResults(DataGraph[] results) {

        ArrayList<DataObject> searchResults = new ArrayList<DataObject>();

        for (int i = 0; i < results.length; i++) {
            searchResults.add(results[i].getRootObject());
        }
        return searchResults;
    }

    public void setPagedList(PagedList pagedList) {
        this.pagedList = pagedList;
    }
}
