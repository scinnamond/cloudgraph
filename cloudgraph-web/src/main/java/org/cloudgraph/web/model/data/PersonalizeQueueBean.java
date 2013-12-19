package org.cloudgraph.web.model.data;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.query.PropertyViewQuery;
import org.cloudgraph.web.sdo.adapter.PropertyViewAdapter;
import org.cloudgraph.web.sdo.core.PropertyView;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;

import commonj.sdo.DataGraph;

@ManagedBean(name="PersonalizeQueueBean")
@SessionScoped
public class PersonalizeQueueBean extends ModelBean {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(PersonalizeQueueBean.class);
	
	private Long clazzId;
	private String saveActionReRender;
	
	public PersonalizeQueueBean() {
		log.debug("created PersonalizeQueueBean");
	}

	public String getSaveActionReRender() {
		return saveActionReRender;
	}

	public void setSaveActionReRender(String saveActionReRender) {
		this.saveActionReRender = saveActionReRender;
	}

	public String getTitle() {
		return "";
	}

	public String createFromAjax() {
		create();
		return null; // maintains AJAX happyness
	}

	public String create() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
	    	return AppActions.CREATE.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
        }       
    }			
	
	public String editFromAjax() {
		edit();
		return null; // maintains AJAX happyness
	}

	public String edit() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
	        return AppActions.EDIT.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
        }       
    }		
	
	public String saveFromAjax() {
		save();
		return null; // maintains AJAX happyness
	}
    	
	public String save() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
            return AppActions.SAVE.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
        }       
    }
	
    public String cancel() {
    	try {
        } catch (Throwable t) {
        	log.error(t.getMessage(), t);
        } finally {
        }       
        return null;
    }
        
    public String exit() {
    	try {
        } catch (Throwable t) {
        	log.error(t.getMessage(), t);
        } finally {
        }       
        return null;
    }
    
    public void clear() {
    	try {

        } catch (Throwable t) {
        } finally {
        }       
    }

	public Long getClazzId() {
		return clazzId;
	}

	public void setClazzId(Long clazzId) {
		this.clazzId = clazzId;
	}	
        
	List<PropertyViewAdapter> availableProperties;
	public List<PropertyViewAdapter> getAvailableProperties() {
		if (this.clazzId == null)
			return new ArrayList<PropertyViewAdapter>();
		
		if (availableProperties == null) {
			availableProperties = new ArrayList<PropertyViewAdapter>();
		    try {
		    	Query query = PropertyViewQuery.createQueryByClassId(this.clazzId);
			    
			    SDODataAccessClient service = new SDODataAccessClient();
			    DataGraph[] results = service.find(query);
			    for (int i = 0; i < results.length; i++) {
			    	PropertyView prop = (PropertyView)results[i].getRootObject();
			    	availableProperties.add(new PropertyViewAdapter(prop));
			    }
		    }
		    catch (Throwable t) {
		    	log.error(t.getMessage(), t);
		    }	
		}
		
		return availableProperties;
	}
	
	public void setAvailableProperties(List<PropertyViewAdapter> available) {
		availableProperties = available;
	}
	
	List<PropertyViewAdapter> selectedProperties = new ArrayList<PropertyViewAdapter>();

	public List<PropertyViewAdapter> getSelectedProperties() {
		return selectedProperties;
	}

	public void setSelectedProperties(List<PropertyViewAdapter> selected) {
		
		
		this.selectedProperties = selected;
	}
	
}
