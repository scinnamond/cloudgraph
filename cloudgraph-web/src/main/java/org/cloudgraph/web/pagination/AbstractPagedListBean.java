package org.cloudgraph.web.pagination;


// java imports
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.model.ModelBean;




/**
 *
 */
public abstract class AbstractPagedListBean extends ModelBean
    implements WebConstants {
    private static Log log =LogFactory.getLog(
        AbstractPagedListBean.class);

    protected PagedList results = new PagedList();
    protected String sortBy = ""; // start with default index sort column.
    protected boolean ascending = false;


    public AbstractPagedListBean() {
        log.debug("AbstractPagedListBean()");
    }
        
    public List getResults() {
        resetPagedListModel();
        return results;
    }

    public void clear() {
    	results.clear();
    }
    
    public abstract void resetPagedListModel();

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * isDefaultAscending
     * 
     * @param sortColumn
     *            String
     * @return boolean
     */
    protected boolean isDefaultAscending(String sortColumn) {
        return true;
    }
    
    public String refresh() {   
    	this.clear();
    	return null; // AJAX action method
    }

    public void refresh(javax.faces.event.ActionEvent event) {
    	this.clear();
    }
}
