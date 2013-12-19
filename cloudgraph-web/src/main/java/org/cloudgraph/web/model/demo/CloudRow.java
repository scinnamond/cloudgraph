package org.cloudgraph.web.model.demo;

import java.io.Serializable;

import org.cloudgraph.web.sdo.adapter.QueueAdapter;

public class CloudRow extends QueueAdapter {
	
	private static final long serialVersionUID = 1L;
	private Object key;
    private Object[] data;

	public CloudRow(Object key, Object[] rowData) {
		super();
		this.data = rowData;
		this.key = key;
	}

	public Object[] getData() {
		return data;
	}

	public Object getKey() {
		return key;
	}
    
    
}
