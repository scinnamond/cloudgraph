package org.cloudgraph.web.model.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.ModelBean;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.cloudgraph.web.sdo.adapter.InstanceSpecificationAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyAdapter;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Property;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

public class DataListBean extends ModelBean{

	private static Log log = LogFactory.getLog(DataListBean.class);
	
    protected List<Object> data = new ArrayList<Object>();
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
	    
    private List<Object> getData() {
		// if classifier name change, re-fetch
    	if (this.previousClassifierName != null && 
		    !this.previousClassifierName.equals(this.classifierName)) {
		    if (this.data != null)
		    	this.data.clear();
		    if (properties != null)
		        properties.clear();
		}
		    
    	if (this.data == null)
    		this.data = new ArrayList<Object>();
    		
    	if (this.data.size() == 0)
    	{
			this.data = new ArrayList<Object>();
		    try {
		    	Query qry = getQuery();
		    			    	
		    	SDODataAccessClient service = new SDODataAccessClient();
		    	DataGraph[] results = service.find(qry);
		    	
		        for (int i = 0; i < results.length; i++) {
		        	InstanceSpecification instance = (InstanceSpecification)results[i].getRootObject();
		        	//log.info("list dump: " + instance.dump());
		        	//String xml = serializeGraph(results[i]);
		        	//log.info("list xml: " + xml); 
		        	InstanceSpecificationAdapter adapter = new InstanceSpecificationAdapter(
    			        instance, this.getProperties(), 1, 2);
		        	data.add(adapter);
		        }
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
			List<Object> data = getData();
			return data;
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
			return this.theMap.values();
		}

		@Override
		public Set<java.util.Map.Entry<String, List<Object>>> entrySet() {
			return this.theMap.entrySet();
		}
	}
}
