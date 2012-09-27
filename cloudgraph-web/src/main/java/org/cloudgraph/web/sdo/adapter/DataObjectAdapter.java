package org.cloudgraph.web.sdo.adapter;

import org.plasma.sdo.PlasmaType;

import commonj.sdo.Type;


public class DataObjectAdapter {

	private commonj.sdo.DataObject dataObject;
	
	@SuppressWarnings("unused")
	private DataObjectAdapter(){}
	public DataObjectAdapter(commonj.sdo.DataObject dataObject) {
		this.dataObject = dataObject;
	}
	
	public Type getType() {
		return this.dataObject.getType();
	}
	
	public String getCaption() {
		try {
		    return this.dataObject.getString("name");
		}
		catch (IllegalArgumentException e) {
			return "";
		}
	}
	
	public String getDescription() {
		return ((PlasmaType)this.dataObject.getType()).getDescriptionText();
	}
}
