package org.cloudgraph.web.model.tree;

public interface GraphObjectNode extends LabeledGraphNode {
	public long getGraphAssembleTimeMillis();
	public long getGraphNodeCount();
	public long getGraphDepth();
	public String getGraphTableNames();
	
	public String getTypeName();
	public String getTypeUri();
	public String getBaseTypeNames();

	public String getSourcePropertyName();
	public String getSourcePropertyIsMany();
	public String getSourcePropertyIsReadOnly();
	public String getSourcePropertyTypeName();
	public String getSourcePropertyTypeUri();
}
