package org.cloudgraph.web.model.data;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.ResourceManager;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.query.EnumerationQuery;
import org.cloudgraph.web.query.EnumerationViewQuery;
import org.cloudgraph.web.query.PackageQuery;
import org.cloudgraph.web.sdo.adapter.EnumerationViewAdapter;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import org.cloudgraph.web.sdo.core.EnumerationView;
import org.cloudgraph.web.sdo.meta.Classifier;
import org.cloudgraph.web.sdo.meta.DataType;
import org.cloudgraph.web.sdo.meta.Element;
import org.cloudgraph.web.sdo.meta.Enumeration;
import org.cloudgraph.web.sdo.meta.EnumerationLiteral;
import org.cloudgraph.web.sdo.meta.NamedElement;
import org.cloudgraph.web.sdo.meta.Package;
import org.cloudgraph.web.sdo.meta.PackageableType;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

public class EnumerationEditBean extends ModelBean {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(EnumerationEditBean.class);
	
	private Long enumerationId;
	private Enumeration enumeration;
	private EnumerationLiteral literal;
	private String saveActionReRender;
	
	public EnumerationEditBean() {
		log.debug("created EnumerationEditBean");
	}

	public String getSaveActionReRender() {
		return saveActionReRender;
	}

	public void setSaveActionReRender(String saveActionReRender) {
		this.saveActionReRender = saveActionReRender;
	}

	public String getTitle() {
		if (this.enumeration != null)
			return "Edit Enumeration: " + this.enumeration.getDataType().getClassifier().getName();
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
	    	PackageableType enumType = pkg.createPackageableType();
	    	Classifier enumClassifier = enumType.createClassifier();
	    	enumClassifier.setName("New Value List");
	    	DataType enumDatatype = enumClassifier.createDataType();		    
	    	this.enumeration = enumDatatype.createEnumeration();
	        this.beanFinder.findReferenceDataCache().expireEnumerations();	        
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
		    DataGraph[] result = service.find(EnumerationQuery.createEditQuery(this.enumerationId));
	        this.enumeration = (Enumeration)result[0].getRootObject();	
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
                log.debug(((PlasmaDataObject)this.enumeration).dump());
		    SDODataAccessClient service = new SDODataAccessClient();
	        service.commit(this.enumeration.getDataGraph(), 
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
        
    public String exit() {
    	try {
    		this.enumeration.getDataGraph().getChangeSummary().endLogging(); // wipe any changes 
    		this.enumeration.getDataGraph().getChangeSummary().beginLogging();
    		this.enumeration = null;   	
        } catch (Throwable t) {
        	log.error(t.getMessage(), t);
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

    public void createLiteral() {
    	try {
            this.literal = this.enumeration.createOwnedLiteral(); 
        } catch (Throwable t) {
        	log.error(t.getMessage(), t);
        } finally {
        }       
    }
    
    public String cancelCreateEditLiteral() {
    	try {
            this.literal = null; 
        } catch (Throwable t) { 
            log.error(t.getMessage(), t); 
        } finally {
        } 
        return null;
    }
        
	public String deleteLiteral() {
        try {
        	this.literal.delete();
	        save();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        } finally {
        }       
        return null;
    }	
        
    
    public List<EnumerationViewAdapter> getEnumerations() {
		List<EnumerationViewAdapter> result = new ArrayList<EnumerationViewAdapter>();
		SDODataAccessClient service = new SDODataAccessClient();
	    DataGraph[] results = service.find(EnumerationViewQuery.createQuery());
	    for (int i = 0; i < results.length; i++) {
	    	EnumerationView view = (EnumerationView)results[i].getRootObject();
	    	result.add(new EnumerationViewAdapter(view));
	    	((PlasmaDataGraph)results[i]).removeRootObject();
	    }
	    return result;
    }    
   
	public long getNameMaxLength() {
		Type type = PlasmaTypeHelper.INSTANCE.getType(Classifier.class);
		PlasmaProperty nameProp = 
			(PlasmaProperty)type.getProperty(
					NamedElement.PROPERTY.name.name());
		return nameProp.getMaxLength();
	}
	
	public long getDefinitionMaxLength() {
		Type type = PlasmaTypeHelper.INSTANCE.getType(Classifier.class);
		PlasmaProperty nameProp = 
			(PlasmaProperty)type.getProperty(
					Element.PROPERTY.definition.name());
		return nameProp.getMaxLength();
	}
	
	public long getLiteralNameMaxLength() {
		
		Type type = PlasmaTypeHelper.INSTANCE.getType(EnumerationLiteral.class);
		PlasmaProperty nameProp = 
			(PlasmaProperty)type.getProperty(
					NamedElement.PROPERTY.name.name());
		return nameProp.getMaxLength();
	}
	
    public void validateNameLength(FacesContext facesContext,
            UIComponent component, Object value) {
		String label = "Name";
    	String text = null;
    	if (value == null || ((String)value).trim().length() == 0) {
    		return;
    	}
    	else
    		text = ((String)value).trim();

    	long max = this.getNameMaxLength();
    	if (text.length() > max) {
            String msg = label + " is longer than allowed maximum "
                + String.valueOf(max) + " characters";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}  	
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
    
    public void validateLiteralNameLength(FacesContext facesContext,
            UIComponent component, Object value) {
		String label = "Name";
    	String text = null;
    	if (value == null || ((String)value).trim().length() == 0) {
    		return;
    	}
    	else
    		text = ((String)value).trim();

    	long max = this.getLiteralNameMaxLength();
    	if (text.length() > max) {
            String msg = label + " is longer than allowed maximum "
                + String.valueOf(max) + " characters";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}  	
	}	

	public Long getEnumerationId() {
		return this.enumerationId;
	}

	public void setEnumerationId(Long selected) {
		this.enumerationId = selected;
	}
	
    public boolean getHasEnumeration() {
		return this.enumeration != null;
	}
    
    public Enumeration getEnumeration() {
		return this.enumeration;
	}

	public void setEnumeration(Enumeration selectedenumeration) {
		this.enumeration = selectedenumeration;
	}
	
	public String getName() {
		return this.enumeration.getDataType().getClassifier().getName();
	}
	
	public void setName(String name) {
		this.enumeration.getDataType().getClassifier().setName(name);
	}
	
	public String getDefinition() {
		return this.enumeration.getDataType().getClassifier().getDefinition();
	}
	
	public void setDefinition(String defn) {
		this.enumeration.getDataType().getClassifier().setDefinition(defn);
	}

	public EnumerationLiteral getLiteral() {
		return literal;
	}

	public void setLiteral(EnumerationLiteral literal) {
		this.literal = literal;
	}
}
