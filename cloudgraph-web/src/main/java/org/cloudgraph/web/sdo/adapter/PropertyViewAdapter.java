package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;

import org.cloudgraph.web.sdo.core.PropertyView;

public class PropertyViewAdapter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private PropertyView prop;

    @SuppressWarnings("unused")
	private PropertyViewAdapter() {}
    
	public PropertyViewAdapter(PropertyView prop) {
		super();
		this.prop = prop;
	}
	
	public boolean equals(Object other) {
		return this.getName().equals(
				((PropertyViewAdapter)other).getName());
	}
		
	public Long getId() {
		return prop.getId();
	}
	
	public Long getPropertyId() {
		return prop.getSeqId();
	}
 
	public String getName() {
		return prop.getName();
	}
	
	public String getDefinition() {
		return prop.getDefinition(); 
	}
	
	public String getClassName() {
		return prop.getClassName();
	}
	
	public String getCat() {
		return prop.getCatName();
	}
	
	public Long getCatId() {
		return prop.getCatId();
	}
	
	public String getParentCat() {
		return prop.getParentCatName();
	}
	
	public Long getParentCatId() {
		return prop.getParentCatSeqId();
	}
	
	public String getDataType() {
		return prop.getDataType();
	}
	
	public String getCardinality() {
		return prop.getLowerValue() + ":"
		    + prop.getUpperValue();
	}
	
	public boolean getIsRequired() {
		return prop.getLowerValue() == 1; 	
	}

	public boolean getIsMany() {
		return "*".equals(prop.getUpperValue()); 	
	}
	
}
