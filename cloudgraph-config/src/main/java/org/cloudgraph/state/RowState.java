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

import org.cloudgraph.store.key.GraphStatefullColumnKeyFactory;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * Provides access to the configuration and state related
 * context information for a specific table row including the
 * management state for the underlying data graph and the
 * composite column key factory used to generated column keys
 * specific to a configured graph. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public interface RowState extends State {
	
	/**
	 * Returns the composite row key bytes for
	 * this row context
	 * @return the composite row key bytes for
	 * this row context
	 */
    public byte[] getRowKey();

	/**
	 * Returns the root data object associated with
	 * the row operation. 
	 * @return the root data object associated with
	 * the row operation. 
	 */
	public DataObject getRootDataObject();	

	/**
	 * Returns the root type associated with
	 * the row operation. 
	 * @return the root type associated with
	 * the row operation. 
	 */
	public Type getRootType();	
	
	/**
	 * Adds the given data object as associated with
	 * the row operation.  
	 * @param dataObject the root data object
	 */
	public void addDataObject(DataObject dataObject);
	
	/**
	 * Returns true if this row operation is associated with the given
	 * data object.
	 * @return true if this row operation is associated with the given
	 * data object.
	 */
	public boolean contains(DataObject dataObject);	
	
	/**
	 * Returns true if this row operation is associated with the given
	 * data object UUID .
	 * @return true if this row operation is associated with the given
	 * data object  UUID  .
	 */
	public boolean contains(java.util.UUID uuid);	

	/**
	 * Returns the data object associated with this row operation based on the given
	 * data object UUID  .
	 * @return the data object associated with this row operation based on the given
	 * data object UUID  .
	 */
	public DataObject getDataObject(java.util.UUID uuid);	
	
	/**
     * Returns the graph state for this row context
     * @return the graph state for this row context
     */
	public GraphState getGraphState() throws IOException;

	/**
	 * Returns the column key factory for this row context
	 * @return the column key factory for this row context
	 */
	public GraphStatefullColumnKeyFactory getColumnKeyFactory() throws IOException;


}
