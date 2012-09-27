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
	private Object[] data;
    
	public InstanceSpecificationQueueAdapter(InstanceSpecification ins,
			List<PropertyAdapter> properties) {
		super(ins, properties);
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
	}
	
    public Object[] getData() {
        return this.data;
    }    
}
