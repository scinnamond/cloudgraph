package org.cloudgraph.web.model.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.model.common.CategorizedPropertySupport;
import org.cloudgraph.web.query.ClassQuery;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.cloudgraph.web.query.PackageQuery;
import org.cloudgraph.web.sdo.adapter.CategorizationAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyAdapter;
import org.cloudgraph.web.sdo.adapter.SlotAdapter;
import org.cloudgraph.web.sdo.adapter.TaxonomyAdapter;
import org.cloudgraph.web.sdo.categorization.Taxonomy;
import org.cloudgraph.web.sdo.core.InstanceCategorization;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Package;
import org.cloudgraph.web.sdo.meta.PackageableType;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.sdo.meta.Slot;
import org.cloudgraph.web.sdo.visitor.CategorizationCollector;
import org.cloudgraph.web.sdo.visitor.InstanceCategorizationCollector;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;

public class InstanceEditBean extends ModelBean {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(InstanceEditBean.class);
	
	private Long instanceId;
	private boolean isDelete;
	private InstanceSpecification instance;
	private String saveActionReRender;
    private CategorizedPropertySupport propertySupport;
	
	public InstanceEditBean() {
		log.debug("created InstanceEditBean");
    	this.propertySupport = new CategorizedPropertySupport();
	}

	public boolean getIsDelete() {
		return this.isDelete;
	}
	
	public String getTitle() {
		if (this.instance != null && 
			this.instance.getClazz() != null &&
			this.instance.getClazz().getClassifier() != null)
			return "Create/Edit: " + this.instance.getClazz().getClassifier().getName();
		else
			return "";
	}

	public String getSaveActionReRender() {
		return saveActionReRender;
	}

	public void setSaveActionReRender(String saveActionReRender) {
		this.saveActionReRender = saveActionReRender;
	}

	public String createFromAjax() {
		create();
		return null; // maintains AJAX happyness
	}

	public String create() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
        	this.isDelete = false;
		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] result = service.find(
		    		ClassQuery.createEditQuery(
		    				beanFinder.findSearchBean().getClazzId()));
		    
		    Clazz clzz = (Clazz)result[0].getRootObject();	
		    this.instance = clzz.createInstanceSpecification();
		    this.instance.setExternalId(UUID.randomUUID().toString());
		    
		    result = service.find(PackageQuery.createQuery("model"));
	        Package pkg = (Package)result[0].getRootObject();
	        pkg.setDataGraph(null);	        
	        PackageableType instType = this.instance.createPackageableType();    	
	    	instType.set_package(pkg);
		    
		    clear();
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
        	this.isDelete = false;
		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] result = service.find(InstanceSpecificationQuery.createEditQueryById(this.instanceId));
	        this.instance = (InstanceSpecification)result[0].getRootObject();	
	        clear();
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
        	if (log.isDebugEnabled())
                log.debug(((PlasmaDataObject)this.instance).dump());
		    SDODataAccessClient service = new SDODataAccessClient();
	        service.commit(this.instance.getDataGraph(), 
		    	    beanFinder.findUserBean().getName());
	        clear();
            return AppActions.SAVE.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
        }       
    }	
	
	public String cancelSelectInstance() {
		return null;
	}
        
    public String exit() {
    	try {
    		this.instance.getDataGraph().getChangeSummary().endLogging(); // wipe any changes 
    		this.instance.getDataGraph().getChangeSummary().beginLogging();
    		this.instance = null;   	
        } catch (Throwable t) {
        } finally {
        }       
        return null;
    }
    
	public String deleteConfirm() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
        	this.isDelete = true;
		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] result = service.find(InstanceSpecificationQuery.createDeleteQueryById(this.instanceId));
		    this.instance = (InstanceSpecification)result[0].getRootObject();
	        	        
	        return AppActions.DELETE.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
        }       
    }		

	public String cancelDelete() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
        	this.instance = null;
	        clear();
	        	        
	        return AppActions.SUCCESS.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
        }       
    }	
	
	public String delete() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
		    SDODataAccessClient service = new SDODataAccessClient();
	        this.instance.delete();
	        service.commit(this.instance.getDataGraph(), 
		    	    beanFinder.findUserBean().getName());
	        clear();
	        	        
	        return AppActions.SUCCESS.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
	        this.instance = null;
        }       
    }		

    public void clear() {
    	try {
    		this.propertySupport.clear();
        } catch (Throwable t) {
        } finally {
        }       
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
    
    // 
    public List<SlotAdapter> getIdentificationSlots() {
    	return getSlots(getIdentificationProperties());
    }    
    public int getIdentificationSlotsCount() {
    	return getSlots(getIdentificationProperties()).size();
    }    
    public List<SlotAdapter> getCostSlots() {
    	return getSlots(getCostProperties());
    }
    public int getCostSlotsCount() {
    	return getSlots(getCostProperties()).size();
    }    
    public List<SlotAdapter> getInvestmentSlots() {
    	return getSlots(getInvestmentProperties());
    }
    public int getInvestmentSlotsCount() {
    	return getSlots(getInvestmentProperties()).size();
    }    
    public List<SlotAdapter> getHostingSlots() {
    	return getSlots(getHostingProperties());
    }
    public int getHostingSlotsCount() {
    	return getSlots(getHostingProperties()).size();
    }    
    public List<SlotAdapter> getProcessSlots() {
    	return getSlots(getProcessProperties());
    }
    public int getProcessSlotsCount() {
    	return getSlots(getProcessProperties()).size();
    }    
    public List<SlotAdapter> getComplianceSlots() {
    	return getSlots(getComplianceProperties());
    }
    public int getComplianceSlotsCount() {
    	return getSlots(getComplianceProperties()).size();
    }    
    
    public List<SlotAdapter> getOtherSlots() {
    	return getSlots(getOtherProperties());
    }
    public int getOtherSlotsCount() {
    	return getSlots(getOtherProperties()).size();
    }    

    private List<SlotAdapter> getSlots(List<PropertyAdapter> properties) {
    	List<SlotAdapter> slotList = new ArrayList<SlotAdapter>();
    	for (PropertyAdapter propAdapter : properties) {
    		Property prop = propAdapter.getProperty();
    		SlotAdapter adapter = null;
    		Slot slot = getSlot(prop.getSeqId());
    		if (slot != null)
    			adapter = new SlotAdapter(slot, prop);
    		else
    			adapter = new SlotAdapter(this.instance, prop);
    		slotList.add(adapter);
    	}
    	return slotList;
    }
    
    private Slot getSlot(Long propertySeqId) {
        for (Slot slot : this.instance.getSlot()) {
     	   if (slot.getDefiningFeature().getSeqId() == propertySeqId) {
     		   return slot;
     	   }
        }
        return null;
    }  
	
	public Long getInstanceId() {
		return this.instanceId;
	}

	public void setInstanceId(Long selected) {
		this.instanceId = selected;
	}
	
    public boolean getHasInstance() {
		return this.instance != null;
	}
    
    public InstanceSpecification getInstance() {
		return this.instance;
	}

	public void setInstance(InstanceSpecification selectedInstance) {
		this.instance = selectedInstance;
	}
	
    public DataObject getInstanceDataObject() {
		return this.instance;
	}
	
    public List<TaxonomyAdapter> getTaxonomies() {
    	List<TaxonomyAdapter> result = new ArrayList<TaxonomyAdapter>();
    	ReferenceDataCache cache = this.beanFinder.findReferenceDataCache();
    	
    	CategorizationCollector visitor = 
    		new InstanceCategorizationCollector(
    				PlasmaTypeHelper.INSTANCE.getType(InstanceCategorization.class),
    				this.beanFinder.findReferenceDataCache());
    	visitor.setCollectOnlyInitializedTaxonomies(true);
    	// make  sure specific taxonomies are loaded we intend to use
    	// for views rendering this model 
     	visitor.initializeTaxonomy(cache.getSegmentArchitectureModel());   	
    	
     	((PlasmaDataObject)this.instance).accept(visitor);
    	Map<Taxonomy, List<CategorizationAdapter>> taxonomyMap = visitor.getResult();
    	
    	Iterator<Taxonomy> iter = taxonomyMap.keySet().iterator();
    	while (iter.hasNext()) {
    		Taxonomy tax = iter.next();
    		List<CategorizationAdapter> pcats = taxonomyMap.get(tax);
    		result.add(new TaxonomyAdapter(tax, pcats));
    	}
    	    
    	return result;
    }
	
}
