package org.cloudgraph.web.model.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.ResourceManager;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.query.PropertyQuery;
import org.cloudgraph.web.sdo.adapter.CategorizationAdapter;
import org.cloudgraph.web.sdo.adapter.TaxonomyAdapter;
import org.cloudgraph.web.sdo.visitor.CategorizationCollector;
import org.cloudgraph.web.sdo.visitor.PropertyCategorizationCollector;
import org.cloudgraph.web.util.BeanFinder;
import org.cloudgraph.web.util.ResourceUtils;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaCopyHelper;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import org.cloudgraph.web.sdo.categorization.Taxonomy;
import org.cloudgraph.web.sdo.core.PropertyCategorization;
import org.cloudgraph.web.sdo.meta.Classifier;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.DataType;
import org.cloudgraph.web.sdo.meta.Enumeration;
import org.cloudgraph.web.sdo.meta.Package;
import org.cloudgraph.web.sdo.meta.PrimitiveType;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.sdo.meta.VisibilityKind;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class PropertyEditBean extends ModelBean {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(PropertyEditBean.class);

	private Long propertyId;
	private Property property;
	private int type;
	
	public PropertyEditBean() {
		log.debug("created PropertyEditBean");
	}

	public String getTitle() {
		if (this.property != null)
			return "Edit Attribute: " + this.property.getName();
		else
			return "";
	}


	public String createFromAjax() {
		create();
		return null; // maintains AJAX happyness
	}

	public String create() {
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
        	DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
            dataGraph.getChangeSummary().beginLogging(); // log changes from this point
        	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Property.class);
        	this.property = (Property)dataGraph.createRootObject(rootType);
        	this.property.setName("New Property");
        	this.property.setExternalId(UUID.randomUUID().toString());
        	this.property.setVisibility(VisibilityKind.PUBLIC.getInstanceName()); 
        	this.property.setLowerValue(0);
        	this.property.setUpperValue("1");
        	
        	// default datatype to a primitive
        	this.type = PropertyType.PRIMITIVE.ordinal();
        	
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
		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] result = service.find(PropertyQuery.createEditQuery(this.propertyId));
	        this.property = (Property)result[0].getRootObject();
	        
	        // determine in datatype classifier is a primitive, enum or class
	        if (this.property.getDataType() != null) {
	        	if (this.property.getDataType().getDataTypeCount() > 0) {
	        	    if (this.property.getDataType().getDataType(0).getPrimitiveTypeCount() > 0)
	        		    this.type = PropertyType.PRIMITIVE.ordinal();
	        	    else if (this.property.getDataType().getDataType(0).getEnumerationCount() > 0)
	        		    this.type = PropertyType.ENUMERATION.ordinal();
	        	}
	        	else if (this.property.getDataType().getClazzCount() > 0)
	        		this.type = PropertyType.CLASS.ordinal();
	        	else
	        		this.type = PropertyType.PRIMITIVE.ordinal();
	        }
	        else
	        	this.type = PropertyType.PRIMITIVE.ordinal();
	        
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
                log.debug(((PlasmaDataObject)property).dump());
		    SDODataAccessClient service = new SDODataAccessClient();
	        service.commit(property.getDataGraph(), 
		    	    beanFinder.findUserBean().getName());
        	// we changed or created one
	        beanFinder.findReferenceDataCache().expireProperties(
        			getOwnerClassId());
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
        
    public String exit() {
    	try {
	    	property.getDataGraph().getChangeSummary().endLogging(); // wipe any changes 
	    	property.getDataGraph().getChangeSummary().beginLogging();
			this.property = null;   	
        } catch (Throwable t) {
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

    public Long getOwnerClassId() {
    	if (this.property != null && this.property.getSourceClass() != null)
    		return this.property.getSourceClass().getSeqId();
    	else
    		return new Long(-1);    		
    }
    
    public void setOwnerClassId(Long id) {
    	if (id != null && this.property != null &&
    		this.property.getSourceClass() != null &&	
    		this.property.getSourceClass().getSeqId() == id)
        	return; // no change - thanks anyway JSF
    	if (this.property != null) {
    		boolean found = false;
		    for (Clazz type : beanFinder.findReferenceDataCache().getClasses()) {
		    	if (type.getSeqId() == id) {
		    		Clazz copy = (Clazz)PlasmaCopyHelper.INSTANCE.copyShallow(type);
		    		this.property.setSourceClass(copy);
		    		found = true;
		    		break;
		    	}
		    }
		    if (!found)
		    	log.error("could not find Clazz id: " + id);
    	}
    }
    
    public void validateOwnerClassId(FacesContext facesContext,
            UIComponent component, Object value) {

    	if (value == null || 
    		((Long)value).intValue() == -1) 
    	{
    		String label = "Owner Class";
    		try {
    		    label = ResourceManager.instance().getString("aplsPropertyEdit_ownerClass_label");
            }
            catch (MissingResourceException e) {
            }
            String msg = label + " is a required field";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}
	}
    
	public List<SelectItem> getOwnerClassItems() {
		List<SelectItem> classItems = new ArrayList<SelectItem>();
		SelectItem item = new SelectItem(new Long(-1), 
    			WebConstants.DEFAULT_SELECTION);
		classItems.add(item);
		for (Clazz type : beanFinder.findReferenceDataCache().getClasses()) {    			
			String def = type.getClassifier().getDefinition();
			if (def != null && def.length() > 26)
				def = def.substring(0, 23) + "...";
			classItems.add(new SelectItem(
					type.getSeqId(), 
					type.getClassifier().getName(),
					def));
    	}
    	return classItems;
	}	    
    
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public void typeChange(javax.faces.event.ValueChangeEvent e) {
		
       //FacesContext.getCurrentInstance().renderResponse();
	}
	
	List<SelectItem> typeItems;
    public List<SelectItem> getTypeItems() {
    	if (typeItems == null) {
    		typeItems = new ArrayList<SelectItem>();    		
	    	//SelectItem item = new SelectItem(WebConstants.DEFAULT_SELECTION, 
	    	//		WebConstants.DEFAULT_SELECTION);
	    	//typeItems.add(item);
	    	PropertyType[] phases = PropertyType.values();    		
    		for (int i = 0; i < phases.length; i++) {
    			String key = ResourceUtils.constructResourceLabelKey(PropertyType.class, phases[i].name());
    			String displayName = null;
    			try {
    			    displayName = ResourceManager.instance().getString(key);
    		    }
    			catch (MissingResourceException e) {
    				displayName = phases[i].name();
    			}
    			typeItems.add(new SelectItem(phases[i].ordinal(),
    					displayName));
    		}    		
   	    }
    	
    	return typeItems;
    }
    
	public int getNameMaxLength() {
		PlasmaProperty nameProp = 
			(PlasmaProperty)this.property.getType().getProperty(
					Property.PTY_NAME);
		return nameProp.getMaxLength();
	}
	
	public int getDefinitionMaxLength() {
		PlasmaProperty nameProp = 
			(PlasmaProperty)this.property.getType().getProperty(
					Property.PTY_DEFINITION);
		return nameProp.getMaxLength();
	}
	
	
    public void validateDefinitionLength(FacesContext facesContext,
            UIComponent component, Object value) {
		String label = "Definition";
    	String text = null;
    	if (value == null || ((String)value).trim().length() == 0) {
    		return;
    	}
    	else
    		text = ((String)value).trim();

    	int max = this.getDefinitionMaxLength();
    	if (text.length() > max) {
            String msg = label + " is longer than allowed maximum "
                + String.valueOf(max) + " characters";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}  	
	}	

	public List<SelectItem> getPrimitiveTypeItems() {
		List<SelectItem> list = this.beanFinder.findReferenceDataCache().getPrimitiveTypeItems();
		return list.subList(1, list.size());
	}	
	
	public List<SelectItem> getEnumerationItems() {
		List<SelectItem> list = this.beanFinder.findReferenceDataCache().getEnumerationItems();
		return list.subList(1, list.size());
	}
	
	public List<SelectItem> getClassItems() {
		List<SelectItem> list = this.beanFinder.findReferenceDataCache().getClassItems();
		return list.subList(1, list.size());
	}
	
	public String getSelectedDataTypeName() {
		Classifier cl = this.property.getDataType();
		if (cl != null)
			return cl.getName();
		else
			return "NOT SELECTED";
	}
	
	public void setSelectedDataTypeName(String dummy) {
	}
	
	public Long getDataTypeId() {
		if (this.property != null && this.property.getDataType() != null) {
			long result = this.property.getDataType().getSeqId();
			return result;
		}
    	else
    		return new Long(-1);
    }

    public void setDataTypeId(Long id) {
    	if (id != null && this.property != null &&
    		this.property.getDataType() != null &&	
    		this.property.getDataType().getSeqId() == id)
        	return; // no change - thanks anyway JSF
    	boolean found = false;
    	switch (this.getType()) {
    	case 0:
		    for (PrimitiveType type : beanFinder.findReferenceDataCache().getPrimitiveTypes()) {
		    	if (type.getDataType().getClassifier().getSeqId() == id) {
		    		Classifier copy = (Classifier)PlasmaCopyHelper.INSTANCE.copyShallow(
		    				type.getDataType().getClassifier());
		    		this.property.setDataType(copy);
		    		found = true;
		    		break;
		    	}
		    }
		    break;
    	case 1:
    	    for (Enumeration type : beanFinder.findReferenceDataCache().getEnumerations()) {
    	    	if (type.getDataType().getClassifier().getSeqId() == id) {
    	    		Classifier copy = (Classifier)PlasmaCopyHelper.INSTANCE.copyShallow(
    	    				type.getDataType().getClassifier());
    	    		this.property.setDataType(copy);
    	    		found = true;
    	    		break;
    	    	}
    	    }
    	    break;
    	case 2:
    	    for (Clazz type : beanFinder.findReferenceDataCache().getClasses()) {
    	    	if (type.getClassifier().getSeqId() == id) {
    	    		Classifier copy = (Classifier)PlasmaCopyHelper.INSTANCE.copyShallow(
    	    				type.getClassifier());
    	    		this.property.setDataType(copy);
    	    		found = true;
    	    		break;
    	    	}
    	    }
    	    break;
    	}
	    if (!found)
	    	log.error("could not find DataType id: " + id);
    }

    public void validateDataTypeId(FacesContext facesContext,
            UIComponent component, Object value) {

    	if (value == null || 
    		((Long)value).intValue() == -1) 
    	{
    		String label = "Data Type";
    		try {
    		    label = ResourceManager.instance().getString("aplsPropertyEdit_dataType_label");
            }
            catch (MissingResourceException e) {
            }
            String msg = label + " is a required field";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}
	}
     
    public int getIsRequired() {
    	return this.property.getLowerValue();
    }
	
    public void setIsRequired(int set) {
    	this.property.setLowerValue(set);
    }
    
	private static List<SelectItem> requiredItems;
	public List<SelectItem> getRequiredItems() {
		if (requiredItems == null) {
			requiredItems = new ArrayList<SelectItem>();
			SelectItem falseItem = new SelectItem(new Integer(0), 
					"no");
			SelectItem trueItem = new SelectItem(new Integer(1), 
			    "yes");
			requiredItems.add(trueItem);
			requiredItems.add(falseItem);
		}
		return requiredItems;
	}

    public String getIsMany() {
    	return this.property.getUpperValue();
    }
	
    public void setIsMany(String set) {
    	this.property.setUpperValue(set);
    }
    
	private static List<SelectItem> manyItems;
	public List<SelectItem> getManyItems() {
		if (manyItems == null) {
			manyItems = new ArrayList<SelectItem>();
			SelectItem falseItem = new SelectItem("1", 
					"no");
			SelectItem trueItem = new SelectItem("*", 
			    "yes");
			manyItems.add(trueItem);
			manyItems.add(falseItem);
		}
		return manyItems;
	}
	
	public Long getPropertyId() {
		return this.propertyId;
	}

	public void setPropertyId(Long selected) {
		this.propertyId = selected;
	}
	
    public boolean getHasProperty() {
		return this.property != null;
	}
    
    public Property getProperty() {
		return this.property;
	}

	public void setProperty(Property selectedProperty) {
		this.property = selectedProperty;
	}
	
    public DataObject getPropertyDataObject() {
		return this.property;
	}
	
    public List<TaxonomyAdapter> getTaxonomies() {
    	List<TaxonomyAdapter> result = new ArrayList<TaxonomyAdapter>();
    	ReferenceDataCache cache = this.beanFinder.findReferenceDataCache();
    	
    	CategorizationCollector visitor = 
    		new PropertyCategorizationCollector(
    				PlasmaTypeHelper.INSTANCE.getType(PropertyCategorization.class),
    				this.beanFinder.findReferenceDataCache());
    	visitor.setCollectOnlyInitializedTaxonomies(true);
    	// make  sure specific taxonomies are loaded we intend to use
    	// for views rendering this model 
    	visitor.initializeTaxonomy(cache.getBusinessReferenceModel());
    	visitor.initializeTaxonomy(cache.getServiceReferenceModel());
    	visitor.initializeTaxonomy(cache.getTechnicalReferenceModel());   	
     	visitor.initializeTaxonomy(cache.getSegmentArchitectureModel());   	
    	
     	((PlasmaDataObject)this.property).accept(visitor);
    	Map<Taxonomy, List<CategorizationAdapter>> taxonomyMap = visitor.getResult();
    	
    	Iterator<Taxonomy> iter = taxonomyMap.keySet().iterator();
    	while (iter.hasNext()) {
    		Taxonomy tax = iter.next();
    		List<CategorizationAdapter> pcats = taxonomyMap.get(tax);
    		result.add(new TaxonomyAdapter(tax, pcats));
    	}
    	    
    	return result;
    }
	
    public List<TaxonomyAdapter> getPerspectives() {
    	List<TaxonomyAdapter> result = new ArrayList<TaxonomyAdapter>();
    	ReferenceDataCache cache = this.beanFinder.findReferenceDataCache();
    	
    	CategorizationCollector visitor = 
    		new PropertyCategorizationCollector(
    				PlasmaTypeHelper.INSTANCE.getType(PropertyCategorization.class),
    				this.beanFinder.findReferenceDataCache());
    	visitor.setCollectOnlyInitializedTaxonomies(true);
    	// make  sure specific taxonomies are loaded we intend to use
    	// for views rendering this model 
    	visitor.initializeTaxonomy(cache.getInventoryPerspectiveModel());
    	visitor.initializeTaxonomy(cache.getICAMPerspectiveModel());
    	visitor.initializeTaxonomy(cache.getGemsPerspectiveModel());
    	
     	((PlasmaDataObject)this.property).accept(visitor);
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
