package org.cloudgraph.web.model.graph;

import java.util.List;

import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.web.model.tree.GraphObjectNode;
import org.cloudgraph.web.model.tree.LabeledGraphNode;
import org.plasma.sdo.core.CoreDataObject;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

class DataObjectNode extends CommonNode implements GraphObjectNode {
	private DataObject dataObject;
	private Property sourceProperty;
	
	public DataObjectNode(DataObject dataObject, Property sourceProperty) {
		super();
		this.dataObject = dataObject;
		this.sourceProperty = sourceProperty;
	}
	public String getName() {
		return getLabel();
	}
	
    public String getLabel() {
    	if (this.sourceProperty != null)
            return this.sourceProperty.getName() + " ("
        	    + this.dataObject.getType().getName()
        	    + ")"; 
    	else 
    		return this.dataObject.getType().getName();  
    }
	
	public DataObject getDataObject() {
		return dataObject;
	}
	
	public void setDataObject(DataObject dataObject) {
		this.dataObject = dataObject;
	}
	
	@Override
	public long getGraphAssembleTimeMillis() {
 		Object object = ((CoreDataObject)this.dataObject).getValueObject().get(
				CloudGraphConstants.GRAPH_ASSEMBLY_TIME);
 		if (object != null) {
 			Long value = (Long)object;
 			return value.longValue();
 		}
		return 0;
	}
	
	@Override
	public long getGraphNodeCount() {
 		Object object = ((CoreDataObject)this.dataObject).getValueObject().get(
				CloudGraphConstants.GRAPH_NODE_COUNT);
 		if (object != null) {
 			Long value = (Long)object;
 			return value.longValue();
 		}
		return 0;
	}
	
	@Override
	public long getGraphDepth() {
 		Object object = ((CoreDataObject)this.dataObject).getValueObject().get(
				CloudGraphConstants.GRAPH_DEPTH);
 		if (object != null) {
 			Long value = (Long)object;
 			return value.longValue();
 		}
		return 0;
	}

	@Override
	public String getGraphTableNames() {
 		Object object = ((CoreDataObject)this.dataObject).getValueObject().get(
				CloudGraphConstants.GRAPH_TABLE_NAMES);
 		if (object != null) {
 			List<String> list = (List<String>)object;
 			StringBuilder buf = new StringBuilder();
 			for (int i = 0; i < list.size(); i++) {
 				if (i > 0)
 					buf.append(", ");
 				buf.append(list.get(i));
 			}
 			return buf.toString();
 		}
		return "";
	}
	
	@Override
	public String getTypeName() {
		return this.dataObject.getType().getName();
	}
	
	@Override
	public String getTypeUri() {
		return this.dataObject.getType().getURI();
	}
	
	@Override
	public String getBaseTypeNames() {
		Type type = this.dataObject.getType();
		if (type.getBaseTypes() != null) {
			StringBuilder buf = new StringBuilder();
		    for (int j = 0; j < type.getBaseTypes().size(); j++) {
		    	if (j > 0)
		    		buf.append(", ");
		    	Type baseType = type.getBaseTypes().get(j);
		    	buf.append(baseType.getName());
		    }
		    return buf.toString();
		}
		return "";
	}	
	
	@Override
	public String getSourcePropertyName() {
		if (this.sourceProperty != null) {
			return this.sourceProperty.getName();
		}
		return "";
	}
	
	@Override
	public String getSourcePropertyIsMany() {
		if (this.sourceProperty != null) {
			return String.valueOf(this.sourceProperty.isMany());
		}
		return "false";
	}
	
	@Override
	public String getSourcePropertyIsReadOnly() {
		if (this.sourceProperty != null) {
			return String.valueOf(this.sourceProperty.isReadOnly());
		}
		return "false";
	}
	
	@Override
	public String getSourcePropertyTypeName() {
		if (this.sourceProperty != null) {
			return this.sourceProperty.getType().getName();
		}
		return "";
	}
	
	@Override
	public String getSourcePropertyTypeUri() {
		if (this.sourceProperty != null) {
			return this.sourceProperty.getType().getURI();
		}
		return "";
	}
}
