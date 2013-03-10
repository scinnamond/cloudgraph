package org.cloudgraph.web.sdo.adapter;

import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.categorization.CategoryLink;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.Property;

public class CategoryLinkAdapter {

	private CategoryLink catLink;
	
	@SuppressWarnings("unused")
	private CategoryLinkAdapter(){}
	public CategoryLinkAdapter(CategoryLink catLink) {
		this.catLink = catLink;
	}
	
	public CategoryLink getDelegate() {
		return this.catLink;
	}

	public long getSeqId() {
		return this.catLink.getSeqId();
	}
	
	public void setSeqId(long id) {
		this.catLink.setSeqId(id);
	}
	
	public String getTooltip() {

		return ((PlasmaType)this.catLink.getType()).getDescriptionText();
	}
	
	public Category getLeft() {
		return this.catLink.getLeft();
	}

	public void setLeft(Category cat) {
		this.catLink.setLeft(cat);
	}
	
	public String getLeftCaption() {
		String result = "";
		Category cat = this.catLink.getLeft();			
		if (cat != null)
			while (cat.getParent() != null) { // exit before taxonomy/root cat
				if (result.length() > 0)
				    result = cat.getName() + "->" + result;
				else
				    result = cat.getName();
				cat = cat.getParent();
			}
		return result;
	}
	
	public String getLeftTooltip() {
		Property prop = this.catLink.getType().getProperty(
				CategoryLink.PROPERTY.left.name());
		return ((PlasmaProperty)prop).getDescriptionText();
	}

	public Category getRight() {
		return this.catLink.getRight();
	}

	public void setRight(Category cat) {
		this.catLink.setRight(cat);
	}
	
	public String getRightCaption() {
		String result = "";
		Category cat = this.catLink.getRight();
		if (cat != null)
			while (cat.getParent() != null) { // exit before taxonomy/root cat
				if (result.length() > 0)
				    result = cat.getName() + "->" + result;
				else
				    result = cat.getName();
				cat = cat.getParent();
			}
		return result;
	}
	
	public String getRightTooltip() {
		Property prop = this.catLink.getType().getProperty(
				CategoryLink.PROPERTY.right.name());
		return ((PlasmaProperty)prop).getDescriptionText();
	}
	
}
