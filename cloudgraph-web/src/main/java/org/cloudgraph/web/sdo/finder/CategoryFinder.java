package org.cloudgraph.web.sdo.finder;

import org.plasma.sdo.PlasmaDataGraphVisitor;

import org.cloudgraph.web.sdo.categorization.Category;

import commonj.sdo.DataObject;

public class CategoryFinder implements PlasmaDataGraphVisitor {
	
	private long seqId = -1L;
	private String name;
	private String parentName;
	private Category result;
	
	@SuppressWarnings("unused")
	private CategoryFinder() {}
	
	public CategoryFinder(long seqId) {
		this.seqId = seqId;
	}
	
	public CategoryFinder(String name, String parentName) {
		this.name = name;
		this.parentName = parentName;
		if (this.name == null)
			throw new IllegalArgumentException("expected non-null name argument");
		if (this.parentName == null)
			throw new IllegalArgumentException("expected non-null parentName argument");
	}	

	public void visit(DataObject target, DataObject source, 
			String sourceKey, int level) {
		
		if (result != null)
			return; // FIXME: need means of aborting traversal
		
		if (target instanceof Category) {
			Category cat = (Category)target;
			if (this.seqId != -1) {
			    if (cat.getSeqId() == this.seqId)
				    result = cat; 
			}
			else {
				if (cat.getParent() != null)
			        if (this.name.equals(cat.getName()) && 
			    	    this.parentName.equals(cat.getParent().getName())	)
				        result = cat; 
			}
		}
	}

	public Category getResult() {
		return result;
	}
	
}
