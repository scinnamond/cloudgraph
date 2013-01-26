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
import org.cloudgraph.web.query.ClassQuery;
import org.cloudgraph.web.query.ClassViewQuery;
import org.cloudgraph.web.query.PackageQuery;
import org.cloudgraph.web.query.PropertyViewQuery;
import org.cloudgraph.web.sdo.adapter.CategorizationAdapter;
import org.cloudgraph.web.sdo.adapter.ClassViewAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyViewAdapter;
import org.cloudgraph.web.sdo.adapter.TaxonomyAdapter;
import org.cloudgraph.web.sdo.visitor.CategorizationCollector;
import org.cloudgraph.web.sdo.visitor.ClassCategorizationCollector;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaCopyHelper;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import org.cloudgraph.web.sdo.categorization.Taxonomy;
import org.cloudgraph.web.sdo.core.ClassCategorization;
import org.cloudgraph.web.sdo.core.ClassView;
import org.cloudgraph.web.sdo.core.PropertyView;
import org.cloudgraph.web.sdo.meta.Classifier;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.Package;
import org.cloudgraph.web.sdo.meta.PackageableType;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;

public class ClassEditBean extends ModelBean {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ClassEditBean.class);
	// these high level cats are part of "App Inventory Model" 
	// taxonomy
	private static final String CAT_COST = "Cost";	
	private static final String CAT_HOSTING = "Hosting";	
	private static final String CAT_PROCESSES = "Processes";	
	private static final String CAT_INVESTMENT = "Investment";
	private static final String CAT_COMPLIANCES = "Compliances";
	private static final String CAT_OTHER = "Other";

	private List<PropertyViewAdapter> otherProperties = null;
	private List<PropertyViewAdapter> compliancesProperties = null;
	private List<PropertyViewAdapter> investmentProperties = null;
	private List<PropertyViewAdapter> costProperties = null;
	private List<PropertyViewAdapter> hostingProperties = null;
	

	
	private Long clazzId;
	private Clazz clazz;
	
	public ClassEditBean() {
		log.debug("created ClassEditBean");
	}

	public String getTitle() {
		if (this.clazz != null)
			return "Edit Class: " + this.clazz.getClassifier().getName();
		else
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
        	
		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] result = service.find(PackageQuery.createQuery("model"));
	        Package pkg = (Package)result[0].getRootObject();
	    	PackageableType superclassType = pkg.createPackageableType();    	
	    	Classifier classifier = superclassType.createClassifier();
	    	classifier.setExternalId(UUID.randomUUID().toString());
	    	this.clazz = classifier.createClazz();
	    	this.clazz.setExternalId(UUID.randomUUID().toString());
	    	classifier.setName("New Class");    	
	        
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
		    DataGraph[] result = service.find(ClassQuery.createEditQuery(this.clazzId));
	        this.clazz = (Clazz)result[0].getRootObject();	
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
                log.debug(((PlasmaDataObject)this.clazz).dump());
		    SDODataAccessClient service = new SDODataAccessClient();
	        service.commit(this.clazz.getDataGraph(), 
		    	    beanFinder.findUserBean().getName());
	        beanFinder.findReferenceDataCache().expireClasses();
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
    		this.clazz.getDataGraph().getChangeSummary().endLogging(); // wipe any changes 
    		this.clazz.getDataGraph().getChangeSummary().beginLogging();
    		this.clazz = null;   	
        } catch (Throwable t) {
        } finally {
        }       
        return null;
    }

    public void clear() {
    	try {
    		otherProperties = null;
    		compliancesProperties = null;
    		investmentProperties = null;
    		costProperties = null;
    		hostingProperties = null;
        } catch (Throwable t) {
        } finally {
        }       
    }
    
    
	public List<PropertyViewAdapter> getOtherProperties() {
		if (this.otherProperties == null)
			this.otherProperties = fetchProperties(CAT_OTHER);		
		return this.otherProperties;
	}	

	public List<PropertyViewAdapter> getCompliancesProperties() {
		if (this.compliancesProperties == null)
			this.compliancesProperties = fetchProperties(CAT_COMPLIANCES);		
		return this.compliancesProperties;
	}	

	public List<PropertyViewAdapter> getInvestmentProperties() {
		if (this.investmentProperties == null)
			this.investmentProperties = fetchProperties(CAT_INVESTMENT);		
		return this.investmentProperties;
	}		
	
	public List<PropertyViewAdapter> getCostProperties() {
		if (this.costProperties == null)
			this.costProperties = fetchProperties(CAT_COST);		
		return this.costProperties;
	}	

	public List<PropertyViewAdapter> getHostingProperties() {
		if (this.hostingProperties == null)
			this.hostingProperties = fetchProperties(CAT_HOSTING);		
		return this.hostingProperties;
	}	
	
    List<PropertyViewAdapter> processesProperties = null;
	public List<PropertyViewAdapter> getProcessesProperties() {
		if (this.processesProperties == null)
			this.processesProperties = fetchProperties(CAT_PROCESSES);		
		return this.processesProperties;
	}
	
	private List<PropertyViewAdapter> fetchProperties(String catName) {
		List<PropertyViewAdapter> result = new ArrayList<PropertyViewAdapter>();
		SDODataAccessClient service = new SDODataAccessClient();
		Long classId = this.beanFinder.findClassEditBean().getClazzId();
		
	    DataGraph[] results = null;
	    if (classId != null)
	    	results = service.find(PropertyViewQuery.createQueryByCatName(catName, classId));
	    else
	        results = service.find(PropertyViewQuery.createQueryByCatName(catName));
	    for (int i = 0; i < results.length; i++) {
	    	PropertyView prop = (PropertyView)results[i].getRootObject();
	    	result.add(new PropertyViewAdapter(prop));
	    	((PlasmaDataGraph)results[i]).removeRootObject();
	    }
	    return result;
	}
   
	public long getNameMaxLength() {
		PlasmaProperty nameProp = 
			(PlasmaProperty)this.clazz.getClassifier().getType().getProperty(
					Classifier.PTY_NAME);
		return nameProp.getMaxLength();
	}
	
	public long getDefinitionMaxLength() {
		PlasmaProperty nameProp = 
			(PlasmaProperty)this.clazz.getClassifier().getType().getProperty(
					Classifier.PTY_DEFINITION);
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

    	long max = this.getDefinitionMaxLength();
    	if (text.length() > max) {
            String msg = label + " is longer than allowed maximum "
                + String.valueOf(max) + " characters";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}  	
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
	
	public Long getClazzId() {
		return this.clazzId;
	}

	public void setClazzId(Long selected) {
		this.clazzId = selected;
	}
	
    public boolean getHasClazz() {
		return this.clazz != null;
	}
    
    public Clazz getClazz() {
		return this.clazz;
	}

	public void setClazz(Clazz selectedClazz) {
		this.clazz = selectedClazz;
	}
	
    public DataObject getClazzDataObject() {
		return this.clazz;
	}
	
    public List<TaxonomyAdapter> getTaxonomies() {
    	List<TaxonomyAdapter> result = new ArrayList<TaxonomyAdapter>();
    	ReferenceDataCache cache = this.beanFinder.findReferenceDataCache();
    	
    	CategorizationCollector visitor = 
    		new ClassCategorizationCollector(
    				PlasmaTypeHelper.INSTANCE.getType(ClassCategorization.class),
    				this.beanFinder.findReferenceDataCache());
    	visitor.setCollectOnlyInitializedTaxonomies(true);
    	// make  sure specific taxonomies are loaded we intend to use
    	// for views rendering this model 
    	visitor.initializeTaxonomy(cache.getInventoryPerspectiveModel());
    	visitor.initializeTaxonomy(cache.getBusinessReferenceModel());
    	visitor.initializeTaxonomy(cache.getServiceReferenceModel());
    	visitor.initializeTaxonomy(cache.getTechnicalReferenceModel());   	
     	visitor.initializeTaxonomy(cache.getSegmentArchitectureModel());   	
    	
     	((PlasmaDataObject)this.clazz).accept(visitor);
    	Map<Taxonomy, List<CategorizationAdapter>> taxonomyMap = visitor.getResult();
    	
    	Iterator<Taxonomy> iter = taxonomyMap.keySet().iterator();
    	while (iter.hasNext()) {
    		Taxonomy tax = iter.next();
    		List<CategorizationAdapter> pcats = taxonomyMap.get(tax);
    		result.add(new TaxonomyAdapter(tax, pcats));
    	}
    	    
    	return result;
    }
	
    private Package getPackage() {
    	if (this.clazz != null && this.clazz.getClassifier() != null && this.clazz.getClassifier().getPackageableType() != null &&
    			this.clazz.getClassifier().getPackageableType().get_package() != null)
    		return this.clazz.getClassifier().getPackageableType().get_package();
    	else
    		return null;
    }
    
    public Long getParentPackageId() {
    	Package pkg = getPackage();
    	if (pkg != null)
    		return pkg.getSeqId();
    	else
    		return new Long(-1);    		
    }
    
    public void setParentPackageId(Long id) {
    	Package oldPkg = getPackage();
    	if (id != null && oldPkg != null &&	
    			oldPkg.getSeqId() == id)
        	return; // no change - thanks anyway JSF
    	if (oldPkg != null) {
    		boolean found = false;
    		Package pkg = beanFinder.findReferenceDataCache().getPackage(id);
		    if (pkg != null) {
		    	found = true;
		    	Package copy = (Package)PlasmaCopyHelper.INSTANCE.copyShallow(pkg);
		    	this.clazz.getClassifier().getPackageableType().set_package(copy);
		    }
		    if (!found)
		    	log.error("could not find Clazz id: " + id);
    	}
    }
    
    public void validateParentPackageId(FacesContext facesContext,
            UIComponent component, Object value) {

    	if (value == null || 
    		((Long)value).intValue() == -1) 
    	{
    		String label = "Parent Catalog";
    		try {
    		    label = ResourceManager.instance().getString("aplsPackageEdit_parentPackage_label");
            }
            catch (MissingResourceException e) {
            }
            String msg = label + " is a required field";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}
	}
    
	public List<SelectItem> getParentPackageItems() {
		List<SelectItem> packageItems = new ArrayList<SelectItem>();
		SelectItem item = new SelectItem(new Long(-1), 
    			WebConstants.DEFAULT_SELECTION);
		packageItems.add(item);
		for (Package type : beanFinder.findReferenceDataCache().getPackages()) {    			
			String def = type.getDefinition();
			if (def != null && def.length() > 26)
				def = def.substring(0, 23) + "...";
			packageItems.add(new SelectItem(
					type.getSeqId(), 
					type.getName(),
					def));
    	}
    	return packageItems;
	}}
