package org.cloudgraph.web.pagination;


// java imports
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.model.ModelBean;


/**
 *
 */
public abstract class AbstractListBean extends ModelBean
    implements WebConstants {
    private static Log log =LogFactory.getLog(
        AbstractListBean.class);

    protected List results = new ArrayList();

    protected String sortBy = ""; // start with default index sort column.

    protected boolean ascending = false;


    public AbstractListBean() {
    }
        
    public List getResults() {
        resetListModel();
        return results;
    }

    public abstract void resetListModel();

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
}
