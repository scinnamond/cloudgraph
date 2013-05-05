package org.cloudgraph.web.model.tree;

public interface GraphPropertyNode extends LabeledGraphNode {

	public String getPropertyName();
	public String getPropertyIsMany();
	public String getPropertyIsReadOnly();
	public String getPropertyDataType();
}
