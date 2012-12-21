package org.cloudgraph.web.model.graph;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

class DataPropertyNode extends CommonNode implements LabeledGraphNode {
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
}
