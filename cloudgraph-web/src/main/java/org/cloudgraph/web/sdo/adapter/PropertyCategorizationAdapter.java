package org.cloudgraph.web.sdo.adapter;

import java.util.UUID;

import org.cloudgraph.web.sdo.categorization.Categorization;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.PropertyCategorization;
import org.cloudgraph.web.sdo.meta.Property;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;


public class PropertyCategorizationAdapter extends CategorizationAdapter {

	private PropertyCategorization propertyCategorization;
	private Property root;
	
	public PropertyCategorizationAdapter(Property element) {
		this.root = element;
		if (this.root == null)
			throw new IllegalArgumentException("expected property element");
	}
	
	public PropertyCategorizationAdapter(PropertyCategorization propertyCategorization) {
		super(propertyCategorization.getCategorization());
		this.propertyCategorization = propertyCategorization;
		if (this.propertyCategorization == null)
			throw new IllegalArgumentException("unexpected null argument, 'propertyCategorization'");
	    this.root = this.propertyCategorization.getProperty();
		if (this.root == null)
			throw new IllegalArgumentException("expected property element");
	}
	
	public PropertyCategorizationAdapter(PropertyCategorization propertyCategorization,
			Category cat) {
		super(propertyCategorization.getCategorization(), cat);
		this.propertyCategorization = propertyCategorization;
		if (this.propertyCategorization == null)
			throw new IllegalArgumentException("unexpected null argument, 'propertyCategorization'");
	    this.root = this.propertyCategorization.getProperty();
		if (this.root == null)
			throw new IllegalArgumentException("expected property element");
	}	
	
	public PropertyCategorization getPropertyCategorization() {
		return this.propertyCategorization;
	}		
	
	protected boolean isInitializedCategorization() {
		return this.propertyCategorization != null;
	}
	public DataGraph getDataGraph() {
		return this.propertyCategorization.getDataGraph();
	}
	
	public DataObject getRoot() {
		return this.propertyCategorization;
	}
	
	protected void initCategorization() {
		this.propertyCategorization = this.root.createPropertyCategorization();
		Categorization catz = this.propertyCategorization.createCategorization();
		catz.setExternalId(UUID.randomUUID().toString());
		super.setCategorization(catz);		
	}	
	
}
