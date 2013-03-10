package org.cloudgraph.web.model.tree;


public interface TreeSelectionModel {

	public TreeNodeAction getSelection();
	public void setSelection(TreeNodeAction selection);
	public void clearSelection();
}
