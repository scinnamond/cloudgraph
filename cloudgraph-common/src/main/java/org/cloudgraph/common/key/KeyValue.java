package org.cloudgraph.common.key;

import org.plasma.query.model.QueryConstants;
import org.plasma.sdo.PlasmaProperty;

/**
 * Associates an SDO property with a data value and optional 
 * path to the property from a given context root.
 * @author Scott Cinnamond
 * @since 0.5
 */
public class KeyValue {
	private PlasmaProperty prop;
	private String propertyPath;
	private Object value;
	private boolean isWildcard = false;
	private String wildcard = QueryConstants.WILDCARD; 
	
	@SuppressWarnings("unused")
	private KeyValue() {}
	public KeyValue(PlasmaProperty prop, Object value) {
    	this.prop = prop;
    	this.value = value;
    }
	
	public PlasmaProperty getProp() {
		return prop;
	}
	
	public Object getValue() {
		return value;
	}
	
	public String getPropertyPath() {
		return propertyPath;
	}
	
	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}
	
	public boolean isWildcard() {
		return isWildcard;
	}
	
	public void setIsWildcard(boolean isWildcard) {
		this.isWildcard = isWildcard;
	}
	
	public String getWildcard() {
		return wildcard;
	}
	
	public void setWildcard(String wildcard) {
		this.wildcard = wildcard;
	}
	
	public String toString() {
		return this.prop.getName() + "/" + String.valueOf(this.value);
	}
}

