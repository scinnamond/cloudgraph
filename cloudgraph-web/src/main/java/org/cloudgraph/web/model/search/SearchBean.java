package org.cloudgraph.web.model.search;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.PropertyDatatype;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.model.tree.TreeNodeModel;
import org.cloudgraph.web.sdo.core.Organization;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.Package;
import org.primefaces.event.NodeSelectEvent;


@ManagedBean(name="SearchBean")
@SessionScoped
public class SearchBean extends ModelBean implements Search {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(SearchBean.class);
	
	private List<SearchParameter> parameterList = 
		new ArrayList<SearchParameter>();
	private List<SearchParameter> activeParamList = new ArrayList<SearchParameter>();
	
	
	public SearchBean() {
		SearchParameterListener orgChangeListener = new SearchParameterListener() {
			public void valueRemoved() {
			}
			public void valueSet() {
			}
		};
		parameterList.add( 
			new OrganizationSearchParameter(ComponentName.PARAMETER___DEPUTY___AREA, 
					PropertyDatatype.REFERENCE, 
				new Long(-1),
				this, orgChangeListener));
		parameterList.add( 
			new OrganizationSearchParameter(ComponentName.PARAMETER___BUSINESS___UNIT, 
				PropertyDatatype.REFERENCE, 
				new Long(-1),
				this, orgChangeListener));
		SearchParameterListener classChangeListener = new SearchParameterListener() {
			public void valueRemoved() {
			}
			public void valueSet() {
			}
		};
		parameterList.add( 
				new ClassSearchParameter(ComponentName.PARAMETER___CLASS, 
					PropertyDatatype.REFERENCE, 
					new Long(-1),
					this, classChangeListener));

		SearchParameterListener packageChangeListener = new SearchParameterListener() {
			public void valueRemoved() {
			}
			public void valueSet() {
			}
		};
		parameterList.add( 
				new PackageSearchParameter(ComponentName.PARAMETER___PACKAGE, 
					PropertyDatatype.REFERENCE, 
					new Long(-1),
					this, packageChangeListener));
		
		init();
	}
	
	private Long getDefaultPackageId() {
		
		Package defaultPackage = this.beanFinder.findReferenceDataCache().getPackage(
				SearchConstants.DEFAULT_PACKAGE_UUID);
		if (defaultPackage != null) {
		    return defaultPackage.getSeqId();
		}
		else
			return new Long(-1);
	}

	private Long getDefaultClassId() {
		
		Clazz defaultClass = this.beanFinder.findReferenceDataCache().getClazz(
				SearchConstants.DEFAULT_CLASS_UUID);
		if (defaultClass != null) {
		    return defaultClass.getSeqId();
		}
		else
			return new Long(-1);
	}
	
	public String search() {
		// noop
		return "results";
	}
	
	private void init() {
		this.setDeputyArea(new Long(-1));
		this.setBusinessUnit(new Long(-1));
		this.setClazzId(getDefaultClassId());
		this.setPackageId(getDefaultPackageId());
	}
	
	public String clear() {
		init();
		this.activeParamList.clear();
		return "results";
	}
	
	public List<SearchParameter> getActiveParameters() {
		if (activeParamList.size() == 0) {
		
			int i = 1;
	        for (SearchParameter param : parameterList) {
		        if (param.getHasValue()) {
		        	param.setIndex(i);
		            activeParamList.add(param);
		            i++;
		        }
	        }
		}		
	    return activeParamList;
	}
	
	public void reloadActiveParameters() {
		activeParamList.clear();
	}
	
	public int getActiveParameterCount() {
		return getActiveParameters().size();
	}	
	
	private SearchParameter getSearchParameter(ComponentName name) {
	    for (SearchParameter param : parameterList)
		    if (param.getComponentName().ordinal() == name.ordinal())
		    	return param;
		    
		return null;
	}
	
	private Object getSearchParameterValue(SearchParameter param) {
		return param.getValue();
		// if we are going to store search params as
		// part of a profile, then the value member
		// should go away from SearchParameter
        /*
		UserBean user = this.beanFinder.findUserBean();
		Setting setting = user.findComponentSetting(param.getComponentName(),  
				PropertyName.VALUE);
		if (setting != null) {
			String value = setting.getValue();
			if (value != null) {
			    Object objectValue = param.fromString(value);
			    param.setValue(objectValue);
			    return objectValue;
			}
			else
				return null;
		}
		else {
			return param.getValue();
		}
		*/
	}
	
	private void setSearchParameterValue(SearchParameter param, Object value) {
		param.setValue(value);
		
		// if we are going to store search params as
		// part of a profile, then the value member
		// should go away from SearchParameter
		/*
		// update user profile setting for this param
        try {		
			UserBean user = this.beanFinder.findUserBean();
	        user.updateProfileSetting(param.getComponentName(), ElementType.PARAMETER, 
	        	PropertyName.VALUE, 
	        	String.valueOf(value));	
	        user.commitProfile();
        }
        catch (Throwable t) {
	        log.error(t.getMessage(), t);
        }
        */		
	}

	public Long getDeputyArea() {
		SearchParameter param = this.getSearchParameter(ComponentName.PARAMETER___DEPUTY___AREA);
		return (Long)this.getSearchParameterValue(param);
	}

	public void setDeputyArea(Long deputyArea) {
		Long oldDeputyArea = getDeputyArea(); 
		SearchParameter param = this.getSearchParameter(ComponentName.PARAMETER___DEPUTY___AREA);
		this.setSearchParameterValue(param, deputyArea);
		if (isChanged(oldDeputyArea, deputyArea)) {
			setBusinessUnit(new Long(-1));
			this.activeParamList.clear();
		}
	}	

	public Long getBusinessUnit() {
		SearchParameter param = this.getSearchParameter(ComponentName.PARAMETER___BUSINESS___UNIT);
		return (Long)this.getSearchParameterValue(param);
	}

	public void setBusinessUnit(Long businessUnit) {
		Long oldBusinessUnit = getBusinessUnit(); 
		SearchParameter param = this.getSearchParameter(ComponentName.PARAMETER___BUSINESS___UNIT);
		this.setSearchParameterValue(param, businessUnit);
		if (isChanged(oldBusinessUnit, businessUnit)) {
			this.activeParamList.clear();
		}
	}
	
    public List<SelectItem> getBusinessUnitItems() {
    	ReferenceDataCache cache = this.beanFinder.findReferenceDataCache();
    	List<SelectItem> rawUnitList = cache.getBusinessUnitItems();	
        if (this.getDeputyArea() == null || this.getDeputyArea().longValue() == -1)
            return rawUnitList;
        
        List<SelectItem> result = new ArrayList<SelectItem>();
        for (SelectItem unitItem : rawUnitList) {
        	Organization unit = cache.getOrganization((Long)unitItem.getValue());
        	if (unit.getParent().getSeqId() == this.getDeputyArea().longValue())
        		result.add(unitItem);
        }
        return result;        
    }

	public Long getPackageId() {
		SearchParameter param = this.getSearchParameter(ComponentName.PARAMETER___PACKAGE);
		return (Long)this.getSearchParameterValue(param);
	}

	public void setPackageId(Long packageId) {
		Long old = getPackageId(); 
		SearchParameter param = this.getSearchParameter(ComponentName.PARAMETER___PACKAGE);
		this.setSearchParameterValue(param, packageId);
		if (isChanged(old, packageId)) {
			this.activeParamList.clear();
		}
	}
	
	public List<Package> getPackages() {
		Package root = this.beanFinder.findReferenceDataCache().getPackage(
				SearchConstants.ROOT_PACKAGE_UUID);
		List<Package> result = this.beanFinder.findReferenceDataCache().getPackages();
		result.remove(root);
		return result;
	}
	
    public List<Clazz> getClasses() {
    	Long pkgId = getPackageId();
    	if (pkgId != null && pkgId.longValue() != -1)
        	return this.beanFinder.findReferenceDataCache().getClassesByPackageId(pkgId);
    	else
    	    return this.beanFinder.findReferenceDataCache().getClasses();
    }
    
	public Long getClazzId() {
		SearchParameter param = this.getSearchParameter(ComponentName.PARAMETER___CLASS);
		return (Long)this.getSearchParameterValue(param);
	}

	public void setClazzId(Long classId) {
		Long old = getClazzId(); 
		SearchParameter param = this.getSearchParameter(ComponentName.PARAMETER___CLASS);
		this.setSearchParameterValue(param, classId);
		if (isChanged(old, classId)) {
			this.activeParamList.clear();
		}
	}
	
	public String getClazzName() {
		SearchParameter param = this.getSearchParameter(ComponentName.PARAMETER___CLASS);
		Long id = (Long)this.getSearchParameterValue(param);
		if (id != null) {
		    Clazz clzz = this.beanFinder.findReferenceDataCache().getClazz(id);
		    if (clzz != null && clzz.getClassifier() != null)
		        return clzz.getClassifier().getName();
		    else
		    	return "";
		} 
		else
	    	return "";
	}
    
	public void orgSelectListener(NodeSelectEvent event) {
    	try {
    		Organization selected = (Organization)event.getTreeNode().getData();
	        
	        log.info("orgSelectListener: " + selected.getName());
	        
	    	ReferenceDataCache cache = this.beanFinder.findReferenceDataCache();
	    	
	    	boolean unitFound = false;
	    	for (Organization unit : cache.getBusinessUnits()) {
	    		if (selected.getSeqId() == unit.getSeqId()) {
	    			unitFound = true;
	    			break;
	    		}
	    	}
	    	if (unitFound) {
	    		this.setBusinessUnit(selected.getSeqId());
	    		Organization childUnit = cache.getOrganization(this.getBusinessUnit());
	    		this.setDeputyArea(childUnit.getParent().getSeqId());
	    	}
	    	else {
		    	boolean areaFound = false;
		    	for (Organization area : cache.getDeputyAreas()) {
		    		if (selected.getSeqId() == area.getSeqId()) {
		    			areaFound = true;
		    			break;
		    		}
		    	}
		    	if (areaFound) {
		    		this.setDeputyArea(selected.getSeqId());
		    		this.setBusinessUnit(new Long(-1));
		    	}
		    	else
		    		throw new IllegalArgumentException("expected org as deputy area or business unit");	    		
	    	}
	        
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    }	
	
	private boolean isChanged(Long oldValue, Long newVale) {
		if (oldValue != null && oldValue != -1 && newVale != null && newVale != -1 && 
			oldValue.longValue() != newVale.longValue()) {
			return true;
		}
	    else if (((oldValue == null || oldValue == -1) && (newVale != null && newVale != -1)) ||
	    		((oldValue != null && oldValue != -1) && (newVale == null || newVale == -1))) {
		    return true;
	    }
		return false;
    }
	
	private boolean isChanged(Integer oldValue, Integer newVale) {
		if (oldValue != null && oldValue != -1 && newVale != null && newVale != -1 && 
			oldValue.intValue() != newVale.intValue()) {
			return true;
		}
	    else if (((oldValue == null || oldValue == -1) && (newVale != null && newVale != -1)) ||
	    		((oldValue != null && oldValue != -1) && (newVale == null || newVale == -1))) {
		    return true;
	    }
		return false;
    }
	
	private boolean isChanged(String oldValue, String newVale) {
		if (oldValue != null && newVale != null && 
			!oldValue.equals(newVale)) {
			return true;
		}
	    else if ((oldValue == null && newVale != null) ||
	    		(oldValue != null && newVale == null)) {
		    return true;
	    }
		return false;
    }
}
