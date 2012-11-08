package org.cloudgraph.state;

import java.io.IOException;

import org.cloudgraph.common.key.GraphStatefullColumnKeyFactory;

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
	 * data object UUID string.
	 * @return true if this row operation is associated with the given
	 * data object  UUID string.
	 */
	public boolean contains(String uuid);	

	/**
	 * Returns the data object associated with this row operation based on the given
	 * data object UUID string.
	 * @return the data object associated with this row operation based on the given
	 * data object UUID string.
	 */
	public DataObject getDataObject(String uuid);	
	
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
