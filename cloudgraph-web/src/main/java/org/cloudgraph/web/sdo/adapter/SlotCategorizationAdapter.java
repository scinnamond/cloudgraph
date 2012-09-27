package org.cloudgraph.web.sdo.adapter;

import java.util.UUID;

import org.cloudgraph.web.sdo.categorization.Categorization;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.SlotCategorization;
import org.cloudgraph.web.sdo.meta.Slot;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;


public class SlotCategorizationAdapter extends CategorizationAdapter {

	private SlotCategorization slotCategorization;
	private Slot root;
	
	public SlotCategorizationAdapter(Slot element) {
		this.root = element;
		if (this.root == null)
			throw new IllegalArgumentException("expected element");
	}
	
	public SlotCategorizationAdapter(SlotCategorization classCategorization) {
		super(classCategorization.getCategorization());
		this.slotCategorization = classCategorization;
		if (this.slotCategorization == null)
			throw new IllegalArgumentException("unexpected null argument, 'propertyCategorization'");
	    this.root = this.slotCategorization.getSlot();
		if (this.root == null)
			throw new IllegalArgumentException("expected element");
	}
	
	public SlotCategorizationAdapter(SlotCategorization classCategorization,
			Category cat) {
		super(classCategorization.getCategorization(), cat);
		this.slotCategorization = classCategorization;
		if (this.slotCategorization == null)
			throw new IllegalArgumentException("unexpected null argument, 'propertyCategorization'");
	    this.root = this.slotCategorization.getSlot();
		if (this.root == null)
			throw new IllegalArgumentException("expected element");
	}	
	
	public SlotCategorization getSlotCategorization() {
		return this.slotCategorization;
	}		
	
	protected boolean isInitializedCategorization() {
		return this.slotCategorization != null;
	}
	
	public DataGraph getDataGraph() {
		return this.slotCategorization.getDataGraph();
	}
	
	public DataObject getRoot() {
		return this.slotCategorization;
	}
	
	protected void initCategorization() {
		this.slotCategorization = this.root.createSlotCategorization();
		Categorization catz = this.slotCategorization.createCategorization();
		catz.setExternalId(UUID.randomUUID().toString());
		super.setCategorization(catz);		
	}	
	
}
