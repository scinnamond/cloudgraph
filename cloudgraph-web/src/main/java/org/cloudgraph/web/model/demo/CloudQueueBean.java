package org.cloudgraph.web.model.demo;

// java imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.common.PaginatedQueueBean;
import org.cloudgraph.web.sdo.adapter.QueueAdapter;
import org.cloudgraph.web.util.BeanFinder;


/**
 */
public abstract class CloudQueueBean extends PaginatedQueueBean {
    private static Log log =LogFactory.getLog(CloudQueueBean.class);

    private static final long serialVersionUID = 1L;
    
    protected BeanFinder beanFinder = new BeanFinder();
    

    public CloudQueueBean() {
    }
    
    
    public String refresh() {   
    	this.clear();
    	return null; // AJAX action method
    }
    
    public void refresh(javax.faces.event.ActionEvent event) {
    	this.clear();
    }
    
    public abstract List<QueueAdapter> getData();

 }
