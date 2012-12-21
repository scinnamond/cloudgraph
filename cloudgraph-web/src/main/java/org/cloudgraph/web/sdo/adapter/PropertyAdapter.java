package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;

import org.cloudgraph.web.sdo.meta.Property;

public class PropertyAdapter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Property prop;
	private String queueColumnSortOrder = "UNSORTED";

    @SuppressWarnings("unused")
	private PropertyAdapter() {}
    
	public PropertyAdapter(Property prop) {
		super();
		this.prop = prop;
	}
	
	// NOTE: must implement equals to use this in
	// RF controls such as listShuttle
	public boolean equals(Object other) {
		return this.getName().equals(
				((PropertyAdapter)other).getName());
	}

	public String getQueueColumnSortOrder() {
		return queueColumnSortOrder;
	}

	public void setQueueColumnSortOrder(String queueColumnSortOrder) {
		this.queueColumnSortOrder = queueColumnSortOrder;
	}

	public Property getProperty() {
		return this.prop;
	}
	
	public String toString() {
		return getName() 
			+ " (required: " + getIsRequired()
			+ " multiplicity: " + getCardinality() + ")";
	}
		
	public Long getId() {
		return prop.getSeqId();
	}
	
	public String getName() {
		return prop.getName();
	}
	
	public String getDisplayName() {
		return prop.getName();
	}
	
	public String getDefinition() {
		return prop.getDefinition(); 
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
