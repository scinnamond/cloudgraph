/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.state;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cloudgraph.common.key.GraphStatefullColumnKeyFactory;
import org.plasma.sdo.PlasmaDataObject;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * Encapsulates the configuration and state related
 * context information for a specific table row including the
 * management state for the underlying data graph and the
 * composite column key factory used to generated column keys
 * specific to a configured graph.
 *  
 * @see org.cloudgraph.config.DataGraphConfig
 * @author Scott Cinnamond
 * @since 0.5.1
 * 
 * @see GraphStatefullColumnKeyFactory
 */
public class GraphRow implements RowState {

	protected byte[] rowKey;
	protected GraphState graphState;
	protected GraphStatefullColumnKeyFactory columnKeyFactory;
	protected DataObject rootDataObject;
	private Map<String, DataObject> dataObjectMap = new HashMap<String, DataObject>();
    
	@SuppressWarnings("unused")
	private GraphRow() {}
	
	public GraphRow(byte[] rowKey, DataObject rootDataObject) {
		this.rowKey = rowKey;
		this.rootDataObject = rootDataObject;
	    String uuid = ((PlasmaDataObject)rootDataObject).getUUIDAsString();
		this.dataObjectMap.put(uuid, rootDataObject);
	}
	
	@Override
	public byte[] getRowKey() {
		return this.rowKey;
	}

	public GraphState getGraphState() throws IOException {
		return graphState;
	}

	public GraphStatefullColumnKeyFactory getColumnKeyFactory() throws IOException {
		return columnKeyFactory;
	}

	/**
	 * Returns the root data object associated with
	 * the row operation. 
	 * @return the root data object associated with
	 * the row operation. 
	 */
	@Override
	public DataObject getRootDataObject() {
		return this.rootDataObject;
	}
	
	/**
	 * Returns the root type associated with
	 * the row operation. 
	 * @return the root type associated with
	 * the row operation. 
	 */
	public Type getRootType() {
		return this.rootDataObject.getType();
	}
	
	/**
	 * Adds the given data object as associated with
	 * the row operation.  
	 * @param dataObject the root data object
	 * @throws IllegalArgumentException if the given data
	 * object is already mapped
	 */
	@Override
	public void addDataObject(DataObject dataObject) {
		 String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		 if (this.dataObjectMap.get(uuid) != null) {
			 throw new IllegalArgumentException("data object ("
				+ uuid + ") of type "
				+ dataObject.getType().getURI() + "#" + dataObject.getType().getName()
				+ " already added"); 
		 }
		 this.dataObjectMap.put(uuid, dataObject);		
	}

	/**
	 * Returns true if this row operation is associated with the given
	 * data object.
	 * @return true if this row operation is associated with the given
	 * data object.
	 */
	@Override
	public boolean contains(DataObject dataObject) {
		 String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		return this.dataObjectMap.containsKey(uuid);
	}
	
	/**
	 * Returns true if this row operation is associated with the given
	 * data object UUID  .
	 * @return true if this row operation is associated with the given
	 * data object  UUID  .
	 */
	public boolean contains(java.util.UUID uuid) {
		return this.dataObjectMap.containsKey(uuid.toString());
	}
	
	/**
	 * Returns the data object associated with this row operation based on the given
	 * data object UUID  .
	 * @return the data object associated with this row operation based on the given
	 * data object UUID  .
	 */
	public DataObject getDataObject(java.util.UUID uuid) {
		DataObject result = this.dataObjectMap.get(uuid.toString());		
		if (result == null) 
			 throw new IllegalArgumentException("data object ("
				+ uuid + ") not found"); 
	    return result;
	}
	
}	
