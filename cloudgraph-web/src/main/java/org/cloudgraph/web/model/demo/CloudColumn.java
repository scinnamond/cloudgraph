package org.cloudgraph.web.model.demo;

import java.io.Serializable;

public class CloudColumn implements Serializable {

	private static final long serialVersionUID = 1L;
	private String queueColumnSortOrder = "UNSORTED";
	private String displayName;
	private String fullName;
	
	public CloudColumn(String fullName) {
		this.fullName = fullName;
		int idx = this.fullName.lastIndexOf(":");// FIXME: get from config
		if (idx > 0)
		    this.displayName = this.fullName.substring(idx); 
		else
			this.displayName = fullName;
	}
	
	public String getDescription() {
		return this.fullName;
	}
	
	public String getQueueColumnSortOrder() {
		return queueColumnSortOrder;
	}
	public void setQueueColumnSortOrder(String queueColumnSortOrder) {
		this.queueColumnSortOrder = queueColumnSortOrder;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	
}
