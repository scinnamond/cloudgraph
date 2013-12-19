package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;

import org.cloudgraph.web.sdo.core.EnumerationView;

public class EnumerationViewAdapter extends QueueAdapter {
	
	private static final long serialVersionUID = 1L;
	private EnumerationView enumView;

    @SuppressWarnings("unused")
	private EnumerationViewAdapter() {}
    
	public EnumerationViewAdapter(EnumerationView enumView) {
		super();
		this.enumView = enumView;
	}
		
	public String getName() {
		return enumView.getName();
	}
	
	public String getDescription() {
		return "description for enum"; 
	}
		
	public Long getId() {
		return enumView.getId();
	}
	
	
}
