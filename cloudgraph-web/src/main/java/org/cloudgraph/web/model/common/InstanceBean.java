package org.cloudgraph.web.model.common;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.cloudgraph.web.sdo.adapter.InstanceSpecificationAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyAdapter;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Property;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;

import commonj.sdo.DataGraph;

@ManagedBean(name="InstanceBean")
@RequestScoped
public class InstanceBean extends ModelBean 
{
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(InstanceBean.class);	
	
	private Long id;
	private InstanceSpecificationAdapter instance;
	protected String classifierName;
	
	public InstanceBean() {}

	public InstanceBean(String classifierName) {
		log.debug("created InstanceBean");
		this.classifierName = classifierName;
	}
	
	private void initialze()
	{
		try {
	    	Query query = InstanceSpecificationQuery.createQueueQueryByInstanceId(
	    			this.classifierName, this.id);
	    	SDODataAccessClient service = new SDODataAccessClient();
	    	DataGraph[] results = service.find(query);
	    	
	    	this.instance = new InstanceSpecificationAdapter(
	    			(InstanceSpecification)results[0].getRootObject(),
	    			this.getProperties(), 1, 2);
	    }   
	    catch (Throwable t) {
	    	log.error(t.getMessage(), t);
	    }
	}
	
    private List<PropertyAdapter> properties;
	private List<PropertyAdapter> getProperties() {
    	if (properties == null || properties.size() == 0) {
    		properties = new ArrayList<PropertyAdapter>();
	    	List<Property> cached = this.beanFinder.findReferenceDataCache().getProperties(
	    			this.classifierName);
	    	if (cached != null) {
	    		for (Property prop : cached) {
	    			properties.add(new PropertyAdapter(prop));
	    		}
	    	}
    	}
    	return properties; 
    }
	
	public String view() {
       return null;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getClassifierName() {
		return classifierName;
	}
	public void setClassifierName(String classifierName) {
		this.classifierName = classifierName;
	}
	public boolean getHasItem() {
		return this.id != null;
	}

	public InstanceSpecificationAdapter getInstance() {
		if (this.instance == null)
			initialze();
		return this.instance;		
	}
	
}
