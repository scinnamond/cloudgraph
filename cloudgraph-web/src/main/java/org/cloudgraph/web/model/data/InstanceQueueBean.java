package org.cloudgraph.web.model.data;

// java imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.model.common.CategorizedPropertySupport;
import org.cloudgraph.web.model.common.PropertySelector;
import org.cloudgraph.web.model.common.QueueBean;
import org.cloudgraph.web.model.configuration.PropertyItem;
import org.cloudgraph.web.model.search.SearchBean;
import org.cloudgraph.web.model.taxonomy.TaxonomyConstants;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.cloudgraph.web.query.PropertyQuery;
import org.cloudgraph.web.query.PropertyViewQuery;
import org.cloudgraph.web.sdo.adapter.InstanceSpecificationQueueAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyViewAdapter;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.PropertyCategorization;
import org.cloudgraph.web.sdo.core.PropertyView;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Property;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;


/**
 */
public class InstanceQueueBean extends QueueBean 
    implements PropertySelector
{
	private static final long serialVersionUID = 1L;

	private static Log log =LogFactory.getLog(InstanceQueueBean.class);

    private CategorizedPropertySupport propertySupport;
	
    protected ReferenceDataCache cache;     
	private String saveActionReRender;

    public InstanceQueueBean() {
    	this.cache = this.beanFinder.findReferenceDataCache();
    	this.cache.getInventoryPerspectiveModel(); // cache/load this
    	this.propertySupport = new CategorizedPropertySupport(this);
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
        	this.data = null; 
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
    	this.data.clear();
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
    	Query query = InstanceSpecificationQuery.createQueueQueryByClassId(searchBean.getClazzId()); ;
        return query; 
    }
	
    public List<Object> getData() {
    	if (this.data == null || this.data.size() == 0) {
			this.data = new ArrayList<Object>();
		    try {
		    	Query qry = getQuery();
		    			    	
		    	SDODataAccessClient service = new SDODataAccessClient();
		    	DataGraph[] results = service.find(qry);
		    	
		        for (int i = 0; i < results.length; i++) {
		        	InstanceSpecification instance = (InstanceSpecification)results[i].getRootObject();
		        	log.info("queue instance: " + instance.dump());
		        	String xml = serializeGraph(results[i]);
		        	log.info("list xml: " + xml); 
		        	InstanceSpecificationQueueAdapter adapter = new InstanceSpecificationQueueAdapter(
		        		instance, getProperties(), 1, 2);
		        	data.add(adapter);
		        	wrappedData.put(new Integer(i), adapter); // assumes flat results set
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
    
    /**
     * This is main part of Visitor pattern. Method called by framework many times
     * during request processing. 
     */
    public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument)
       throws IOException {
        int firstRow = ((SequenceRange)range).getFirstRow();
        int numberOfRows = ((SequenceRange)range).getRows();
		int lastRow = Math.min(firstRow + numberOfRows, getRowCount());
		log.debug("walk from: " + firstRow + " to " + lastRow);
		
		boolean alreadyRead = true;
		for (int i = firstRow; i < lastRow; i++)
		{
			if (wrappedData.get(new Integer(i)) == null)
			{
				alreadyRead = false;
				break;
			}
		}
		
		if (alreadyRead)
		{
			log.debug("Rows " + firstRow + " Thru " + lastRow + " Found In Cache");
		}
		else
//		if (!alreadyRead)
		{
			log.debug("Read DB For Rows " + firstRow + " Thru " + lastRow);
		    try {
		    	Query qry = getQuery();
		    	
		    	qry.setStartRange(firstRow);
		    	qry.setEndRange(firstRow + numberOfRows);
		    	
		    	SDODataAccessClient service = new SDODataAccessClient();
		    	DataGraph[] results = service.find(qry);
		    	
		        for (int i = 0; i < results.length; i++) {
		        	InstanceSpecificationQueueAdapter adapter = new InstanceSpecificationQueueAdapter(
		        			(InstanceSpecification)results[i].getRootObject(),
		        			getProperties(), 1, 2);
		        	data.add(adapter);
		        	wrappedData.put(new Integer(i+firstRow), adapter); // assumes flat results set
		        }
		    }   
		    catch (Throwable t) {
		    	log.error(t.getMessage(), t);
		    }
		}
		
		for (int i = firstRow; i < lastRow; i++)
			visitor.process(context, new Integer(i), argument);
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
}
