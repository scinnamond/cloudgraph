package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;

import org.cloudgraph.web.sdo.core.ClassView;

public class ClassViewAdapter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private ClassView classView;

    @SuppressWarnings("unused")
	private ClassViewAdapter() {}
    
	public ClassViewAdapter(ClassView classView) {
		super();
		this.classView = classView;
	}
		
	public String getName() {
		return classView.getName();
	}
	
	public String getDescription() {
		return "description for class"; 
	}
		
	public Long getId() {
		return classView.getId();
	}
	
	
}
