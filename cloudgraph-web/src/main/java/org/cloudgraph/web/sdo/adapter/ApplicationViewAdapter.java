package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;

import org.cloudgraph.web.sdo.core.ApplicationView;

public class ApplicationViewAdapter implements Serializable {
	private static final long serialVersionUID = 1L;
	private ApplicationView app;

    @SuppressWarnings("unused")
	private ApplicationViewAdapter() {}
    
	public ApplicationViewAdapter(ApplicationView app) {
		super();
		this.app = app;
	}
	
	public Long getId() {
		return app.getId();
	}
	
	public Long getInstanceSeqId() {
		return this.app.getInstanceSeqId();
	}
	
	public Long getInstanceValue() {
		return this.app.getInstanceValue();
	}
	
	public String getStringValue() {
		return this.app.getStringValue();
	}
	
	public int getIntValue() {
		return this.app.getIntValue();
	}

	public String getPropertyName() {
		return this.app.getPropertyName();
	}
	
	public int getCatId() {
		return this.app.getCatId();
	}
	
	public String getCatName() {
		return this.app.getCatName();
	}
	
	public int getParentCatId() {
		return this.app.getParentCatId();
	}
	public String getParentCatName() {
		return this.app.getParentCatName();
	}
		
	

	
}
