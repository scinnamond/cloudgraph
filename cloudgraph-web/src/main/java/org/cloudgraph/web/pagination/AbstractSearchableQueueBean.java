package org.cloudgraph.web.pagination;


// java imports
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.WebConstants;



/**
 *
 */
public abstract class AbstractSearchableQueueBean extends AbstractPagedListBean
    implements WebConstants {
    private static Log log =LogFactory.getLog(
        AbstractSearchableQueueBean.class);

    protected boolean advancedSearch = false;
    protected List statusItems;

    public AbstractSearchableQueueBean() {
    }
     
    public String toggleAdvancedSearch() {  
        if (advancedSearch)
            advancedSearch = false;
        else
            advancedSearch = true;
        return getAction();
    }
    
    private String getAction() {
    	return "";
    }
    
    public boolean getAdvancedSearch()
    {
        return advancedSearch;
    }

    public void setAdvancedSearch(boolean advanced)
    {
        advancedSearch = advanced;
    }

    public void validateAdvancedSearch(FacesContext context, UIComponent component, Object value) {
    	log.debug("here");
    }

    public String getAdvancedSearchAsString()
    {
        return String.valueOf(advancedSearch);
    }

    public void setAdvancedSearchAsString(String advanced)
    {
        advancedSearch = Boolean.parseBoolean(advanced);
    }
    
}
