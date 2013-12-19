package org.cloudgraph.web.model.taxonomy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.tree.DynamicTreeNodeModel;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.categorization.Taxonomy;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public abstract class DynamicTaxonomyTreeBean extends ModelBean {

	private static Log log = LogFactory.getLog(DynamicTaxonomyTreeBean.class);
	private long ids = 0;
	private DefaultTreeNode treeRoot;
	// private TreeState treeState;
	protected TreeNodeTypeMap typeMap;
	protected CategoryLabelFormat labelFormat;
	private boolean displayNodeHelpText = true;
	protected Set<Integer> ajaxKeys = new HashSet<Integer>();
	protected TreeNode selectedNode;
    protected Category selectedCategory;

	public DynamicTaxonomyTreeBean() {
		treeRoot = new DefaultTreeNode("Root", null);
	}

	public DynamicTaxonomyTreeBean(TreeNodeTypeMap typeMap) {
		this.typeMap = typeMap;
	}

	public TreeNode getModel() {
		return this.treeRoot;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}
	
	public boolean getHasSelectedNode() {
		return this.selectedNode != null;
	}
	
    public Category getSelectedCategory() {
		return selectedCategory;
	}

	public void setSelectedCategory(Category selectedCategory) {
		this.selectedCategory = selectedCategory;
	}
    
    public boolean getCategorySelected() {
    	return this.selectedCategory != null;
    }

	public boolean isDisplayNodeHelpText() {
		return displayNodeHelpText;
	}

	public void setDisplayNodeHelpText(boolean displayNodeHelpText) {
		this.displayNodeHelpText = displayNodeHelpText;
	}

	public TreeNodeTypeMap getTypeMap() {
		return typeMap;
	}

	public void setTypeMap(TreeNodeTypeMap typeMap) {
		this.typeMap = typeMap;
	}

	/**
	 * Init the tree structure. Load only the top-level nodes and then
	 * add/remove child tree nodes dynamically based on user input, instead of
	 * loading all of the nodes on instantiation.
	 * 
	 * @param model
	 *            - the data graph with all the pertinent db data.
	 */
	protected void initTree(Taxonomy model) {
		
		treeRoot.setData(model);
		if (treeRoot.getChildren() != null)
			treeRoot.getChildren().clear(); // rebuild it
		if (model.getCategory() == null)
			return;

		Category[] topcats = model.getCategory().getChild();
		for (int i = 0; i < topcats.length; i++) {
			DefaultTreeNode topNode = new DefaultTreeNode(topcats[i], treeRoot);
			DefaultTreeNode dummyNode = new DefaultTreeNode("loading...", topNode);
			// topNode.setType(typeMap.getTreeNodeType(topNode.getLevel()));
		}
	}

	public void onNodeExpand(NodeExpandEvent event) {
		if (!event.getTreeNode().isLeaf() && !event.getTreeNode().isExpanded()) {
			addChildrenNodes(event.getTreeNode());
		}
	}

	public void onNodeCollapse(NodeCollapseEvent event) {
	}

	public void onNodeSelect(NodeSelectEvent event) {
		this.selectedCategory = (Category)event.getTreeNode().getData();
    }

	public void onNodeUnselect(NodeUnselectEvent event) {
		this.selectedCategory = null;
	}

	/**
	 * Method for adding children nodes to a given tree model node.
	 */

	public void addChildrenNodes(TreeNode parentNode) {
		Category parentCat = null;
		if (parentNode.getData() instanceof Category)
			parentCat = (Category) parentNode.getData();
		else if (parentNode.getData() instanceof Taxonomy)
			parentCat = ((Taxonomy) parentNode.getData()).getCategory();
		else
			throw new IllegalStateException(
					"expected either a Catagory or Taxonomy as user-data");

		Category[] childCats = parentCat.getChild();
		if (childCats == null || childCats.length == 0) {
			if (parentNode.getChildren() != null)
			    parentNode.getChildren().clear();
			return;
		}
		if (parentNode.getChildren() != null
				&& parentNode.getChildren().iterator().hasNext()) {
			log.warn("found existing children - clearing and re-adding");
			parentNode.getChildren().clear();
			//return;
		}
		for (Category childCat : childCats) {
			DefaultTreeNode childNode = new DefaultTreeNode(childCat,
					parentNode);
			DefaultTreeNode dummyNode = new DefaultTreeNode("loading...", childNode);
		}

	}

	public CategoryLabelFormat getLabelFormat() {
		return labelFormat;
	}

	public void setLabelFormat(CategoryLabelFormat labelFormat) {
		this.labelFormat = labelFormat;
	}

}
