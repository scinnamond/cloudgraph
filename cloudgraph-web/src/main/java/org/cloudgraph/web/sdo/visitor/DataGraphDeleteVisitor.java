package org.cloudgraph.web.sdo.visitor;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.helper.PlasmaDataFactory;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

/**
 * Removes any links to reference data we don't want to attempt
 * to delete by calling SDO 'unset'. After traversal is complete
 * clients should call delete() on the root object
 * @author scinnamond
 */
public class DataGraphDeleteVisitor implements PlasmaDataGraphVisitor {
    private static Log log = LogFactory.getLog(DataGraphDeleteVisitor.class);
	
	private Type[] referenceTypes;
	
	public DataGraphDeleteVisitor() {
		this.referenceTypes = new Type[0];
	}
	
	public DataGraphDeleteVisitor(Type[] referenceTypes) {
		this.referenceTypes = referenceTypes;
	}
	
	public void visit(DataObject target, DataObject source,
			String sourceKey, int level) {
		
		PlasmaDataObject targetObject = (PlasmaDataObject)target;
		
		// process the root and exit
		if (source == null) {
			DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
			dataGraph.getChangeSummary().beginLogging(); // log changes from this point
	            	
			if (log.isDebugEnabled())
				log.debug("deleting root object "
					+ targetObject.getType().getURI() + "#" + targetObject.getType().getName()
					+ " (" + targetObject.getUUIDAsString() + ")");
			
	    	return;
		}
		PlasmaDataObject sourceObject = (PlasmaDataObject)source;
    	Property sourceProperty = sourceObject.getType().getProperty(sourceKey);
		if (isReferenceType(targetObject.getType())) {
			if (log.isDebugEnabled())
				log.debug("unsetting property "
					+ targetObject.getType().getURI() + "#" + targetObject.getType().getName()
					+ "." + sourceProperty.getName());
			sourceObject.unset(sourceProperty);
			//sourceObject.getValueObject().remove(sourceProperty.getName());
		}
	}
	
	private boolean isReferenceType(Type type) {
		boolean found = false;
		for (Type t : referenceTypes)
            if (t.getName().equals(type.getName()) && 
            	t.getURI().equals(type.getURI())) {	
            	found = true;
            	break;
            }	
		return found;
	}
	


}
