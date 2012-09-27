package org.cloudgraph.web.sdo.adapter;

import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

import org.cloudgraph.web.sdo.categorization.Category;

import commonj.sdo.Property;

public class CategoryAdapter {

	private Category cat;
	
	@SuppressWarnings("unused")
	private CategoryAdapter(){}
	public CategoryAdapter(Category cat) {
		this.cat = cat;
	}
	
	public Category getDelegate() {
		return this.cat;
	}

	public long getSeqId() {
		return this.cat.getSeqId();
	}
	
	public void setSeqId(long id) {
		this.cat.setSeqId(id);
	}
	
	public String getTooltip() {
		return ((PlasmaType)this.cat.getType()).getDescriptionText();
	}
	
	public String getName() {
		return this.cat.getName();
	}
	
	public void setName(String name) {
		this.cat.setName(name);
	}
	
	public String getNameTooltip() {
		Property prop = this.cat.getType().getProperty(
				Category.PTY_NAME);

		return ((PlasmaProperty)prop).getDescriptionText();
	}

	public int getId() {
		return this.cat.getId();
	}
	
	public void setId(int id) {
		this.cat.setId(id);
	}

	public String getIdTooltip() {
		Property prop = this.cat.getType().getProperty(
				Category.PTY_ID);
		return ((PlasmaProperty)prop).getDescriptionText();
	}
	
	public String getDefinition() {
		return this.cat.getDefinition();
	}

	public void setDefinition(String defn) {
		this.cat.setDefinition(defn);
	}
	
	public String getDefinitionTooltip() {
		Property prop = this.cat.getType().getProperty(
				Category.PTY_DEFINITION);
		return ((PlasmaProperty)prop).getDescriptionText();
	}
	
	public String getTruncatedDefinition() {
		if (this.cat.getDefinition() != null) {
			if (this.cat.getDefinition().length() <= 40)
		        return this.cat.getDefinition();
			else
				return this.cat.getDefinition().substring(0, 37) + "...";
		}
		else
			return null;
	}
	
}
