package org.cloudgraph.web.model.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.tree.DynamicTreeNodeModel;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.profile.KeyType;
import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlTreeNode;
import org.richfaces.component.state.TreeState;
import org.richfaces.event.NodeExpandedEvent;
import org.richfaces.model.TreeRowKey;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;

public abstract class DynamicGraphTreeBean extends ModelBean {

    private static Log log = LogFactory.getLog(DynamicGraphTreeBean.class);
    private long ids = 0;
    private DynamicTreeNodeModel treeRoot;
    private TreeState treeState;
    protected TreeNodeTypeMap typeMap;
    protected NodeLabelFormat labelFormat;
    private boolean displayNodeHelpText = true;
	protected Set<Integer> ajaxKeys = new HashSet<Integer>();
	
	public DynamicGraphTreeBean() {
	}
	
	public DynamicGraphTreeBean(TreeNodeTypeMap typeMap) {
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
	protected void initTree(DataGraph[] graphs)
	{	
		treeRoot = new DynamicTreeNodeModel(++ids);
		treeRoot.setUserData("root");
		treeRoot.setLevel(0);
		
		for (int i = 0; i < graphs.length; i++)
		{
			DataObject topObject = graphs[i].getRootObject();
			DataObjectNode objectNode = new DataObjectNode(topObject, null);
			DynamicTreeNodeModel topNode = new DynamicTreeNodeModel(++ids);
	        topNode.setEnabled(true);
	        topNode.setName(topObject.getType().getName());
        	if (this.labelFormat == null) 
        		topNode.setLabel(objectNode.getLabel());
	        else 
	        	topNode.setLabel(
	        			this.labelFormat.getLabel(objectNode));
        	if (this.displayNodeHelpText)
	            topNode.setTooltip(((PlasmaType)topObject.getType()).getName());
			topNode.setLevel(treeRoot.getLevel() + 1);
	        topNode.setType(typeMap.getTreeNodeType(topNode.getLevel()));
	        topNode.setUserData(objectNode);
	        if (log.isDebugEnabled())
	        	log.debug("created top node: " + "(" + topNode.getType() + ") " + topNode.getLabel());

	        if (this.getChildCount(topObject) == 0)
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
		DataObject parentDataObject = null;
		if (parentNode.getUserData() instanceof DataObjectNode) {
			DataObjectNode dataObjectNode = (DataObjectNode)parentNode.getUserData();
			parentDataObject = dataObjectNode.getDataObject();
		}
		else
			throw new IllegalStateException("expected DataObjectNode as user-data");
		
		CommonNode[] childObjects = this.getChild(parentDataObject);
        if (childObjects == null || childObjects.length == 0) 
        	return;
        if (parentNode.getChildren() != null && parentNode.getChildren().hasNext())
        {
            log.warn("found existing children - clearing and re-adding");
            parentNode.getNodes().clear();
        	//return;
        }
        for (CommonNode childObject : childObjects)
        {
        	DynamicTreeNodeModel childNode = new DynamicTreeNodeModel(++ids);
        	childNode.setEnabled(true);
        	childNode.setName(childObject.getName());
        	if (this.labelFormat == null) 
	        	childNode.setLabel(childObject.getLabel());

	        else 
	        	childNode.setLabel(
	        			this.labelFormat.getLabel(childObject));
        	if (this.displayNodeHelpText)
        	    childNode.setTooltip(childObject.getLabel());
        	childNode.setLevel(parentNode.getLevel() + 2);
        	childNode.setType(typeMap.getTreeNodeType(childNode.getLevel()));
        	childNode.setUserData(childObject);
        	if (log.isDebugEnabled())
        		log.debug("created child node: " + "(" + childNode.getType() + ") " + childNode.getLabel());

        	if (childObject instanceof DataObjectNode) {
        		DataObjectNode objectNode = (DataObjectNode)childObject;
        	    if (this.getChildCount(objectNode.getDataObject()) == 0)
        		    childNode.setLeaf(true);
        	    else
        		    childNode.setLeaf(false);
        	}
        	else
        		childNode.setLeaf(true);

        	parentNode.addNode(childNode);
        }
	}

	private CommonNode[] getChild(DataObject parent) {
		List<CommonNode> list = new ArrayList<CommonNode>();
		for (Property prop : parent.getType().getProperties()) {
			if (prop.getType().isDataType())
			{
				PlasmaProperty p = (PlasmaProperty)prop;
				if (p.isKey(KeyType.primary))
					continue; // boring
				if (parent.isSet(prop))
				    list.add(new DataPropertyNode(prop, parent));				 
			}
			else {
				if (prop.isMany()) {
					List<DataObject> childList = parent.getList(prop);
					if (childList != null) {
						for (DataObject child : childList)
					        list.add(new DataObjectNode(child, prop));
					}
				}
				else {
					DataObject child = parent.getDataObject(prop);
					if (child != null)
						list.add(new DataObjectNode(child, prop));
				}
			}
		}
		CommonNode[] result = new CommonNode[list.size()];
		list.toArray(result);
		return result;
	}
	
	private int getChildCount(DataObject parent) {
		int count = 0;
		for (Property prop : parent.getType().getProperties()) {
			if (prop.getType().isDataType()) {
				PlasmaProperty p = (PlasmaProperty)prop;
				if (p.isKey(KeyType.primary))
					continue; // boring
				if (parent.isSet(prop))
					count++;
			}
			else {
				if (prop.isMany()) {
					List list = parent.getList(prop);
					if (list != null)
						count += list.size();
				}
				else {
					DataObject child = parent.getDataObject(prop);
					if (child != null)
						count++;
				}
			}
		}
		return count;
	}

	public NodeLabelFormat getLabelFormat() {
		return labelFormat;
	}

	public void setLabelFormat(NodeLabelFormat labelFormat) {
		this.labelFormat = labelFormat;
	}
	
}


