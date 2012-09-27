package org.cloudgraph.web.sdo.visitor;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;

/**
 * Sets the given data graph
 * @author scinnamond
 */
public class DataGraphReparentVisitor implements PlasmaDataGraphVisitor {
    private static Log log = LogFactory.getLog(DataGraphReparentVisitor.class);
	
	private DataGraph newParent;
	private List<PlasmaDataObject> list = new ArrayList<PlasmaDataObject>();
	
	@SuppressWarnings("unused")
	private DataGraphReparentVisitor() {
	}
	
	public DataGraphReparentVisitor(DataGraph newParent) {
		this.newParent = newParent;
	}
	
	public void visit(DataObject target, DataObject source,
			String sourceKey, int level) {
		
		PlasmaDataObject targetObject = (PlasmaDataObject)target;
		list.add(targetObject);
	}
	
	public void removeParent() {
		for (PlasmaDataObject dataObject : list) {
			dataObject.setDataGraph(null);
		}
	}

	public DataGraph getResultGraph() {
		for (PlasmaDataObject dataObject : list) {
			dataObject.setDataGraph(this.newParent);
		}
		return this.newParent;
	}


}
