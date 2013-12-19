package org.cloudgraph.web.model.data;

// java imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.model.common.CategorizedPropertySupport;
import org.cloudgraph.web.model.common.PaginatedQueueBean;
import org.cloudgraph.web.model.common.PropertySelector;
import org.cloudgraph.web.model.configuration.PropertyItem;
import org.cloudgraph.web.model.search.SearchBean;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.cloudgraph.web.sdo.adapter.InstanceSpecificationQueueAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyViewAdapter;
import org.cloudgraph.web.sdo.adapter.QueueAdapter;
import org.cloudgraph.web.sdo.core.PropertyView;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.primefaces.model.SortOrder;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;


/**
 */
@ManagedBean(name="InstanceQueueBean")
@SessionScoped
public class InstanceQueueBean extends PaginatedQueueBean 
    implements PropertySelector
{
	private static final long serialVersionUID = 1L;

	private static Log log =LogFactory.getLog(InstanceQueueBean.class);
    private BeanFinder beanFinder = new BeanFinder();

    private CategorizedPropertySupport propertySupport;
    protected SDODataAccessClient service;
    protected InstanceSpecificationQueueAdapter selectedInstance;
	
    protected ReferenceDataCache cache;     
	private String saveActionReRender;

    public InstanceQueueBean() {
    	this.cache = this.beanFinder.findReferenceDataCache();
    	this.cache.getInventoryPerspectiveModel(); // cache/load this
    	this.propertySupport = new CategorizedPropertySupport(this);
    	this.service = new SDODataAccessClient();
    }
    
	public InstanceSpecificationQueueAdapter getSelectedInstance() {
		return selectedInstance;
	}

	public void setSelectedInstance(
			InstanceSpecificationQueueAdapter selectedInstance) {
		this.selectedInstance = selectedInstance;
	}
	
	public boolean getHasSelectedInstance() {
		return this.selectedInstance != null;
	}	

	public String getSaveActionReRender() {
		return saveActionReRender;
	}

	public void setSaveActionReRender(String saveActionReRender) {
		this.saveActionReRender = saveActionReRender;
	}
    
	public String saveFromAjax() {
		save();
		return null; // maintains AJAX happyness
	}
    	
	public String save() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
        	//this.data = null; 
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
    	super.clear();
    	this.propertySupport.clear();
		SearchBean search = this.beanFinder.findSearchBean();
		this.beanFinder.findReferenceDataCache().expireProperties(search.getClazzId());
    	this.availablePropertiesMap.clear();
    	this.selectedPropertiesMap.clear();
    }

    public List<PropertyAdapter> getProperties() {
    	return this.propertySupport.getProperties();
    }
    
    public int getPropertiesCount() {
    	return this.propertySupport.getPropertiesCount();
    }
    
    public List<PropertyAdapter> getIdentificationProperties() {
    	return this.propertySupport.getIdentificationProperties();
    }
    
    public int getIdentificationPropertiesCount() {
    	return this.propertySupport.getIdentificationPropertiesCount();
    }
    
    public List<PropertyAdapter> getCostProperties() {
    	return this.propertySupport.getCostProperties();
    }
    
    public int getCostPropertiesCount() {
    	return this.propertySupport.getCostPropertiesCount();
    }
    
    public List<PropertyAdapter> getInvestmentProperties() {
    	return this.propertySupport.getInvestmentProperties();
    }
    public int getInvestmentPropertiesCount() {
    	return this.propertySupport.getInvestmentPropertiesCount();
    }
    public List<PropertyAdapter> getHostingProperties() {
    	return this.propertySupport.getHostingProperties();
    }
    public int getHostingPropertiesCount() {
    	return this.propertySupport.getHostingPropertiesCount();
    }
    public List<PropertyAdapter> getProcessProperties() {
    	return this.propertySupport.getProcessProperties();
    }
    public int getProcessPropertiesCount() {
    	return this.propertySupport.getProcessPropertiesCount();
    }
    public List<PropertyAdapter> getComplianceProperties() {
    	return this.propertySupport.getComplianceProperties();
    }
    public int getCompliancePropertiesCount() {
    	return this.propertySupport.getCompliancePropertiesCount();
    }
    public List<PropertyAdapter> getOtherProperties() {
    	return this.propertySupport.getOtherProperties();
    }
    public int getOtherPropertiesCount() {
    	return this.propertySupport.getOtherPropertiesCount();
    }
    
	public Query getQuery() {
    	SearchBean searchBean = this.beanFinder.findSearchBean();
    	Query query = InstanceSpecificationQuery.createQueueQueryByClassId(searchBean.getClazzId());  
        return query; 
    }
	
    public List<InstanceSpecificationQueueAdapter> getData() {
    	List<InstanceSpecificationQueueAdapter> data = new ArrayList<InstanceSpecificationQueueAdapter>();
		    try {
		    	Query qry = getQuery();
		    			    	
		    	DataGraph[] results = service.find(qry);
		    	
		        for (int i = 0; i < results.length; i++) {
		        	InstanceSpecification instance = (InstanceSpecification)results[i].getRootObject();
		        	if (log.isDebugEnabled()) {
		        	    String xml = serializeGraph(results[i]);
		        	    log.debug("list xml: " + xml);
		        	}
		        	InstanceSpecificationQueueAdapter adapter = new InstanceSpecificationQueueAdapter(
		        		instance, getProperties(), 1, 2);
		        	data.add(adapter);
		        }
		    }   
		    catch (Throwable t) {
		    	log.error(t.getMessage(), t);
		    }
		    return data;
    }
    
    protected String serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("queue");
        
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
    
	 
    // FIXME: these maps need to be personalizations
	private Map<Long,List<PropertyItem>> availablePropertiesMap = new HashMap<Long,List<PropertyItem>>();
	public List<PropertyItem> getAvailableProperties() {		
    	SearchBean searchBean = this.beanFinder.findSearchBean();
    	List<PropertyItem> available = availablePropertiesMap.get(searchBean.getClazzId());
	    if (available == null) {
	    	available = new ArrayList<PropertyItem>();
	    	availablePropertiesMap.put(searchBean.getClazzId(), available);
	    }
	    return available;
	}
	
	public void setAvailableProperties(List<PropertyItem> availableList) {
    	SearchBean searchBean = this.beanFinder.findSearchBean();
    	List<PropertyItem> available = availablePropertiesMap.get(searchBean.getClazzId());
	    if (available == null) {
	    	available = new ArrayList<PropertyItem>();
	    	availablePropertiesMap.put(searchBean.getClazzId(), available);
	    }
	    else
	    	available.clear();
	    available.addAll(availableList);
	}	
	
    // FIXME: these maps need to be personalizations
	private Map<Long,List<PropertyItem>> selectedPropertiesMap = new HashMap<Long,List<PropertyItem>>();
	public List<PropertyItem> getSelectedProperties() {
    	SearchBean searchBean = this.beanFinder.findSearchBean();
    	List<PropertyItem> selected = selectedPropertiesMap.get(searchBean.getClazzId());
	    if (selected == null) {
	    	selected = new ArrayList<PropertyItem>();
	    	for (PropertyAdapter adapter : this.propertySupport.getAllProperties())
	    	    selected.add(new PropertyItem(adapter.getId(), 
	    	    		adapter.getName()));
	    	selectedPropertiesMap.put(searchBean.getClazzId(), selected);
	    }
    	return selected;
	}

	public void setSelectedProperties(List<PropertyItem> selectedList) {
    	SearchBean searchBean = this.beanFinder.findSearchBean();
    	List<PropertyItem> selected = selectedPropertiesMap.get(searchBean.getClazzId());
	    if (selected == null) {
	    	selected = new ArrayList<PropertyItem>();
	    	selectedPropertiesMap.put(searchBean.getClazzId(), selected);
	    }
	    else
	    	selected.clear();
    	selected.addAll(selectedList);
	}

	@Override
	public boolean isSelected(Property property) {
	    for (PropertyItem item : getSelectedProperties()) {
	    	if (item.getId() == property.getSeqId()) {
	    		return true;
	    	}
	    }
		return false;
	}

	@Override
	public List<QueueAdapter> findResults(int startRow, int endRow,
			String sortField, SortOrder sortOrder, Map<String, String> filters) {
    	Query qry = getQuery();
    	
    	DataGraph[] graphs = service.find(qry);
    	
    	List<QueueAdapter> results = new ArrayList<QueueAdapter>();
        for (int i = 0; i < graphs.length; i++) {
        	InstanceSpecification instance = (InstanceSpecification)graphs[i].getRootObject();
       	    InstanceSpecificationQueueAdapter adapter = new InstanceSpecificationQueueAdapter(
	        		instance, getProperties(), 1, 2);
       	    adapter.setIndex(i);
        	results.add(adapter);
        	if (log.isDebugEnabled())
        	try {
				log.debug(this.serializeGraph(instance.getDataGraph()));
			} catch (IOException e) {
			}
        }
        return results;
	}

	@Override
	public int countResults() {
    	Query qry = getQuery();
    	return service.count(qry);
	}
}
