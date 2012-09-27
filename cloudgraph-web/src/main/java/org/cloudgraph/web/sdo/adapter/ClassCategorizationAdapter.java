package org.cloudgraph.web.sdo.adapter;

import java.util.UUID;

import org.cloudgraph.web.sdo.categorization.Categorization;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.ClassCategorization;
import org.cloudgraph.web.sdo.meta.Clazz;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;


public class ClassCategorizationAdapter extends CategorizationAdapter {

	private ClassCategorization classCategorization;
	private Clazz root;
	
	public ClassCategorizationAdapter(Clazz element) {
		this.root = element;
		if (this.root == null)
			throw new IllegalArgumentException("expected property element");
	}
	
	public ClassCategorizationAdapter(ClassCategorization classCategorization) {
		super(classCategorization.getCategorization());
		this.classCategorization = classCategorization;
		if (this.classCategorization == null)
			throw new IllegalArgumentException("unexpected null argument, 'propertyCategorization'");
	    this.root = this.classCategorization.getClazz();
		if (this.root == null)
			throw new IllegalArgumentException("expected property element");
	}
	
	public ClassCategorizationAdapter(ClassCategorization classCategorization,
			Category cat) {
		super(classCategorization.getCategorization(), cat);
		this.classCategorization = classCategorization;
		if (this.classCategorization == null)
			throw new IllegalArgumentException("unexpected null argument, 'propertyCategorization'");
	    this.root = this.classCategorization.getClazz();
		if (this.root == null)
			throw new IllegalArgumentException("expected property element");
	}	
	
	public ClassCategorization getProjectCategorization() {
		return this.classCategorization;
	}		
	
	protected boolean isInitializedCategorization() {
		return this.classCategorization != null;
	}
	
	public DataGraph getDataGraph() {
		return this.classCategorization.getDataGraph();
	}
	
	public DataObject getRoot() {
		return this.classCategorization;
	}
	
	protected void initCategorization() {
		this.classCategorization = this.root.createClassCategorization();
		Categorization catz = this.classCategorization.createCategorization();
		catz.setExternalId(UUID.randomUUID().toString());
		super.setCategorization(catz);		
	}	
	
}
