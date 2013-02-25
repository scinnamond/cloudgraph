package org.cloudgraph.web.sdo.adapter;

import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

import org.cloudgraph.web.sdo.categorization.Categorization;
import org.cloudgraph.web.sdo.categorization.CategorizationNote;
import org.cloudgraph.web.sdo.categorization.CategorizationNoteType;
import org.cloudgraph.web.sdo.categorization.CategorizationWeight;
import org.cloudgraph.web.sdo.categorization.CategorizationWeightType;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Slot;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;

public abstract class CategorizationAdapter {

	protected Categorization categorization;
	protected Category category;
	
	/**
	 *   
	 */
	public CategorizationAdapter(){		
	}	
	
	public CategorizationAdapter(Categorization categorization) {
		if (categorization == null)
			throw new IllegalArgumentException("undepected null argument, 'categorization'");
		this.categorization = categorization;
	}
	
	public CategorizationAdapter(Categorization categorization, 
			Category cat) {
		if (categorization == null)
			throw new IllegalArgumentException("undepected null argument, 'categorization'");
		if (cat == null)
			throw new IllegalArgumentException("undepected null argument, 'cat'");
		this.categorization = categorization;
		this.category = cat;
	}
	
	protected abstract boolean isInitializedCategorization();
	protected abstract void initCategorization();
	public abstract DataGraph getDataGraph();
	public abstract DataObject getRoot();
	
	public static CategorizationAdapter newAdapter(DataObject dataObject) {
		if (dataObject instanceof org.cloudgraph.web.sdo.meta.Property)
			return new PropertyCategorizationAdapter((org.cloudgraph.web.sdo.meta.Property)dataObject);
		else if (dataObject instanceof Clazz)
			return new ClassCategorizationAdapter((Clazz)dataObject);
		else if (dataObject instanceof InstanceSpecification)
			return new InstanceCategorizationAdapter((InstanceSpecification)dataObject);
		else if (dataObject instanceof Slot)
			return new SlotCategorizationAdapter((Slot)dataObject);
		else
			throw new RuntimeException("unexpected instance, "
					+ dataObject.getClass().getName());
	}
	
	public Categorization getCategorization() {
		return this.categorization;
	}
	
	public void setCategorization(Categorization categorization) {
		this.categorization = categorization;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
		if (this.categorization == null)
			this.initCategorization();
		this.categorization.setCategory(this.category);
	}
	
	public long getSeqId() {
		return this.categorization.getSeqId();
	}
	
	public void setSeqId(long id) {
		this.categorization.setSeqId(id);
	}
	
	public String getCategoryTooltip() {
		return ((PlasmaType)this.category.getType()).getDescriptionText();
	}
	
	public String getCategoryName() {
		return this.category.getName();
	}
	
	public void setCategoryName(String name) {
		// cat is reference data - readony
	}

	public int getCategoryId() {
		return this.category.getId();
	}
	
	public void setCategoryId(int id) {
		// cat is reference data - readony
	}
	
	public String getCategoryNameTooltip() {
		Property prop = this.category.getType().getProperty(
				Category.PROPERTY.name.name());
		return ((PlasmaProperty)prop).getDescriptionText();
	}
	
	public String getCategoryDefinition() {
		return this.category.getDefinition();
	}

	public void setCategoryDefinition(String defn) {
		// cat is reference data - readony
	}
	
	public String getCategoryDefinitionTooltip() {
		Property prop = this.category.getType().getProperty(
				Category.PROPERTY.definition.name());
		return ((PlasmaProperty)prop).getDescriptionText();
	}
	
	public String getTruncatedCategoryDefinition() {
		if (this.category.getDefinition() != null) {
			if (this.category.getDefinition().length() <= 40)
		        return this.category.getDefinition();
			else
				return this.category.getDefinition().substring(0, 37) + "...";
		}
		else
			return null;
	}
	
	public String getParentCategoryName() {
		if (this.category.getParent() != null)
		    return this.category.getParent().getName();
		else
			return null;
	}
	
	public void setParentCategoryName(String name) {
		// cat is reference data - readony
	}
	
	public int getParentCategoryId() {
		if (this.category.getParent() != null)
		    return this.category.getParent().getId();
		else
			return 0;
	}
	
	public void setParentCategoryId(int id) {
		// cat is reference data - readony
	}
	
	public Float getProbability() {
		return getWeight(CategorizationWeightType.PROBABILITY);
	}
	
	public void setProbability(Float value) {
		setWeight(CategorizationWeightType.PROBABILITY, value);
	}
	
	public Float getImpact() {
		return getWeight(CategorizationWeightType.IMPACT);
	}
	
	public Float getRiskFactor() {
		return getProbability() * getImpact();
	}	
	
	public void setImpact(Float value) {
		setWeight(CategorizationWeightType.IMPACT, value);
	}

	public String getRiskMitigationNote() {
		return getNote(CategorizationNoteType.GENERAL);
	}
	
	public void setRiskMitigationNote(String value) {
		setNote(CategorizationNoteType.GENERAL, value);
	}	
	
	public Float getWeight(CategorizationWeightType weightType) {
		CategorizationWeight weight = findCategorizationWeight(weightType);
		if (weight != null)
			return weight.getQuantity();
		else
			return new Float(-1);
	}	
	
	public void setWeight(CategorizationWeightType weightType, Float value) {
		CategorizationWeight weight = findCategorizationWeight(weightType);
		if (weight == null) {
			if (!isInitializedCategorization())
				initCategorization();

			weight = this.categorization.createCategorizationWeight();
			weight.setWeightType(weightType.getInstanceName());
			weight.setQuantity(value);			
		}
		weight.setQuantity(value);
	}
	
	public void setNote(CategorizationNoteType noteType, String value) {
		CategorizationNote note = findCategorizationNote(noteType);
		if (note == null) {
			if (!isInitializedCategorization())
				initCategorization();
			Categorization catz = this.getCategorization();
			note = catz.createCategorizationNote();
			note.setNoteType(noteType.getInstanceName());
			note.setNote(value);			
		}
		note.setNote(value);
	}

	public String getNote(CategorizationNoteType noteType) {
		CategorizationNote note = findCategorizationNote(noteType);
		if (note != null)
			return note.getNote();
		else
			return null;
	}
	
	private CategorizationWeight findCategorizationWeight(CategorizationWeightType type) {
		if (this.getCategorization() != null) {
			Categorization catz = this.getCategorization();
			for (int i = 0; i < catz.getCategorizationWeightCount(); i++) {
				CategorizationWeight weight = catz.getCategorizationWeight(i);
				if (type.getInstanceName().equals(
						weight.getWeightType()))
					return weight;
			}
		}
		return null;
	}

	private CategorizationNote findCategorizationNote(CategorizationNoteType type) {
		if (this.getCategorization() != null) {
			Categorization catz = this.getCategorization();
			for (int i = 0; i < catz.getCategorizationNoteCount(); i++) {
				CategorizationNote note = catz.getCategorizationNote(i);
				if (type.getInstanceName().equals(
						note.getNoteType()))
					return note;
			}
		}
		return null;
	}
}
