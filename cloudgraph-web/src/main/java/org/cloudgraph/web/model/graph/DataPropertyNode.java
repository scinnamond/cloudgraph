package org.cloudgraph.web.model.graph;

import org.cloudgraph.web.model.tree.GraphPropertyNode;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

class DataPropertyNode extends CommonNode implements GraphPropertyNode {
	private Property dataProperty;
	private DataObject sourceDataObject;
	
	public DataPropertyNode(Property dataProperty, DataObject sourceDataObject) {
		super();
		this.dataProperty = dataProperty;
		this.sourceDataObject = sourceDataObject;
	}
	public String getName() {
		return getLabel();
	}
	
    public String getLabel() {
        return this.dataProperty.getName() + 
        	": " + this.sourceDataObject.getString(
        			this.dataProperty); 
    }

	public Property getDataProperty() {
		return dataProperty;
	}

	public DataObject getSourceDataObject() {
		return sourceDataObject;
	}
	
	@Override
	public String getPropertyName() {
		return this.dataProperty.getName();
	}
	
	@Override
	public String getPropertyIsMany() {
		return String.valueOf(this.dataProperty.isMany());
	}
	
	@Override
	public String getPropertyIsReadOnly() {
		return String.valueOf(this.dataProperty.isReadOnly());
	}
	
	@Override
	public String getPropertyDataType() {
		return String.valueOf(this.dataProperty.getType().getName());
	}
	
}
