package org.cloudgraph.web.model.data;

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
import org.cloudgraph.web.model.search.SearchBean;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.cloudgraph.web.query.PropertyQuery;
import org.cloudgraph.web.query.PropertyViewQuery;
import org.cloudgraph.web.sdo.adapter.InstanceSpecificationQueueAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyViewAdapter;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;

import org.cloudgraph.web.sdo.core.PropertyView;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;

import commonj.sdo.DataGraph;


/**
 */
public class InstanceBrowserQueueBean extends InstanceQueueBean {
	private static final long serialVersionUID = 1L;

	private static Log log =LogFactory.getLog(InstanceBrowserQueueBean.class);

    private Long classId = new Long(-1);     
    

    public InstanceBrowserQueueBean() {
    }
    
    public Long getClassId() {
		return classId;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}   
    
	public Query getQuery() {
		return InstanceSpecificationQuery.createQueueQueryByClassId(
				this.classId);    	
    }
 }
