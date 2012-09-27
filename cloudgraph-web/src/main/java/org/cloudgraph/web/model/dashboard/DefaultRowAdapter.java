package org.cloudgraph.web.model.dashboard;

import commonj.sdo.DataObject;

public class DefaultRowAdapter implements Row {

	protected Object[] data;
	protected DataObject dataObject;
	
	public DefaultRowAdapter(DataObject dataObject, Object[] data) {
		this.dataObject = dataObject;
		this.data = data;
	}
	
	public Object[] getData() {
		return this.data;
	}
	
    public DataObject getDataObject() {
    	return this.dataObject; 
    }

	public String getType() {
		return RowType.DEFAULT.name();
	}

}
