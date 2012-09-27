package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.model.search.SearchBean;
import org.cloudgraph.web.model.taxonomy.TaxonomyConstants;
import org.cloudgraph.web.util.BeanFinder;

import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.PropertyCategorization;
import org.cloudgraph.web.sdo.meta.Classifier;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.sdo.meta.Slot;
import org.cloudgraph.web.sdo.meta.ValueSpecification;

/**
 */
public class InstanceSpecificationAdapter implements Serializable {
	private static Log log = LogFactory.getLog(InstanceSpecificationAdapter.class);
	private static final long serialVersionUID = 1L;
	
	protected InstanceSpecification ins;
	protected List<PropertyAdapter> properties;
	protected ReferenceDataCache cache; 

    @SuppressWarnings("unused")
	private InstanceSpecificationAdapter() {}
    
	public InstanceSpecificationAdapter(InstanceSpecification ins,
			List<PropertyAdapter> properties) {
		super();
		this.ins = ins;
		this.properties = properties;
    	this.cache = (new BeanFinder()).findReferenceDataCache();
    	cache.getInventoryPerspectiveModel(); // cache/load this
	}
	
	public InstanceSpecification getInstanceSpecification() {
		return this.ins;
	}
	
	public Long getId() {
		return ins.getSeqId();
	}
	
	public String getClassName() {
		if (ins.getClazz() != null) {
			Clazz clzz = ins.getClazz();
			if (clzz.getClassifier()!= null)
			{
				Classifier clss = clzz.getClassifier();
			    return clss.getName();
			}	
	    }
		return "";
	}
	
	private String caption = null;
    public String getCaption() {
    	if (caption != null)
    		return caption;
    	return this.createCaption(this.ins, this.properties);
    }
	
	protected String createCaption(InstanceSpecification inst, 
			List<PropertyAdapter> properties) {
    	
    	StringBuilder buf = new StringBuilder();
    	
    	for (PropertyAdapter adapter : properties) {
    		Property prop = adapter.getProperty();
			Object value = getValue(inst, prop);
			if (value != null) {
				buf.append(value);
				buf.append(" ");
				buf.append("\t");
			}
    	}
    	if (buf.toString().trim().length() > 0)
    		caption = buf.toString().trim();
    	else
    		caption = String.valueOf(inst.getSeqId());
    	
    	return caption;
    }  
	
	protected String createCaption2(InstanceSpecification instSpec, 
			List<Property> properties) {
    	
    	StringBuilder buf = new StringBuilder();
    	
    	for (Property prop : properties) {
			Object value = getValue(instSpec, prop);
			if (value != null) {
				buf.append(value);
				buf.append(" ");
				buf.append("\t");
			}
    	}
    	if (buf.toString().trim().length() > 0)
    		caption = buf.toString().trim();
    	else
    		caption = String.valueOf(instSpec.getSeqId());
    	
    	return caption;
    }  
	
	private Object getValue(InstanceSpecification instSpec, Property prop) {
		if (prop.getPropertyCategorizationCount() > 0)
    		for (PropertyCategorization pc : prop.getPropertyCategorization()) {
    			Category cat = pc.getCategorization().getCategory();
    			
    			Category cached = this.cache.getCategory(cat.getSeqId());;
    			if (isIdentifierCat(cached)) {
    				Slot slot = getSlot(instSpec, prop.getSeqId());
    				if (slot != null) {
    					return getValue(instSpec, slot);
    				}
    			}
    		}
		return null;
	}
    
    private boolean isIdentifierCat(Category current)
    {   	
    	if (TaxonomyConstants.SYS_TAXONOMY_CAT_IDENT != current.getId())
    		return false; // FIXME; somehow
    	while (current != null) {
			if (ReferenceDataCache.TAXONOMY_NAME_INVPM.equals(current.getName()))
			    return true;
			current = current.getParent();
		}
    	return false;
    }
    
    protected Slot getSlot(InstanceSpecification instSpec, 
    		Long propertySeqId) {
       if (instSpec.getSlot() != null)	
	       for (Slot slot : instSpec.getSlot()) {
	    	   if (slot.getDefiningFeature().getSeqId() == propertySeqId) {
	    		   return slot;
	    	   }
	       }
       return null;
    }
    
    protected Object getValue(InstanceSpecification owner, Slot slot) {   	
        for (ValueSpecification vs : slot.getValue()) {
        	if (vs.getInstanceValueCount() > 0) {
        		if (vs.getInstanceValue(0).getInstance() != null) {
        			InstanceSpecification is = vs.getInstanceValue(0).getInstance();
        			if (is.getSeqId() == owner.getSeqId()) {
        				log.warn("instance linked to same instance ("
        						+ is.getSeqId() + ")");
        				return ""; // recursion
        			}
        			List<Property> props = this.cache.getProperties(is.getClazz().getSeqId());
        			
        			//log.info(is.dump());
        			return createCaption2(is, props);
        		}
        		else
        			return "";
        	}
        	if (vs.getLiteralStringCount() > 0)
        		return vs.getLiteralString(0).getValue();
        	if (vs.getLiteralClobCount() > 0)
        		return vs.getLiteralClob(0).getValue();
        	if (vs.getLiteralShortCount() > 0)
        		return vs.getLiteralShort(0).getValue();
        	if (vs.getLiteralIntegerCount() > 0)
        		return vs.getLiteralInteger(0).getValue();
        	if (vs.getLiteralLongCount() > 0)
        		return vs.getLiteralLong(0).getValue();
        	if (vs.getLiteralFloatCount() > 0)
        		return vs.getLiteralFloat(0).getValue();
        	if (vs.getLiteralDoubleCount() > 0)
        		return vs.getLiteralDouble(0).getValue();
        	if (vs.getLiteralBooleanCount() > 0)
        		return vs.getLiteralBoolean(0).getValue();
        	if (vs.getLiteralDateCount() > 0)
        		return vs.getLiteralDate(0).getValue();
        }
        return "";
    }
}
