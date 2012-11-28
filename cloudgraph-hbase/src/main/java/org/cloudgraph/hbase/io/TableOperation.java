package org.cloudgraph.hbase.io;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.cloudgraph.state.TableState;


/**
 * The the pooled connection and other context for 
 * read or write operations for a specific HBase table.  
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public interface TableOperation extends TableState {
	
	/**
	 * Returns the HBase table pooled connection
	 * for this context. 
	 * @return the HBase table pooled connection
	 * for this context.
	 */
	public HTableInterface getConnection(); 
	
	/**
	 * Returns whether there is an active HBase table pooled connection
	 * for this context. 
	 * @return whether there is an active HBase table pooled connection
	 * for this context.
	 */
	public boolean hasConnection(); 
	
	/**
	 * Returns the federated context associated with this table
	 * operation context. 
	 * @return the federated context associated with this table
	 * operation context. 
	 */
	public FederatedOperation getFederatedOperation();
	
	/**
	 * Sets the federated context associated with this table
	 * operation context. 
	 * @param federatedOperation the operation
	 */
	public void setFederatedOperation(FederatedOperation federatedOperation);
}
