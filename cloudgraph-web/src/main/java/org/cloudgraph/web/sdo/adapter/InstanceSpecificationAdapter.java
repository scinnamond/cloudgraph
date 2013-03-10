package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.model.taxonomy.TaxonomyConstants;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.PropertyCategorization;
import org.cloudgraph.web.sdo.meta.Classifier;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.InstanceValue;
import org.cloudgraph.web.sdo.meta.LiteralBoolean;
import org.cloudgraph.web.sdo.meta.LiteralClob;
import org.cloudgraph.web.sdo.meta.LiteralDate;
import org.cloudgraph.web.sdo.meta.LiteralDouble;
import org.cloudgraph.web.sdo.meta.LiteralFloat;
import org.cloudgraph.web.sdo.meta.LiteralInteger;
import org.cloudgraph.web.sdo.meta.LiteralLong;
import org.cloudgraph.web.sdo.meta.LiteralShort;
import org.cloudgraph.web.sdo.meta.LiteralString;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.sdo.meta.Slot;
import org.cloudgraph.web.sdo.meta.ValueSpecification;
import org.cloudgraph.web.util.BeanFinder;

/**
 */
public class InstanceSpecificationAdapter implements Serializable, Comparable<InstanceSpecificationAdapter> {
	private static Log log = LogFactory.getLog(InstanceSpecificationAdapter.class);
	private static final List<Object> EMPTY_LIST = new ArrayList<Object>();
	private static final long serialVersionUID = 1L;
	private InstanceSpecificationAdapter source;
	protected int level;
	protected int maxLevel;	
	
	protected InstanceSpecification ins;
	protected Map<String, PropertyAdapter> properyMap;
	protected List<PropertyAdapter> propertyList;
	protected ReferenceDataCache cache; 
	protected ValueMap values = new ValueMap();

    @SuppressWarnings("unused")
	private InstanceSpecificationAdapter() {
    	this.cache = (new BeanFinder()).findReferenceDataCache();
    	cache.getInventoryPerspectiveModel(); // cache/load this taxonomy
    	cache.getOrderingModel(); // cache/load this taxonomy
    }
    
	public InstanceSpecificationAdapter(
			InstanceSpecificationAdapter source, 
			InstanceSpecification ins,
			List<PropertyAdapter> props, 
			int level, int maxLevel) {
		this();
		this.level = level;
		this.maxLevel = maxLevel;
		this.source = source;
		this.ins = ins;
		this.propertyList = props;
		this.properyMap = new HashMap<String, PropertyAdapter>();
		
    	for (PropertyAdapter property : props) {
    		String name = property.getProperty().getName();
			this.properyMap.put(name, property);
			
			Object value = getValue(this.ins, property.getProperty());
			value = getValue(this.ins, property.getProperty());
			if (value != null) {
			    this.values.put(name, value);
			}
    	}    	
	}
	
	public InstanceSpecificationAdapter(
			InstanceSpecification ins,
			List<PropertyAdapter> props,
			int level, int maxLevel) {
	    this(null, ins, props, level, maxLevel);	
	}
	
	@Override
	public int compareTo(InstanceSpecificationAdapter other) {
		
		Object thisOrderingValue = getPrimaryOrderingValue(this.ins);
		Object otherOrderingValue = getPrimaryOrderingValue(other.ins);
		if (thisOrderingValue != null && otherOrderingValue != null) {
			if (Comparable.class.isAssignableFrom(thisOrderingValue.getClass()))
			{
				Comparable thisValueComparable = (Comparable)thisOrderingValue;
				return thisValueComparable.compareTo(otherOrderingValue);
			}
		}
		// TODO Auto-generated method stub
		return 0;
	}
	
	public InstanceSpecificationAdapter getSource() {
		return source;
	}

	public InstanceSpecification getInstanceSpecification() {
		return this.ins;
	}
	
	public Long getId() {
		return ins.getSeqId();
	}
	
	public Map<String, Object> getValues() {
		return this.values;
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
	
    public String getCaption() {
    	return this.createCaption(this.ins, 
    			this.properyMap.values());
    }
    
    public String toString() {
    	return getCaption();
    }
	
	protected String createCaption(InstanceSpecification inst, 
			Collection<PropertyAdapter> properties) {
    	
    	StringBuilder buf = new StringBuilder();
    	
    	for (PropertyAdapter adapter : properties) {
    		Property prop = adapter.getProperty();
			Object value = getIdentifierValue(inst, prop);
			if (value != null) {
				buf.append(value);
				buf.append(" ");
				buf.append("\t");
			}
    	}
    	
    	String result = null;
    	if (buf.toString().trim().length() > 0)
    		result = buf.toString().trim();
    	else
    		result = String.valueOf(inst.getSeqId());
    	
    	return result;
    }  
	
	protected String createCaption2(InstanceSpecification instSpec, 
			List<Property> properties) {
    	
    	StringBuilder buf = new StringBuilder();
    	
    	for (Property prop : properties) {
			Object value = getIdentifierValue(instSpec, prop);
			if (value != null) {
				buf.append(value);
				buf.append(" ");
				buf.append("\t");
			}
    	}
    	String result = null;
    	if (buf.toString().trim().length() > 0)
    		result = buf.toString().trim();
    	else
    		result = String.valueOf(instSpec.getSeqId());
    	
    	return result;
    }  
	
	private Object getValue(InstanceSpecification instSpec, Property prop) {
		Slot slot = getSlot(instSpec, prop.getSeqId());
		if (slot != null) {
			return getValue(instSpec, slot);
		}
		else {
			if (prop.getLowerValue() == 1)
			    log.warn("no slot found for required property, "
			    	+ instSpec.getClazz().getClassifier().getName() + "."
			    	+ prop.getName());
		}
		return null;
	}
	
	private Object getIdentifierValue(InstanceSpecification instSpec, Property prop) {
		if (prop.getPropertyCategorizationCount() > 0)
    		for (PropertyCategorization pc : prop.getPropertyCategorization()) {
    			Category cat = pc.getCategorization().getCategory();
    			
    			Category cached = this.cache.getCategory(cat.getSeqId());
    			if (isIdentifierCat(cached)) {
    				Slot slot = getSlot(instSpec, prop.getSeqId());
    				if (slot != null) {
    					return getValue(instSpec, slot);
    				}
    			}
    		}
		return null;
	}
	
	private Object getPrimaryOrderingValue(InstanceSpecification instSpec) {
		for (PropertyAdapter adpater : this.properyMap.values()) {
			Object value = getPrimaryOrderingValue(instSpec, adpater.getProperty());
			if (value != null) {
				return value;
			}
		}
		return null;
	}
	
	private Object getPrimaryOrderingValue(InstanceSpecification instSpec, Property prop) {
		if (prop.getPropertyCategorizationCount() > 0)
    		for (PropertyCategorization pc : prop.getPropertyCategorization()) {
    			Category cat = pc.getCategorization().getCategory();
    			
    			Category cached = this.cache.getCategory(cat.getSeqId());
    			if (isPrimaryOrderingCat(cached)) {
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
    	if (current != null) {
	    	if (TaxonomyConstants.SYS_TAXONOMY_CAT_IDENT != current.getId())
	    		return false; // FIXME; somehow
	    	while (current != null) {
				if (ReferenceDataCache.TAXONOMY_NAME_INVPM.equals(current.getName()))
				    return true;
				current = current.getParent();
			}
    	}
    	return false;
    }
    
    private boolean isPrimaryOrderingCat(Category current)
    {   	
    	if (current != null) {
	    	if (TaxonomyConstants.SYS_TAXONOMY_ORDERING_CAT_PRIMARY != current.getId())
	    		return false; // FIXME; somehow
	    	while (current != null) {
				if (ReferenceDataCache.TAXONOMY_NAME_ORDERING.equals(current.getName()))
				    return true;
				current = current.getParent();
			}
    	}
    	return false;
    }
    
    protected Slot getSlot(InstanceSpecification instSpec, 
    		Long propertySeqId) {
       if (instSpec.getSlot() != null)	
	       for (Slot slot : instSpec.getSlot()) {
	    	   long featureId = slot.getDefiningFeature().getSeqId();
	    	   if (featureId == propertySeqId.longValue()) {
	    		   return slot;
	    	   }
	       }
       return null;
    }
    
    protected Object getValue(InstanceSpecification owner, Slot slot) { 
    	try {
	    	if ("*".equals(slot.getDefiningFeature().getUpperValue())) {
	    		return getMultiValue(owner, slot);
	    	}
	    	else {
	    		return getSingularValue(owner, slot);
	    	}
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t); 
    		return "";
    	}
    }
    
    protected Object getMultiValue(InstanceSpecification owner, Slot slot) {   	
        List<Object> result = new ArrayList<Object>();
        if (slot.getValueCount() > 0) {
        	if (slot.getValueCount() > 1)
    			log.warn("found multiple value specs for multi property '"
	    				+ slot.getDefiningFeature().getName() + "' - ignoring");
      	    ValueSpecification vs = slot.getValue(0);
        	return getMultiSpecificationValue(owner, slot, vs);
        }

        return result;
    }

    protected Object getSingularValue(InstanceSpecification owner, Slot slot) {   	
        if (slot.getValueCount() > 0) {
        	if (slot.getValueCount() > 1)
    			log.warn("found multiple value specs for singular property '"
	    				+ slot.getDefiningFeature().getName() + "' - ignoring");
      	    ValueSpecification vs = slot.getValue(0);
        	return getSingularSpecificationValue(owner, slot, vs);
        }
        return ""; //FIXME: why does JSF demand this??
    }
    
    //FIXME: use its datatype
    protected Object getSingularSpecificationValue(InstanceSpecification owner, 
    		Slot slot, ValueSpecification valueSpec) {   	
    	if (valueSpec.getInstanceValueCount() > 0) {
			if (this.level == this.maxLevel)
				return null; // no more levels				
			List<PropertyAdapter> props = null;
    		InstanceValue iv = valueSpec.getInstanceValue(0);
			if (iv.getInstance() != null) {
    			InstanceSpecification is = iv.getInstance();
    			InstanceSpecificationAdapter source = this.getSource(); 
    			if (source != null) {
    				if (source.getInstanceSpecification().equals(is))
        				return null; // skip its parent
    			}
    			if (props == null) { //FIXME: use its datatype
    				props = new ArrayList<PropertyAdapter>();
    				List<Property> list = this.cache.getProperties(is.getClazz().getSeqId());
    				if (list != null)
    					for (Property p : list)
    						props.add(new PropertyAdapter(p));
    			}
    			if (is.getSeqId() == owner.getSeqId()) {
    				log.warn("instance linked to same instance ("
    						+ is.getSeqId() + ")");
    				return null; // recursion
    			}
    			return new InstanceSpecificationAdapter(
    				this, is, props, this.level + 1, this.maxLevel);
    			//log.info(is.dump());
    		}
		}
    	else if (valueSpec.getLiteralStringCount() > 0)
    		return valueSpec.getLiteralString(0).getValue();
    	else if (valueSpec.getLiteralClobCount() > 0)
    		return valueSpec.getLiteralClob(0).getValue();
    	else if (valueSpec.getLiteralShortCount() > 0)
    		return valueSpec.getLiteralShort(0).getValue();
    	else if (valueSpec.getLiteralIntegerCount() > 0)
    		return valueSpec.getLiteralInteger(0).getValue();
    	else if (valueSpec.getLiteralLongCount() > 0)
    		return valueSpec.getLiteralLong(0).getValue();
    	else if (valueSpec.getLiteralFloatCount() > 0)
    		return valueSpec.getLiteralFloat(0).getValue();
    	else if (valueSpec.getLiteralDoubleCount() > 0)
    		return valueSpec.getLiteralDouble(0).getValue();
    	else if (valueSpec.getLiteralBooleanCount() > 0)
    		return valueSpec.getLiteralBoolean(0).getValue();
    	else if (valueSpec.getLiteralDateCount() > 0)
    		return valueSpec.getLiteralDate(0).getValue();
        return null;
    }

    protected List<Comparable> getMultiSpecificationValue(InstanceSpecification owner, 
    		Slot slot, ValueSpecification valueSpec) { 
		List<PropertyAdapter> props = null;
    	List<Comparable> list = new ArrayList<Comparable>();
    	if (valueSpec.getInstanceValueCount() > 0) {
			if (this.level == this.maxLevel)
				return list; // no more levels				
    		for (InstanceValue iv : valueSpec.getInstanceValue()) {
				if (iv.getInstance() == null)
				    continue;
				InstanceSpecification is = iv.getInstance();
				if (this.getSource() != null && this.getSource().getInstanceSpecification().equals(is))
					continue; // skip its parent
				if (props == null) { //FIXME: use its datatype
					props = new ArrayList<PropertyAdapter>();
					List<Property> plist = this.cache.getProperties(is.getClazz().getSeqId());
					if (plist != null)
						for (Property p : plist)
							props.add(new PropertyAdapter(p));
				}
				if (is.getSeqId() == owner.getSeqId()) {
					log.warn("instance linked to same instance ("
							+ is.getSeqId() + ")");
					continue; // recursion
				}
				list.add(new InstanceSpecificationAdapter(
					this, is, props, this.level + 1, this.maxLevel));
	    			//log.info(is.dump());
    		} // for
		} // if
    	else if (valueSpec.getLiteralStringCount() > 0) {
	       	for (LiteralString lit : valueSpec.getLiteralString())
	       		list.add(lit.getValue());
    	}
    	else if (valueSpec.getLiteralClobCount() > 0) {
	       	for (LiteralClob lit : valueSpec.getLiteralClob())
	       		list.add(lit.getValue());
    	}
    	else if (valueSpec.getLiteralShortCount() > 0) {
	       	for (LiteralShort lit : valueSpec.getLiteralShort())
	       		list.add(lit.getValue());
    	}
    	else if (valueSpec.getLiteralIntegerCount() > 0) {
	       	for (LiteralInteger lit : valueSpec.getLiteralInteger())
	       		list.add(lit.getValue());
    	}
    	else if (valueSpec.getLiteralLongCount() > 0) {
	       	for (LiteralLong lit : valueSpec.getLiteralLong())
	       		list.add(lit.getValue());
    	}
    	else if (valueSpec.getLiteralFloatCount() > 0) {
	       	for (LiteralFloat lit : valueSpec.getLiteralFloat())
	       		list.add(lit.getValue());
    	}
    	else if (valueSpec.getLiteralDoubleCount() > 0) {
	       	for (LiteralDouble lit : valueSpec.getLiteralDouble())
	       		list.add(lit.getValue());
    	}
    	else if (valueSpec.getLiteralBooleanCount() > 0) {
	       	for (LiteralBoolean lit : valueSpec.getLiteralBoolean())
	       		list.add(lit.getValue());
    	}
    	else if (valueSpec.getLiteralDateCount() > 0) {
	       	for (LiteralDate lit : valueSpec.getLiteralDate())
	       		list.add(lit.getValue());
    	}
    	Collections.sort(list);
        return list;
    }
    
	class ValueMap implements Map<String, Object> {

		private Map<String, Object> theMap = new HashMap<String, Object>();

		@Override
		public int size() {
			return this.theMap.size();
		}

		@Override
		public boolean isEmpty() {
			return this.theMap.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return this.theMap.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return this.theMap.containsValue(value);
		}

		@Override
		public Object get(Object key) {
			Object result = null;
			PropertyAdapter property = properyMap.get(key);
			if (property == null) {
				log.warn("property '" 
			        + key + "' is not defined for entity, "
			        + getClassName());
				result = EMPTY_LIST;
			}
			else {
				result = this.theMap.get(key);
				if (result == null && property.getIsMany())
					result = EMPTY_LIST; // keeps JSTL forEach happy
			}
			return result;
		}

		@Override
		public Object put(String key, Object value) {
			return this.theMap.put(key, value);
		}

		@Override
		public Object remove(Object key) {
			return this.theMap.remove(key);
		}


		@Override
		public void clear() {
			this.theMap.clear();			
		}

		@Override
		public Set<String> keySet() {
			return this.theMap.keySet();
		}

		@Override
		public Collection<Object> values() {
			return this.theMap.values();
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return this.theMap.entrySet();
		}

		@Override
		public void putAll(Map<? extends String, ? extends Object> m) {
			this.theMap.putAll(m);
			
		}
	}
}
