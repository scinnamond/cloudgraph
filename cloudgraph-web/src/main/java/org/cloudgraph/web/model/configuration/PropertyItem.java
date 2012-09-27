package org.cloudgraph.web.model.configuration;

import java.io.Serializable;


public class PropertyItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String name;
	public PropertyItem(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	// NOTE: must implement equals to use this in
	// RF controls such as listShuttle
	public boolean equals(Object other) {
		return this.getId().equals(
				((PropertyItem)other).getId());
	}
	
	public Long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLabel() {
		return name;
	}
}
