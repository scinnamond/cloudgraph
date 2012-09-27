package org.cloudgraph.web.model.taxonomy;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.tree.TreeNodeModel;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaNode;

import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.categorization.Taxonomy;

import commonj.sdo.DataObject;

public class TaxonomyTreeAssembler implements PlasmaDataGraphVisitor 
{
    private static Log log = LogFactory.getLog(TaxonomyTreeAssembler.class);
    
	private long ids = 0;
	private TreeNodeModel treeRoot;
	private Stack<TreeNodeModel> stack = new Stack<TreeNodeModel>();
	private int currentLevel = -1;	
	private TreeNodeTypeMap typeMap;
	
	public TaxonomyTreeAssembler() {
		this.typeMap = new DefaultTreeNodeTypeMap();
	}
	
	public TaxonomyTreeAssembler(TreeNodeTypeMap typeMap) {
		this.typeMap = typeMap;
	}
	
	public TreeNodeModel getTreeRoot() {
		return treeRoot;
	}

	public void visit(DataObject target, DataObject source,
			String sourceKey, int level) {
		
		
		TreeNodeModel node = null;
		if (source == null) { 
			node = new TreeNodeModel(++ids);
			node.setUserData(target);
			treeRoot = node;
			stack.push(node);
		}
		else {			
	        node = new TreeNodeModel(++ids);
	        node.setEnabled(true);
	        node.setName(((Category)target).getName());
	        node.setLabel(((Category)target).getName());
	        node.setTooltip(((Category)target).getDefinition());
	        node.setType(this.typeMap.getTreeNodeType(level));
	        node.setUserData(target);
	        if (log.isDebugEnabled())
	        	log.debug("created node: " + "(" + node.getType() + ") " + node.getLabel());
		}
		
		if (source != null) {
		
			if (level > currentLevel) {
	            stack.peek().addNode(node);	
				stack.push(node); 				
			}
			else if (level <= currentLevel) { // pop till we find the parent
				String sourceHashKey = getSourceHashKey(source);
				while (stack.size() > 0) {
					PlasmaNode top = (PlasmaNode)stack.peek().getUserData();	
					if (!top.getUUIDAsString().equals(sourceHashKey))
					    stack.pop();
					else
						break;
				}
	            stack.peek().addNode(node);	
	            stack.push(node);
			}
		}
		
		currentLevel = level;
	}
	
    private String getSourceHashKey(DataObject source) {
    	if (source instanceof Category) {
    		return ((PlasmaDataObject)source).getUUIDAsString();
    	}
    	else if (source instanceof Taxonomy) {
    		return ((PlasmaDataObject)source).getUUIDAsString();
    	}
    	else
    		throw new IllegalArgumentException("unexpected instance class, "
    				+ source.getClass().getName());
    }
		
}		

