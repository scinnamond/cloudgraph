package org.cloudgraph.web.model.search;

import java.util.MissingResourceException;

import org.cloudgraph.web.ResourceManager;
import org.cloudgraph.web.ResourceType;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.PropertyDatatype;



public class SearchParameter {
    protected PropertyDatatype dataType;
    protected ComponentName componentName;
    protected Object value;
    protected Object defaultValue;
    protected Search search;
    protected int index;
    protected SearchParameterListener listener;
    
    @SuppressWarnings("unused")
	private SearchParameter(){}
	public SearchParameter(ComponentName name, PropertyDatatype dataType, 
			Object defaultValue,
			Search search, 
			SearchParameterListener listener) {
		super();
		this.dataType = dataType;
		this.componentName = name;
		this.search = search;
		this.defaultValue = defaultValue;
		this.listener = listener;
	}
	
	public String getDisplayValue() {
		return String.valueOf(value);
	}
	
	public String getValueIconName() {
		String id = "aplsSearch_" + this.componentName.value();
		String valueIcon = "";
		try {
			valueIcon = ResourceManager.instance().getString(id, 
				ResourceType.ICON);
		}
		catch (MissingResourceException e) {			
		}
		return valueIcon;
	}
		
	public String getDisplayName() {
		String id = "aplsSearch_" + this.componentName.value();
		String displayName = this.componentName.toString();
		try {
		    displayName = ResourceManager.instance().getString(id, 
				ResourceType.LABEL);
		}
		catch (MissingResourceException e) {			
		}
		return displayName;
	}
	
	public PropertyDatatype getDataType() {
		return dataType;
	}
	
	public ComponentName getComponentName() {
		return this.componentName;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
		if (this.listener != null)
		    this.listener.valueSet();
	}

	public Object fromString(String value) {
		switch(this.dataType) {
		case STRING:
		    return value;	
		case INTEGER:
			return new Integer(value);
		case REFERENCE:
			return new Long(value);
		default:
			throw new RuntimeException("unknown datatype, " 
					+ this.dataType.value());
		}
	}
	
	public boolean getHasValue() {
		switch(this.dataType) {
		case STRING:
		    return this.value != null && ((String)this.value).trim() != (String)defaultValue;	
		case INTEGER:
			return this.value != null && ((Integer)this.value).intValue() != ((Integer)defaultValue).intValue();
		case REFERENCE:
			return this.value != null && ((Long)this.value).longValue() != ((Long)defaultValue).longValue();
		}
		return false;
	}
    
    public String removeValue() {
		switch(this.dataType) {
		case STRING:
		    this.value = (String)defaultValue;
		    break;
		case INTEGER:
			this.value = (Integer)defaultValue;
			break;
		case REFERENCE:
			this.value = (Long)defaultValue;
			break;
		}
		
		if (this.listener != null)
		    this.listener.valueRemoved();
		this.search.reloadActiveParameters();
		
		return null;
    }
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
    
    
}
