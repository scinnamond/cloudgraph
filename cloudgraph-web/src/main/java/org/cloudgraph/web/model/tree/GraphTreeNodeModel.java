package org.cloudgraph.web.model.tree;


@SuppressWarnings("serial")
public class GraphTreeNodeModel extends DynamicTreeNodeModel {
	
	private boolean isRoot = false;

	public GraphTreeNodeModel(Object id) {
		super(id);
	}

	public boolean getIsRoot() {
		return isRoot;
	}

	public void setIsRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	public long getGraphAssembleTimeMillis() {
		if (this.getUserData() != null && this.getUserData() instanceof GraphObjectNode) {
			GraphObjectNode node = (GraphObjectNode)this.getUserData();
			return node.getGraphAssembleTimeMillis();
		}
		else
			return 0;
	}
	
	public String getGraphTableNames() {
		if (this.getUserData() != null && this.getUserData() instanceof GraphObjectNode) {
			GraphObjectNode node = (GraphObjectNode)this.getUserData();
			return node.getGraphTableNames();
		}
		else
			return "";
	}

	public long getGraphNodeCount() {
		if (this.getUserData() != null && this.getUserData() instanceof GraphObjectNode) {
			GraphObjectNode node = (GraphObjectNode)this.getUserData();
			return node.getGraphNodeCount();
		}
		else
			return 0;
	}


	public long getGraphDepth() {
		if (this.getUserData() != null && this.getUserData() instanceof GraphObjectNode) {
			GraphObjectNode node = (GraphObjectNode)this.getUserData();
			return node.getGraphDepth();
		}
		else
			return 0;
	}

	public String getTypeName() {
		if (this.getUserData() != null) {
		    if (this.getUserData() instanceof GraphObjectNode) {
			    GraphObjectNode node = (GraphObjectNode)this.getUserData();
			    return node.getTypeName();
		    }
		    else if (this.getUserData() instanceof GraphPropertyNode) {
		    	GraphPropertyNode node = (GraphPropertyNode)this.getUserData();
			    return node.getPropertyDataType();
		    }
		}
		return "";
	}

	public String getTypeUri() {
		if (this.getUserData() != null && this.getUserData() instanceof GraphObjectNode) {
			GraphObjectNode node = (GraphObjectNode)this.getUserData();
			return node.getTypeUri();
		}
		else
			return "";
	}

	public String getBaseTypeNames() {
		if (this.getUserData() != null && this.getUserData() instanceof GraphObjectNode) {
			GraphObjectNode node = (GraphObjectNode)this.getUserData();
			return node.getBaseTypeNames();
		}
		else
			return "";
	}

	public String getPropertyName() {
		if (this.getUserData() != null) {
		    if (this.getUserData() instanceof GraphObjectNode) {
			    GraphObjectNode node = (GraphObjectNode)this.getUserData();
			    return node.getSourcePropertyName();
		    }
		    else if (this.getUserData() instanceof GraphPropertyNode) {
		    	GraphPropertyNode node = (GraphPropertyNode)this.getUserData();
			    return node.getPropertyName();
		    }
		}
		return "";
	}

	public String getPropertyIsMany() {
		if (this.getUserData() != null) {
		    if (this.getUserData() instanceof GraphObjectNode) {
			    GraphObjectNode node = (GraphObjectNode)this.getUserData();
			    return node.getSourcePropertyIsMany();
		    }
		    else if (this.getUserData() instanceof GraphPropertyNode) {
		    	GraphPropertyNode node = (GraphPropertyNode)this.getUserData();
			    return node.getPropertyIsMany();
		    }
		}
		return "";
	}

	public String getPropertyIsReadOnly() {
		if (this.getUserData() != null) {
		    if (this.getUserData() instanceof GraphObjectNode) {
			    GraphObjectNode node = (GraphObjectNode)this.getUserData();
			    return node.getSourcePropertyIsReadOnly();
		    }
		    else if (this.getUserData() instanceof GraphPropertyNode) {
		    	GraphPropertyNode node = (GraphPropertyNode)this.getUserData();
			    return node.getPropertyIsReadOnly();
		    }
		}
		return "";
	}

	public String getPropertyDataType() {
		if (this.getUserData() != null) {
		    if (this.getUserData() instanceof GraphObjectNode) {
			    GraphObjectNode node = (GraphObjectNode)this.getUserData();
			    return node.getSourcePropertyTypeName();
		    }
		    else if (this.getUserData() instanceof GraphPropertyNode) {
		    	GraphPropertyNode node = (GraphPropertyNode)this.getUserData();
			    return node.getPropertyDataType();
		    }
		}
		return "";
	}

    
}
