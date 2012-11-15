package org.cloudgraph.hbase.io;

import org.cloudgraph.state.StateMarshallingContext;


/**
 * Encapsulates one or more
 * graph table operation components for federation across
 * multiple physical tables and/or physical table rows. 
 * @see org.cloudgraph.hbase.io.GraphTableReader
 * @see org.cloudgraph.hbase.io.GraphTableWriter
 * @see org.cloudgraph.state.GraphTable
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public interface FederatedOperation {
	
	/**
	 * Returns true if only one table operation exists
	 * with only one associated (root) type for this
	 * operation. 
	 * <p>
	 * Note: because there may be a single root 
	 * type within an operational context, this does
	 * not mean there is no federation involved. A type
	 * is a metadata entity, and there may be many (data object)
	 * instances of the type within a graph operation. 
     * </p>
	 * @return true if only one table operation exists
	 * with only one associated (root) type for this
	 * operation. 
	 */
    public boolean hasSingleRootType();
    
    /**
     * Returns the marshalling context for this operation.
     * @return the marshalling context for this operation
     */
    public StateMarshallingContext getMarshallingContext();
}
