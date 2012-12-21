package org.cloudgraph.web.model.graph;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

class DataObjectNode extends CommonNode implements LabeledGraphNode {
	private DataObject dataObject;
	private Property sourceProperty;
	
	public DataObjectNode(DataObject dataObject, Property sourceProperty) {
		super();
		this.dataObject = dataObject;
		this.sourceProperty = sourceProperty;
	}
	public String getName() {
		return getLabel();
	}
	
    public String getLabel() {
    	if (this.sourceProperty != null)
            return this.sourceProperty.getName() + " ("
        	    + this.dataObject.getType().getName()
        	    + ")"; 
    	else 
    		return this.dataObject.getType().getName();  
    }
	
	public DataObject getDataObject() {
		return dataObject;
	}
	
	public void setDataObject(DataObject dataObject) {
		this.dataObject = dataObject;
	}
}
