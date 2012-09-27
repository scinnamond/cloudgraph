package org.cloudgraph.web.sdo.adapter;

import java.util.UUID;

import org.cloudgraph.web.sdo.categorization.Categorization;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.InstanceCategorization;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;


public class InstanceCategorizationAdapter extends CategorizationAdapter {

	private InstanceCategorization instanceCategorization;
	private InstanceSpecification root;
	
	public InstanceCategorizationAdapter(InstanceSpecification element) {
		this.root = element;
		if (this.root == null)
			throw new IllegalArgumentException("expected element");
	}
	
	public InstanceCategorizationAdapter(InstanceCategorization classCategorization) {
		super(classCategorization.getCategorization());
		this.instanceCategorization = classCategorization;
		if (this.instanceCategorization == null)
			throw new IllegalArgumentException("unexpected null argument, 'propertyCategorization'");
	    this.root = this.instanceCategorization.getInstanceSpecification();
		if (this.root == null)
			throw new IllegalArgumentException("expected element");
	}
	
	public InstanceCategorizationAdapter(InstanceCategorization classCategorization,
			Category cat) {
		super(classCategorization.getCategorization(), cat);
		this.instanceCategorization = classCategorization;
		if (this.instanceCategorization == null)
			throw new IllegalArgumentException("unexpected null argument, 'propertyCategorization'");
	    this.root = this.instanceCategorization.getInstanceSpecification();
		if (this.root == null)
			throw new IllegalArgumentException("expected element");
	}	
	
	public InstanceCategorization getInstanceCategorization() {
		return this.instanceCategorization;
	}		
	
	protected boolean isInitializedCategorization() {
		return this.instanceCategorization != null;
	}
	public DataGraph getDataGraph() {
		return this.instanceCategorization.getDataGraph();
	}
	public DataObject getRoot() {
		return this.instanceCategorization;
	}
	
	protected void initCategorization() {
		this.instanceCategorization = this.root.createInstanceCategorization();
		Categorization catz = this.instanceCategorization.createCategorization();
		catz.setExternalId(UUID.randomUUID().toString());
		super.setCategorization(catz);		
	}	
	
}
