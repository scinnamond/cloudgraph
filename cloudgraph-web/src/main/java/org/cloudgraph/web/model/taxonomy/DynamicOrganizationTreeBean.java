package org.cloudgraph.web.model.taxonomy;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.tree.DynamicTreeNodeModel;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;
import org.cloudgraph.web.sdo.core.Organization;
import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlTreeNode;
import org.richfaces.component.state.TreeState;
import org.richfaces.event.NodeExpandedEvent;
import org.richfaces.model.TreeRowKey;

public abstract class DynamicOrganizationTreeBean extends ModelBean {

    private static Log log = LogFactory.getLog(DynamicOrganizationTreeBean.class);
    private long ids = 0;
    private DynamicTreeNodeModel treeRoot;
    private TreeState treeState;
    protected TreeNodeTypeMap typeMap;
    protected OrganizationLabelFormat labelFormat;
    private boolean displayNodeHelpText = true;
	protected Set<Integer> ajaxKeys = new HashSet<Integer>();
	
	public DynamicOrganizationTreeBean() {
	}
	
	public DynamicOrganizationTreeBean(TreeNodeTypeMap typeMap) {
		this.typeMap = typeMap;
	}

	public DynamicTreeNodeModel getModel() {
		return this.treeRoot;
	}
	
	public void setTreeState(TreeState treeState) {this.treeState = treeState;}
    public TreeState getTreeState() {return treeState;}
	public Set<Integer> getAjaxKeys() {
		return ajaxKeys;
	}

	public void setAjaxKeys(Set<Integer> ajaxKeys) {
		this.ajaxKeys = ajaxKeys;
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
     * Init the tree structure. Load only the top-level nodes and then add/remove
     * child tree nodes dynamically based on user input, instead of loading all of
     * the nodes on instantiation.
     * @param model - the data graph with all the pertinent db data.
     */
	protected void initTree(List<Organization> model)
	{	

		treeRoot = new DynamicTreeNodeModel(++ids);
		treeRoot.setUserData("ROOT");
		treeRoot.setLevel(0);
		
		Organization[] childOrgs = new Organization[model.size()];
		model.toArray(childOrgs);
		
		for (int i = 0; i < childOrgs.length; i++)
		{
			DynamicTreeNodeModel topNode = new DynamicTreeNodeModel(++ids);
	        topNode.setEnabled(true);
	        topNode.setName(childOrgs[i].getName());
        	if (this.labelFormat == null) 
        		topNode.setLabel(childOrgs[i].getName());
	        else 
	        	topNode.setLabel(
	        			this.labelFormat.getLabel(childOrgs[i]));
        	if (this.displayNodeHelpText)
	            topNode.setTooltip(childOrgs[i].getName());
			topNode.setLevel(treeRoot.getLevel() + 1);
	        topNode.setType(typeMap.getTreeNodeType(topNode.getLevel()));
	        topNode.setUserData(childOrgs[i]);
	        if (log.isDebugEnabled())
	        	log.debug("created top node: " + "(" + topNode.getType() + ") " + topNode.getLabel());

	        if (childOrgs[i].getChildCount() == 0)
		        topNode.setLeaf(true);
	        else
	        	topNode.setLeaf(false);
	        
	        treeRoot.addNode(topNode);
		}
	}
	
	
	/** 
	 * The tree expand expand listener method. 
	 */  
	public void processExpansion(NodeExpandedEvent nodeExpandedEvent){  
		// get the source or the component who fired this event.  
		Object source = nodeExpandedEvent.getSource();  
		if (source instanceof HtmlTreeNode) {  
			// It should be a html tree node, if yes get  
			// the ui tree which contains this node.  
			UITree tree = ((HtmlTreeNode) source).getUITree();  
			// avoid null pointer exceptions even though not needed. but safety first ;-)  
			if (tree == null) {  
				return;  
			}  
			
			// get the row key i.e. id of the given node.  
			Object rowKey = tree.getRowKey(); 
			
			// get node expanded state.
			boolean isExpanded = false;
			if (getTreeState() != null)
				isExpanded = getTreeState().isExpanded((TreeRowKey) rowKey);
			else
				isExpanded = tree.isExpanded();

			// get the model node of this node.  
			DynamicTreeNodeModel selectedTreeModelNode =
				(DynamicTreeNodeModel) tree.getTreeNode(rowKey);
			
			if(null != selectedTreeModelNode){  
				if (isExpanded) {
    				// add the children nodes.  
	    			addChildrenNodes(selectedTreeModelNode);  
				}
				else {
    				// remove the children nodes.  
					Iterator<?> it = selectedTreeModelNode.getChildren();
					while (it.hasNext()) {
						it.next();
						it.remove();
					}
				}
			}  
		}  
	}  

	/** 
	 * Method for adding children nodes to a given tree model node. 
	 */  
	public void addChildrenNodes(DynamicTreeNodeModel parentNode){
		Organization parentOrg = null;
		if (parentNode.getUserData() instanceof Organization)
			parentOrg = (Organization) parentNode.getUserData();
		else
			throw new IllegalStateException("expected an Organization as user-data");
		
		Organization[] childOrgs = parentOrg.getChild();
        if (childOrgs == null || childOrgs.length == 0) 
        	return;
        if (parentNode.getChildren() != null && parentNode.getChildren().hasNext())
        {
            log.warn("found existing children - clearing and re-adding");
            parentNode.getNodes().clear();
        	//return;
        }
        for (Organization childOrg : childOrgs)
        {
        	if (childOrg.getCode().contains("SHARED"))
        		continue; // demo hack
        	
        	DynamicTreeNodeModel childNode = new DynamicTreeNodeModel(++ids);
        	childNode.setEnabled(true);
        	childNode.setName(childOrg.getName());
        	if (this.labelFormat == null) 
	        	childNode.setLabel(childOrg.getName());

	        else 
	        	childNode.setLabel(
	        			this.labelFormat.getLabel(childOrg));
        	if (this.displayNodeHelpText)
        	    childNode.setTooltip(childOrg.getName());
        	childNode.setLevel(parentNode.getLevel() + 2);
        	childNode.setType(typeMap.getTreeNodeType(childNode.getLevel()));
        	childNode.setUserData(childOrg);
        	if (log.isDebugEnabled())
        		log.debug("created child node: " + "(" + childNode.getType() + ") " + childNode.getLabel());

        	if (childOrg.getChildCount() == 0)
        		childNode.setLeaf(true);
        	else
        		childNode.setLeaf(false);

        	parentNode.addNode(childNode);
        }
	}

	public OrganizationLabelFormat getLabelFormat() {
		return labelFormat;
	}

	public void setLabelFormat(OrganizationLabelFormat labelFormat) {
		this.labelFormat = labelFormat;
	}

  

	
}


