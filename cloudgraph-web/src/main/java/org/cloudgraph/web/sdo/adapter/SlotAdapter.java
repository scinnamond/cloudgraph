package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.model.data.InstanceEditBean;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.cloudgraph.web.query.PropertyQuery;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.DataConverter;
import org.plasma.sdo.helper.PlasmaCopyHelper;

import commonj.sdo.DataGraph;

import org.cloudgraph.web.sdo.meta.Classifier;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.DataType;
import org.cloudgraph.web.sdo.meta.Enumeration;
import org.cloudgraph.web.sdo.meta.EnumerationLiteral;
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
import org.cloudgraph.web.sdo.meta.PrimitiveType;
import org.cloudgraph.web.sdo.meta.PrimitiveTypeName;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.sdo.meta.Slot;
import org.cloudgraph.web.sdo.meta.ValueSpecification;


public class SlotAdapter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(SlotAdapter.class);
	private InstanceSpecification root;
	private Clazz rootClass;
	private Slot slot; // can be null if not created yet
	private Property property;
	private static Long defaultId = new Long(-1);
	
	@SuppressWarnings("unused")
	private SlotAdapter() {}
	
	public SlotAdapter(Slot slot, 
			Property property) {
		this.slot = slot;
		this.root = slot.getOwningInstance();
		this.property = property;
		if (this.slot == null)
			throw new IllegalArgumentException("expected arg, slot");
		if (this.property == null)
			throw new IllegalArgumentException("expected arg, property");
	}
	
	public SlotAdapter(InstanceSpecification root, 
			Property property) {
		this.root = root;
		this.property = property;
		if (this.root == null)
			throw new IllegalArgumentException("expected arg, root");
		if (this.property == null)
			throw new IllegalArgumentException("expected arg, property");
	}

	public Slot getSlot() {
		return this.slot;
	}

	public Property getProperty() {
		return property;
	}
	
	public String getPropertyName() {
		return this.property.getName();
	}
	
	public String getPropertyDefinition() {
		if (this.property.getDefinition() == null)
			return "";
		String result = this.property.getDefinition();
		if (result.startsWith("<p>"))
			result = result.substring(3);
		if (result.endsWith("</p>"))
		    result = result.substring(0, result.length() - 4);
		return result;
	}
	
	public boolean getIsPrimitiveType() {
		Classifier dataTypeClassifier = this.property.getDataType();
		if (dataTypeClassifier.getDataTypeCount() == 1) {
			DataType dataType = dataTypeClassifier.getDataType(0); 
			if (dataType.getPrimitiveTypeCount() == 1) {
				return true;
			}
			else if (dataType.getPrimitiveTypeCount() > 1)
				log.warn("expected zero or 1 primitive type");
		}		
		else if (dataTypeClassifier.getDataTypeCount() > 1)
			log.warn("expected zero or 1 datatype classifier");
	    return false; 
	}

	public PrimitiveType getPrimitiveType() {
		Classifier dataTypeClassifier = this.property.getDataType();
		if (dataTypeClassifier.getDataTypeCount() == 1) {
			DataType dataType = dataTypeClassifier.getDataType(0); 
			if (dataType.getPrimitiveTypeCount() == 1) {
				return dataType.getPrimitiveType(0);
			}
			else if (dataType.getPrimitiveTypeCount() > 1)
				log.warn("expected zero or 1 primitive type");
		}		
		else if (dataTypeClassifier.getDataTypeCount() > 1)
			log.warn("expected zero or 1 datatype classifier");
	    return null; 
	}
	
	public boolean getIsString() {
		PrimitiveType pt = getPrimitiveType();
		if (pt != null) {
			String name = pt.getDataType().getClassifier().getName();
			return PrimitiveTypeName.STRING.name().equalsIgnoreCase(name);
		}
		return false;
	}
	
	public boolean getIsLongString() {
		PrimitiveType pt = getPrimitiveType();
		if (pt != null) {
			String name = pt.getDataType().getClassifier().getName();
			return PrimitiveTypeName.CLOB.name().equalsIgnoreCase(name);
		}
		return false;
	}
	
	public boolean getIsDate() {
		PrimitiveType pt = getPrimitiveType();
		if (pt != null) {
			String name = pt.getDataType().getClassifier().getName();
			return PrimitiveTypeName.DATE.name().equalsIgnoreCase(name);
		}
		return false;
	}

	public boolean getIsFloatingPoint() {
		PrimitiveType pt = getPrimitiveType();
		if (pt != null) {
			String name = pt.getDataType().getClassifier().getName();
			return PrimitiveTypeName.FLOAT.name().equalsIgnoreCase(name) ||
			    PrimitiveTypeName.DOUBLE.name().equalsIgnoreCase(name);
		}
		return false;
	}

	public boolean getIsIntegral() {
		PrimitiveType pt = getPrimitiveType();
		if (pt != null) {
			String name = pt.getDataType().getClassifier().getName();
			return PrimitiveTypeName.SHORT.name().equalsIgnoreCase(name) ||
			    PrimitiveTypeName.INTEGER.name().equalsIgnoreCase(name) ||
			    PrimitiveTypeName.LONG.name().equalsIgnoreCase(name);
		}
		return false;
	}	
	
	public boolean getIsBoolean() {
		PrimitiveType pt = getPrimitiveType();
		if (pt != null) {
			String name = pt.getDataType().getClassifier().getName();
			return PrimitiveTypeName.BOOLEAN.name().equalsIgnoreCase(name);
		}
		return false;
	}
	
	private static List<SelectItem> booleanItems;
	public List<SelectItem> getBooleanItems() {
		if (booleanItems == null) {
			booleanItems = new ArrayList<SelectItem>();
			SelectItem trueItem = new SelectItem(new Boolean(true), 
					"yes");
			SelectItem falseItem = new SelectItem(new Boolean(false), 
			    "no");
			booleanItems.add(trueItem);
			booleanItems.add(falseItem);
		}
		return booleanItems;
	}
		   	
	public boolean getIsEnumerationType() {
		Classifier dataTypeClassifier = this.property.getDataType();
		if (dataTypeClassifier.getDataTypeCount() == 1) {
			DataType dataType = dataTypeClassifier.getDataType(0); 
			if (dataType.getEnumerationCount() == 1) {
				return true;
			}
			else if (dataType.getEnumerationCount() > 1)
				log.warn("expected zero or 1 enumeration type");
		}		
		else if (dataTypeClassifier.getDataTypeCount() > 1)
			log.warn("expected zero or 1 datatype classifier");
	    return false; 
	}

	public Enumeration getEnumerationType() {
		Classifier dataTypeClassifier = this.property.getDataType();
		if (dataTypeClassifier.getDataTypeCount() == 1) {
			DataType dataType = dataTypeClassifier.getDataType(0); 
			if (dataType.getEnumerationCount() == 1) {
				return dataType.getEnumeration(0);
			}
			else if (dataType.getEnumerationCount() > 1)
				log.warn("expected zero or 1 enumeration type");
		}		
		else if (dataTypeClassifier.getDataTypeCount() > 1)
			log.warn("expected zero or 1 datatype classifier");
	    return null; 
	}
	
	public List<SelectItem> getEnumerationLiteralItems() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		Enumeration enm = getEnumerationType();
		if (enm != null) {
			for (EnumerationLiteral el : enm.getOwnedLiteral()) {
				SelectItem item = new SelectItem(el.getName(),
						el.getName(), el.getDefinition());
				result.add(item);
			}
		}
		return result;
	}
	
	public boolean getIsClassType() {
		Classifier dataTypeClassifier = this.property.getDataType();
		if (dataTypeClassifier.getClazzCount() == 1) {
			return true;
		}		
		else if (dataTypeClassifier.getClazzCount() > 1)
			log.warn("expected zero or 1 classifier class extensions");
	    return false; 
	}

	public Clazz getClassType() {
		Classifier dataTypeClassifier = this.property.getDataType();
		if (dataTypeClassifier.getClazzCount() == 1) {
			return dataTypeClassifier.getClazz(0);
		}		
	    return null; 
	}
	
	private Map<Long, InstanceSpecificationAdapter> classInstanceMap;
	@SuppressWarnings("unchecked")
	public List<SelectItem> getClassTypeItems() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		Clazz clzz = getClassType();
		if (clzz != null) {
			SelectItem defaultItem = new SelectItem(defaultId, 
					WebConstants.DEFAULT_SELECTION);
			result.add(defaultItem);
			if (classInstanceMap == null)
				classInstanceMap = new HashMap<Long, InstanceSpecificationAdapter>();
			
	    	SDODataAccessClient service = new SDODataAccessClient();
	    	DataGraph[] results = service.find(
	    			PropertyQuery.createQueryBySourceClassId(clzz.getSeqId()));
	        List<PropertyAdapter> properties = new ArrayList<PropertyAdapter>();
	    	for (int i = 0; i < results.length; i++) {
	    		Property prop = (Property)results[i].getRootObject();
	    		properties.add(new PropertyAdapter(prop));
	        }
			
	    	results = service.find(InstanceSpecificationQuery.createQueueQueryByClassId(
					clzz.getSeqId())); 
	        
	    	SelectItem[] items = new SelectItem[results.length];
	    	for (int i = 0; i < results.length; i++) {
	        	InstanceSpecification inst = (InstanceSpecification)results[i].getRootObject();
	        	inst.setDataGraph(null);
	        	InstanceSpecificationAdapter adapter = 
	        		new InstanceSpecificationAdapter(inst, properties);
	        	classInstanceMap.put(adapter.getId(), adapter);
	        	items[i] = new SelectItem(Long.valueOf(
		        		inst.getSeqId()),
		        		adapter.getCaption());
	        }
	    	Arrays.sort(items, new Comparator() {
				public int compare(Object o1, Object o2) {
					SelectItem i1 = (SelectItem)o1;
					SelectItem i2 = (SelectItem)o2;
					return i1.getLabel().compareTo(i2.getLabel());
				}
	    	});

	    	for (int i = 0; i < items.length; i++)
	    	    result.add(items[i]);
		}
		return result;
	}
	
	public boolean getIsMany() {
		return "*".equals(this.property.getUpperValue());
	}
	
	public boolean getIsRequired() {
		return this.property.getLowerValue() == 1;
	}
	
	public Object getValue() {
		if (this.slot != null) {
			return this.getValue(this.slot.getValue(0));
		}
		return "";
	}
	
	private Object getValue(ValueSpecification vs) {		
		if (this.getIsPrimitiveType()) {
		    PrimitiveType pt = getPrimitiveType();
			return getPrimitiveValue(vs, pt);
		}
		else if (this.getIsEnumerationType()) {
			// for slots/properties with an enumeration datatype
			// the data is stored as a literal string which is
			// the enumeration literal value(s)
	       	if (vs.getLiteralStringCount() > 0)
	    		return vs.getLiteralString(0).getValue();
		}
		else if (this.getIsClassType()) {
	    	if (vs.getInstanceValueCount() > 0) {
	    		InstanceSpecification is = vs.getInstanceValue(0).getInstance();
	    		if (is != null)
	    		    return is.getSeqId(); 
	    		else
	    			return defaultId;
	    	}
	    	else
	    		return defaultId;
		}
		else
			log.warn("unexpected data type");
    	
		return null;
	}  
	
    public void setValue(Object value) {
	    try {
	    	ValueSpecification vs = null;
	    	if (this.slot == null) { // create it if user setting it
	    		this.slot = this.root.createSlot();	
	    		this.slot.setExternalId(UUID.randomUUID().toString());
	    		Property copy = (Property)PlasmaCopyHelper.INSTANCE.copyShallow(this.property);
	    		this.slot.setDefiningFeature(copy); // link an orphaned copy
	    	    vs = this.slot.createValue();
	    	    vs.setName("value spec for " + this.property.getName());
	    	}    
	    	else
	    		vs = this.slot.getValue(0);
	    	
			if (this.getIsPrimitiveType()) {
			    PrimitiveType pt = getPrimitiveType();
				   setPrimitiveValue(vs, pt, value);
			}
			else if (this.getIsEnumerationType()) {
				// for slots/properties with an enumeration datatype
				// the data is stored as a literal string which is
				// the enumeration literal value(s)
				LiteralString literal = null;
				if (vs.getLiteralStringCount() > 0)
					literal = vs.getLiteralString(0);
				else
					literal = vs.createLiteralString();
				literal.set(LiteralString.PTY_VALUE, value);
			}
			else if (this.getIsClassType()) {
				InstanceValue instValue = null;
		    	// JSF is sending us String objects from
		    	// selectOneMenu even though Long objects
		    	// are being explicitly set into SelectItem, presumably
		    	// because of the generic getValue/setValue method signature (?)
		    	// Hence, below hack
		    	Long id = Long.valueOf(String.valueOf(value));
		    	
		    	if (vs.getInstanceValueCount() > 0) 
		    		instValue = vs.getInstanceValue(0); 
		    	else if (id.longValue() != -1)
		    		instValue = vs.createInstanceValue();	
		    	
		    	if (instValue != null) {
		    		if (id.longValue() != -1) {
		    			InstanceSpecificationAdapter is = this.classInstanceMap.get(id);			    	    
			    	    InstanceSpecification copy = (InstanceSpecification)PlasmaCopyHelper.INSTANCE.copyShallow(
			    	    		is.getInstanceSpecification());
			    	    instValue.setInstance(copy);
		    		}
		    		else
		    			instValue.unsetInstance();
		    	}
			}
			else
				log.warn("unexpected data type");
	    }
	    catch (Throwable t) {
	    	log.error(t.getMessage(), t);
	    }
    }   

	private Object getPrimitiveValue(ValueSpecification vs, 
			PrimitiveType pt) {

		String name = pt.getDataType().getClassifier().getName();
		if (PrimitiveTypeName.STRING.name().equalsIgnoreCase(name)) {
	       	if (vs.getLiteralStringCount() > 0)
	    		return vs.getLiteralString(0).getValue();
		}
		else if (PrimitiveTypeName.CLOB.name().equalsIgnoreCase(name)) {
	       	if (vs.getLiteralClobCount() > 0)
	    		return vs.getLiteralClob(0).getValue();
		}
		else if (PrimitiveTypeName.DATE.name().equalsIgnoreCase(name)) {
	       	if (vs.getLiteralDateCount() > 0)
	    		return vs.getLiteralDate(0).getValue();
		}
		else if (PrimitiveTypeName.BOOLEAN.name().equalsIgnoreCase(name)) {
	       	if (vs.getLiteralBooleanCount() > 0)
	    		return vs.getLiteralBoolean(0).getValue();
		}
		else if (PrimitiveTypeName.SHORT.name().equalsIgnoreCase(name)) {
	       	if (vs.getLiteralShortCount() > 0)
	    		return vs.getLiteralShort(0).getValue();
		}
		else if (PrimitiveTypeName.INTEGER.name().equalsIgnoreCase(name)) {
	       	if (vs.getLiteralIntegerCount() > 0)
	    		return vs.getLiteralInteger(0).getValue();
		}
		else if (PrimitiveTypeName.LONG.name().equalsIgnoreCase(name)) {
	       	if (vs.getLiteralLongCount() > 0)
	    		return vs.getLiteralLong(0).getValue();
		}
		else if (PrimitiveTypeName.FLOAT.name().equalsIgnoreCase(name)) {
	       	if (vs.getLiteralFloatCount() > 0)
	    		return vs.getLiteralFloat(0).getValue();
		}
		else if (PrimitiveTypeName.DOUBLE.name().equalsIgnoreCase(name)) {
	       	if (vs.getLiteralDoubleCount() > 0)
	    		return vs.getLiteralDouble(0).getValue();
		}
		else
			log.warn("unknown type, " + name);
		return null;
	}
	
	private void setPrimitiveValue(ValueSpecification vs, 
			PrimitiveType pt, Object value) {

		String name = pt.getDataType().getClassifier().getName();
		if (PrimitiveTypeName.STRING.name().equalsIgnoreCase(name)) {
			LiteralString literal = null;
			if (vs.getLiteralStringCount() > 0)
				literal = vs.getLiteralString(0);
			else
				literal = vs.createLiteralString();
			literal.set(LiteralString.PTY_VALUE, value);
		}
		else if (PrimitiveTypeName.CLOB.name().equalsIgnoreCase(name)) {
			LiteralClob literal = null;
			if (vs.getLiteralClobCount() > 0)
				literal = vs.getLiteralClob(0);
			else
				literal = vs.createLiteralClob();
			literal.set(LiteralClob.PTY_VALUE, value);
		}
		else if (PrimitiveTypeName.DATE.name().equalsIgnoreCase(name)) {
			LiteralDate literal = null;
			if (vs.getLiteralDateCount() > 0)
				literal = vs.getLiteralDate(0);
			else
				literal = vs.createLiteralDate();
			literal.set(LiteralDate.PTY_VALUE, value);
		}
		else if (PrimitiveTypeName.BOOLEAN.name().equalsIgnoreCase(name)) {
			LiteralBoolean literal = null;
			if (vs.getLiteralBooleanCount() > 0)
				literal = vs.getLiteralBoolean(0);
			else
				literal = vs.createLiteralBoolean();
			// JSF can send us a String "true" / "false" or here 
			literal.set(LiteralBoolean.PTY_VALUE, 
					Boolean.valueOf(String.valueOf(value)));
		}
		else if (PrimitiveTypeName.SHORT.name().equalsIgnoreCase(name)) {
			LiteralShort literal = null;
			if (vs.getLiteralShortCount() > 0)
				literal = vs.getLiteralShort(0);
			else
				literal = vs.createLiteralShort();
			literal.set(LiteralShort.PTY_VALUE, value);
		}
		else if (PrimitiveTypeName.INTEGER.name().equalsIgnoreCase(name)) {
			LiteralInteger literal = null;
			if (vs.getLiteralIntegerCount() > 0)
				literal = vs.getLiteralInteger(0);
			else
				literal = vs.createLiteralInteger();
			literal.set(LiteralInteger.PTY_VALUE, value);
		}
		else if (PrimitiveTypeName.LONG.name().equalsIgnoreCase(name)) {
			LiteralLong literal = null;
			if (vs.getLiteralLongCount() > 0)
				literal = vs.getLiteralLong(0);
			else
				literal = vs.createLiteralLong();
			literal.set(LiteralLong.PTY_VALUE, value);
		}
		else if (PrimitiveTypeName.FLOAT.name().equalsIgnoreCase(name)) {
			LiteralFloat literal = null;
			if (vs.getLiteralFloatCount() > 0)
				literal = vs.getLiteralFloat(0);
			else
				literal = vs.createLiteralFloat();
			// JSF can send us a Double or Long here based on the
			// converter on the input text
			literal.set(LiteralFloat.PTY_VALUE, 
					Float.valueOf(String.valueOf(value))); 
		}
		else if (PrimitiveTypeName.DOUBLE.name().equalsIgnoreCase(name)) {
			LiteralDouble literal = null;
			if (vs.getLiteralDoubleCount() > 0)
				literal = vs.getLiteralDouble(0);
			else
				literal = vs.createLiteralDouble();
			literal.set(LiteralDouble.PTY_VALUE, value);
			// JSF can send us a Double or Long here based on the
			// converter on the input text
			literal.set(LiteralFloat.PTY_VALUE, 
					Double.valueOf(String.valueOf(value))); 
		}
		else
			log.warn("unknown type, " + name);
	}
	
}
