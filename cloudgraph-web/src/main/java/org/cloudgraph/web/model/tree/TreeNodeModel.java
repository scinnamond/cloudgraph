package org.cloudgraph.web.model.tree;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.OperationName;
import org.primefaces.model.TreeNode;


@SuppressWarnings({ "serial", "unchecked" })
public class TreeNodeModel implements TreeNode {
	
    private static Log log = LogFactory.getLog(TreeNodeModel.class);

    private Object id;
    private String name;
    private String label;
    private String tooltip;
    private SortedMap<Object, TreeNode> nodes;
    private TreeNode parent;
    private String type = DefaultTreeNodeType.node.toString();
    private boolean enabled = true;
    private boolean selected = false;
    private String action;
    private OperationName operation;
    private Object userData;

    @SuppressWarnings("unused")
	private TreeNodeModel(Comparator comparator) {
    	if (comparator != null)
    	    nodes = new TreeMap<Object, TreeNode>(comparator);
    	else
            nodes = new TreeMap<Object, TreeNode>();	
    }
    
    public TreeNodeModel(Object id, Comparator comparator) {
    	this(comparator);
        this.id = String.valueOf(id);
    }

    public TreeNodeModel(Object id) {
    	this(null);
        this.id = String.valueOf(id);
    }
    
    public Map<Object, TreeNode> getNodes() {
        return this.nodes;
    }

    public void addNode(TreeNodeModel node) {
        addChild(node.getId(), node);
        node.setParent(this);
    }

	public void addChild(Object id, TreeNode child) {
        getNodes().put(String.valueOf(id), child);
    }

    public TreeNode getChild(Object id) {
        return (TreeNode) getNodes().get(String.valueOf(id));
    }

    //public Iterator getChildren() {
    //    return getNodes().entrySet().iterator();
    //}

    public Object getData() {
        return this; // RichFaces rendering seems to desperately need this
    }

    public TreeNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return getNodes().isEmpty();
    }

    public void removeChild(Object id) {
        getNodes().remove(String.valueOf(id));
    }

    public void setData(Object data) {
    	// RichFaces rendering seems to desperately need this 
    }

    public void setParent(TreeNode parent) {
    	this.parent = parent;
    }
    
    public Object getId() {
        return id;
    }
    
    public Object getUserData() {
    	return this.userData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
    
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	
    public void setUserData(Object userData) {
    	this.userData = userData;
    }

	public String onAction()
    {
		this.selected = true;
		if (log.isDebugEnabled())
            log.debug("onAction: " + String.valueOf(this.action));	
        
        return null; //this.action; // don't return the action lest we cause a refresh
    }

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public void clearSelection()
	{
		this.selected = false;
		//Iterator<TreeNode> iter = this.getChildren();
		//while (iter.hasNext())
		//	((TreeNodeModel)iter.next()).clearSelection();
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public OperationName getOperation() {
		return operation;
	}

	public void setOperation(OperationName operation) {
		this.operation = operation;
	}

	@Override
	public boolean isExpanded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setExpanded(boolean expanded) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getChildCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSelectable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSelectable(boolean selectable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<TreeNode> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}
	
	   
}