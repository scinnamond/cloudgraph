package org.cloudgraph.web.model.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

@ManagedBean(name="DataListBean")
@RequestScoped
public class DataListBean extends ModelBean{

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(DataListBean.class);
	
    protected List<InstanceSpecificationAdapter> data = new ArrayList<InstanceSpecificationAdapter>();
	private InstanceMap dataMap = new InstanceMap();
	private String classifierName;
	private String previousClassifierName;

	public DataListBean() {
		super();
	}
		
	public InstanceMap getDataMap() {
		return this.dataMap;
	}
	
	private Query getQuery() {
    	Query query = InstanceSpecificationQuery.createQueueQueryByClassifierName(classifierName);
        return query; 
    }

	List<PropertyAdapter> properties;
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
	    
    private List<InstanceSpecificationAdapter> getData() {
		// if classifier name change, re-fetch
    	if (this.previousClassifierName != null && 
		    !this.previousClassifierName.equals(this.classifierName)) {
		    if (this.data != null)
		    	this.data.clear();
		    if (properties != null)
		        properties.clear();
		}
		    
    	if (this.data == null)
    		this.data = new ArrayList<InstanceSpecificationAdapter>();
    		
    	if (this.data.size() == 0)
    	{
			this.data = new ArrayList<InstanceSpecificationAdapter>();
		    try {
		    	Query qry = getQuery();
		    			    	
		    	SDODataAccessClient service = new SDODataAccessClient();
		    	DataGraph[] results = service.find(qry);
		    	
		        for (int i = 0; i < results.length; i++) {
		        	InstanceSpecification instance = (InstanceSpecification)results[i].getRootObject();
		        	//log.info("list dump: " + instance.dump());
		        	//String xml = serializeGraph(instance.getDataGraph());
		        	//log.info("list xml: " + xml); 
		        	InstanceSpecificationAdapter adapter = new InstanceSpecificationAdapter(
    			        instance, this.getProperties(), 1, 2);
		        	data.add(adapter);
		        }
		        
		        Collections.sort(data);
		    }   
		    catch (Throwable t) {
		    	log.error(t.getMessage(), t);
		    }
    	}
    	return this.data;
    }
	
    protected String serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("list");
        
        XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(graph.getRootObject(), 
        		graph.getRootObject().getType().getURI(), 
        		null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PlasmaXMLHelper.INSTANCE.save(doc, os, options);        
        os.flush();
        os.close(); 
        String xml = new String(os.toByteArray());
        return xml;
    }
    
    
	class InstanceMap implements Map<String, List<Object>> {

		private Map<String, List<Object>> theMap = new HashMap<String, List<Object>>();

		@Override
		public int size() {
			return this.theMap.size();
		}

		@Override
		public boolean isEmpty() {
			return this.theMap.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return this.theMap.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return this.theMap.containsValue(value);
		}

		@Override
		public List<Object> get(Object key) {
			previousClassifierName = classifierName;
			classifierName = (String)key;
			List<Object> result = new ArrayList<Object>();
			result.addAll(getData());
			return result;
		}

		@Override
		public List<Object> put(String key, List<Object> value) {
			return this.theMap.put(key, value);
		}

		@Override
		public List<Object> remove(Object key) {
			return this.theMap.remove(key);
		}

		@Override
		public void putAll(Map<? extends String, ? extends List<Object>> m) {
			this.theMap.putAll(m);			
		}

		@Override
		public void clear() {
			this.theMap.clear();			
		}

		@Override
		public Set<String> keySet() {
			return this.theMap.keySet();
		}

		@Override
		public Collection<List<Object>> values() {
			Collection<List<Object>> result = this.theMap.values();
			return result;
		}

		@Override
		public Set<java.util.Map.Entry<String, List<Object>>> entrySet() {
			return this.theMap.entrySet();
		}
	}
}
