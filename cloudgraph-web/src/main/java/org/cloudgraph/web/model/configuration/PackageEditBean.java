package org.cloudgraph.web.model.configuration;

import java.util.ArrayList;
import java.util.List;
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
import org.cloudgraph.web.query.ClassQuery;
import org.cloudgraph.web.query.PackageQuery;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaCopyHelper;

import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.Package;
import org.cloudgraph.web.sdo.meta.PackageableType;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;

public class PackageEditBean extends ModelBean {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(PackageEditBean.class);
	
	private Long packageId;
	private Package package_;
	
	public PackageEditBean() {
		log.debug("created PackageEditBean");
	}

	public String getTitle() {
		if (this.package_ != null)
			return "Edit Package: " + this.package_.getName();
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
	        this.package_ = pkg.createChild();
	    	PackageableType packageableType = this.package_.createPackageableType();    	
	    	this.package_.setExternalId(UUID.randomUUID().toString());
	    	this.package_.setName("New Catalog");    	
	        
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
		    DataGraph[] result = service.find(PackageQuery.createEditQuery(this.packageId));
		    this.package_ = (Package)result[0].getRootObject();	
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
                log.debug(((PlasmaDataObject)this.package_).dump());
		    SDODataAccessClient service = new SDODataAccessClient();
	        service.commit(this.package_.getDataGraph(), 
		    	    beanFinder.findUserBean().getName());
	        beanFinder.findReferenceDataCache().expirePackages();
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
    		this.package_.getDataGraph().getChangeSummary().endLogging(); // wipe any changes 
    		this.package_.getDataGraph().getChangeSummary().beginLogging();
    		this.package_ = null;   	
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
   
	public int getNameMaxLength() {
		PlasmaProperty nameProp = 
			(PlasmaProperty)this.package_.getType().getProperty(
					Package.PTY_NAME);
		return nameProp.getMaxLength();
	}
	
	public int getDefinitionMaxLength() {
		PlasmaProperty nameProp = 
			(PlasmaProperty)this.package_.getType().getProperty(
					Package.PTY_DEFINITION);
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
    
	
	public Long getPackageId() {
		return this.packageId;
	}

	public void setPackageId(Long selected) {
		this.packageId = selected;
	}
	
    public boolean getHasPackage() {
		return this.package_ != null;
	}
    
    public Package getPackage() {
		return this.package_;
	}

	public void setPackage(Package selectedPackage) {
		this.package_ = selectedPackage;
	}
	
    public DataObject getPackageDataObject() {
		return this.package_;
	}
	
    public Long getParentPackageId() {
    	if (this.package_ != null && this.package_.getParent() != null)
    		return this.package_.getParent().getSeqId();
    	else
    		return new Long(-1);    		
    }
    
    public void setParentPackageId(Long id) {
    	if (id != null && this.package_ != null &&
    		this.package_.getParent() != null &&	
    		this.package_.getParent().getSeqId() == id)
        	return; // no change - thanks anyway JSF
    	if (this.package_ != null) {
    		boolean found = false;
    		Package pkg = beanFinder.findReferenceDataCache().getPackage(id);
		    if (pkg != null) {
		    	found = true;
		    	this.package_ = (Package)PlasmaCopyHelper.INSTANCE.copyShallow(pkg);
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
	}	    
}
