package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;
import java.util.List;

import org.cloudgraph.web.sdo.meta.Classifier;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Slot;
import org.cloudgraph.web.sdo.meta.ValueSpecification;

/**
 * An adapter for any instance which for use in queues/tables
 * where the number of columns is dynamic based
 * on the slots/properties the instance contains at runtime. Constructs
 * a data array based on defined properties rather than slots for
 * an instance which may or may not yet exist.
 * 
 * @author scinnamond
 */
public class InstanceSpecificationQueueAdapter extends InstanceSpecificationAdapter 
implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//private Object[] data;
    
	public InstanceSpecificationQueueAdapter(InstanceSpecification ins,
			List<PropertyAdapter> properties, int level, int maxLevel) {
		super(ins, properties, level, maxLevel);
		/*
		this.data = new Object[properties.size()];
		int i = 0;
		for (PropertyAdapter prop : properties) {
			this.data[i] = "";
			Slot slot = this.getSlot(ins, prop.getId());
			if (slot != null) {
				this.data[i] = this.getValue(ins, slot);
			}
		    i++;
		}
		*/
	}
	
    public Object[] getData() {
    	
    	Object[] result = new Object[this.propertyList.size()];
		int i = 0;
    	for (PropertyAdapter prop : this.propertyList) {
    		Object value = values.get(prop.getName());
			if (value != null) 
				result[i] = value;
			else
	    		result[i] = "";
		    i++;
    	}
    	return result;
        //return this.data;
    }    
}
