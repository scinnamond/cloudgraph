package org.cloudgraph.web.model.common;

import java.util.ArrayList;
import java.util.List;

import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.model.search.SearchBean;
import org.cloudgraph.web.model.taxonomy.TaxonomyConstants;
import org.cloudgraph.web.sdo.adapter.PropertyAdapter;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.PropertyCategorization;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.util.BeanFinder;

public class CategorizedPropertySupport {
	private static final long serialVersionUID = 5241051901785754599L;
	protected static final List<PropertyAdapter> EMPTY_PROP_LIST = new ArrayList<PropertyAdapter>();
	protected List<PropertyAdapter> allProperties = new ArrayList<PropertyAdapter>();
	protected ReferenceDataCache cache;
	protected BeanFinder beanFinder;
	protected PropertySelector selector;
    
    public CategorizedPropertySupport(PropertySelector selector) {
    	this.beanFinder = new BeanFinder();
    	this.cache = this.beanFinder.findReferenceDataCache();
    	this.selector = selector;
    }
    
    public CategorizedPropertySupport() {
    	this(null);
    }
    
    public void clear() {
    	this.allProperties.clear();
    }
    
    public List<PropertyAdapter> getProperties()
    {
    	List<PropertyAdapter> result = new ArrayList<PropertyAdapter>();
    	result.addAll(getIdentificationProperties());
    	result.addAll(getCostProperties());
    	result.addAll(getHostingProperties());
    	result.addAll(getComplianceProperties());
    	result.addAll(getProcessProperties());
    	result.addAll(getOtherProperties());
    	
    	return result;
    }

    public int getPropertiesCount() {
    	return getProperties().size();
    }
    
    public List<PropertyAdapter> getAllProperties()
    {
    	if (allProperties.size() == 0) {
			SearchBean search = this.beanFinder.findSearchBean();
	    	List<Property> cached = this.beanFinder.findReferenceDataCache().getProperties(
	    			search.getClazzId());
	    	if (cached != null) {
	    		for (Property prop : cached) {
	    			allProperties.add(new PropertyAdapter(prop));
	    		}
	    	}
    	}
    	return allProperties;
    }

    public int getAllPropertiesCount() {
    	return getAllProperties().size();
    }
    
    public List<PropertyAdapter> getIdentificationProperties()
    {
    	return getPropertiesBySystemCat(TaxonomyConstants.SYS_TAXONOMY_CAT_IDENT,
    			getAllProperties());
    }

    public int getIdentificationPropertiesCount() {
    	return getIdentificationProperties().size();
    } 

    public List<PropertyAdapter> getCostProperties()
    {
    	return getPropertiesBySystemCat(TaxonomyConstants.SYS_TAXONOMY_CAT_COST,
    			getAllProperties());
    }

    public int getCostPropertiesCount() {
    	return getCostProperties().size();
    } 

    public List<PropertyAdapter> getInvestmentProperties()
    {
    	return getPropertiesBySystemCat(TaxonomyConstants.SYS_TAXONOMY_CAT_INVST,
    			getAllProperties());
    }

    public int getInvestmentPropertiesCount() {
    	return getInvestmentProperties().size();
    } 
    
    public List<PropertyAdapter> getHostingProperties()
    {
    	return getPropertiesBySystemCat(TaxonomyConstants.SYS_TAXONOMY_CAT_HOSTING,
    			getAllProperties());
    }

    public int getHostingPropertiesCount() {
    	return getHostingProperties().size();
    } 

    public List<PropertyAdapter> getProcessProperties()
    {
    	return getPropertiesBySystemCat(TaxonomyConstants.SYS_TAXONOMY_CAT_PROCESS,
    			getAllProperties());
    }

    public int getProcessPropertiesCount() {
    	return getProcessProperties().size();
    } 
    
    public List<PropertyAdapter> getComplianceProperties()
    {
    	return getPropertiesBySystemCat(TaxonomyConstants.SYS_TAXONOMY_CAT_COMPL,
    			getAllProperties());
    }

    public int getCompliancePropertiesCount() {
    	return getComplianceProperties().size();
    } 
    
    public List<PropertyAdapter> getOtherProperties()
    {
    	return getPropertiesBySystemCat(TaxonomyConstants.SYS_TAXONOMY_CAT_OTHER,
    			getAllProperties());
    }

    public int getOtherPropertiesCount() {
    	return getOtherProperties().size();
    } 

    private List<PropertyAdapter> getPropertiesBySystemCat(int sysCatId,
    		List<PropertyAdapter> allProperties)
    {
    	List<PropertyAdapter> result = new ArrayList<PropertyAdapter>();
		for (PropertyAdapter adapter : allProperties) {
			Property prop = adapter.getProperty();
			if (selector != null && !selector.isSelected(prop))
				continue;
     		if (prop.getPropertyCategorizationCount() > 0)
	    		for (PropertyCategorization pc : prop.getPropertyCategorization()) {
	    			Category cat = pc.getCategorization().getCategory();
	    			
	    			Category cached = cache.getCategory(cat.getSeqId());;
	    			if (isSystemCat(cached, sysCatId)) {
    				    result.add(adapter);
	    			}
	    		}
		} 
		return result;
    }
 
    private boolean isSystemCat(Category current, int catId)
    {   	
    	if (current == null)
    		return false; // FIXME WTF?
    	if (catId != current.getId())
    		return false; // FIXME; somehow
    	while (current != null) {
			if (ReferenceDataCache.TAXONOMY_NAME_INVPM.equals(current.getName()))
			    return true;
			current = current.getParent();
		}
    	return false;
    }

}
