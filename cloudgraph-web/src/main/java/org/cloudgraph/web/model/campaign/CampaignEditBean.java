package org.cloudgraph.web.model.campaign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
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
import org.cloudgraph.web.model.common.CategorizedPropertySupport;
import org.cloudgraph.web.model.common.PropertySelector;
import org.cloudgraph.web.model.configuration.PropertyItem;
import org.cloudgraph.web.model.configuration.PropertyType;
import org.cloudgraph.web.model.search.SearchBean;
import org.cloudgraph.web.query.CampaignQuery;
import org.cloudgraph.web.sdo.adapter.PropertyAdapter;
import org.cloudgraph.web.sdo.campaign.Campaign;
import org.cloudgraph.web.sdo.campaign.CampaignType;
import org.cloudgraph.web.sdo.campaign.DispersalType;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.util.BeanFinder;
import org.cloudgraph.web.util.ResourceUtils;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;

@ManagedBean(name="CampaignEditBean")
@SessionScoped
public class CampaignEditBean extends ModelBean 
    implements PropertySelector
{
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(CampaignEditBean.class);
	
	private Long campaignId;
	private Campaign campaign;
    private CategorizedPropertySupport propertySupport;
	
	public CampaignEditBean() {
		log.debug("created CampaignEditBean");
    	this.propertySupport = new CategorizedPropertySupport(this);
	}

	public String getTitle() {
		if (this.campaign != null)
			return "Edit Campaign: " + this.campaign.getName();
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
	    	DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
	        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
	    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Campaign.class);
	    	this.campaign = (Campaign)dataGraph.createRootObject(rootType);
	    	this.campaign.setName("New Campaign");
	    	this.campaign.setExternalId(UUID.randomUUID().toString());	        
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
		    DataGraph[] result = service.find(CampaignQuery.createEditQuery(this.campaignId));
	        this.campaign = (Campaign)result[0].getRootObject();	
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
                log.debug(((PlasmaDataObject)this.campaign).dump());
		    SDODataAccessClient service = new SDODataAccessClient();
	        service.commit(this.campaign.getDataGraph(), 
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
    		this.campaign.getDataGraph().getChangeSummary().endLogging(); // wipe any changes 
    		this.campaign.getDataGraph().getChangeSummary().beginLogging();
    		this.campaign = null;   	
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
       
	public long getNameMaxLength() {
		PlasmaProperty nameProp = 
			(PlasmaProperty)this.campaign.getType().getProperty(
					Campaign.PROPERTY.name.name());
		return nameProp.getMaxLength();
	}
	
	public long getDescriptionMaxLength() {
		PlasmaProperty nameProp = 
			(PlasmaProperty)this.campaign.getType().getProperty(
					Campaign.PROPERTY.description.name());
		return nameProp.getMaxLength();
	}
	
    public void validateDescriptionLength(FacesContext facesContext,
            UIComponent component, Object value) {
		String label = "Definition";
    	String text = null;
    	if (value == null || ((String)value).trim().length() == 0) {
    		return;
    	}
    	else
    		text = ((String)value).trim();

    	long max = this.getDescriptionMaxLength();
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
	
	public Long getCampaignId() {
		return this.campaignId;
	}

	public void setCampaignId(Long selected) {
		this.campaignId = selected;
	}
	
    public boolean getHasCampaign() {
		return this.campaign != null;
	}
    
    public Campaign getCampaign() {
		return this.campaign;
	}

	public void setCampaign(Campaign selectedCampaign) {
		this.campaign = selectedCampaign;
	}
	
    public DataObject getCampaignDataObject() {
		return this.campaign;
	}    

	List<SelectItem> typeItems;
    public List<SelectItem> getTypeItems() {
    	if (typeItems == null) {
    		typeItems = new ArrayList<SelectItem>();    		
	    	SelectItem item = new SelectItem(WebConstants.DEFAULT_SELECTION, 
	    			WebConstants.DEFAULT_SELECTION);
	    	typeItems.add(item);
	    	CampaignType[] types = CampaignType.values();    		
    		for (int i = 0; i < types.length; i++) {
    			String key = ResourceUtils.constructResourceLabelKey(PropertyType.class, types[i].name());
    			String displayName = null;
    			try {
    			    displayName = ResourceManager.instance().getString(key);
    		    }
    			catch (MissingResourceException e) {
    				displayName = types[i].name();
    			}
    			typeItems.add(new SelectItem(types[i].getInstanceName(),
    					displayName));
    		}    		
   	    }
    	
    	return typeItems;
    }
    
	List<SelectItem> dispersalMethodItems;
    public List<SelectItem> getDispersalMethodItems() {
    	if (dispersalMethodItems == null) {
    		dispersalMethodItems = new ArrayList<SelectItem>();    		
	    	SelectItem item = new SelectItem(WebConstants.DEFAULT_SELECTION, 
	    			WebConstants.DEFAULT_SELECTION);
	    	dispersalMethodItems.add(item);
	    	DispersalType[] dispersalTypes = DispersalType.values();    		
    		for (int i = 0; i < dispersalTypes.length; i++) {
    			String key = ResourceUtils.constructResourceLabelKey(PropertyType.class, dispersalTypes[i].name());
    			String displayName = null;
    			try {
    			    displayName = ResourceManager.instance().getString(key);
    		    }
    			catch (MissingResourceException e) {
    				displayName = dispersalTypes[i].name();
    			}
    			dispersalMethodItems.add(new SelectItem(dispersalTypes[i].getInstanceName(),
    					displayName));
    		}    		
   	    }
    	
    	return dispersalMethodItems;
    }
    
	public long getNotesMaxLength() {
		PlasmaProperty prop = 
			(PlasmaProperty)this.campaign.getType().getProperty(
					Campaign.PROPERTY.notes.name());
		return prop.getMaxLength();
	}
	
    public void validateNotesLength(FacesContext facesContext,
            UIComponent component, Object value) {
		String label = "Notes";
    	String text = null;
    	if (value == null || ((String)value).trim().length() == 0) {
    		return;
    	}
    	else
    		text = ((String)value).trim();

    	long max = this.getNotesMaxLength();
    	if (text.length() > max) {
            String msg = label + " is longer than allowed maximum "
                + String.valueOf(max) + " characters";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}  	
	}	
    
	private Map<Long,List<PropertyItem>> availablePropertiesMap = new HashMap<Long,List<PropertyItem>>();
	public List<PropertyItem> getAvailableProperties() {		
    	SearchBean searchBean = this.beanFinder.findSearchBean();
    	List<PropertyItem> available = availablePropertiesMap.get(searchBean.getClazzId());
	    if (available == null) {
	    	available = new ArrayList<PropertyItem>();
	    	for (PropertyAdapter adapter : this.propertySupport.getAllProperties())
	    		available.add(new PropertyItem(adapter.getId(), 
	    	    		adapter.getName()));
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
	
	private Map<Long,List<PropertyItem>> selectedPropertiesMap = new HashMap<Long,List<PropertyItem>>();
	public List<PropertyItem> getSelectedProperties() {
    	SearchBean searchBean = this.beanFinder.findSearchBean();
    	List<PropertyItem> selected = selectedPropertiesMap.get(searchBean.getClazzId());
	    if (selected == null) {
	    	selected = new ArrayList<PropertyItem>();
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
