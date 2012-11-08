package org.cloudgraph.hbase.io;

import java.util.List;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;

/**
 * Provides access to the operational, configuration and other state information
 * required for write operations on a single graph row.
 * <p>
 * Acts as a single component within a {@link TableWriter} container
 * and encapsulates the HBase client <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Put.html">Put</a> and
 * <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Delete.html">Delete</a> 
 * operations for use in write operations across multiple logical
 * entities within a graph row. 
 * </p>
 * 
 * @see org.cloudgraph.hbase.io.TableWriter
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public interface RowWriter extends RowOperation {
	
	/**
	 * Returns the row put mutation.
	 * @return the row put mutation.
	 */
	public Put getRow();
	
	/**
	 * Returns the row delete mutation.
	 * @return the row delete mutation.
	 */
	public Delete getRowDelete();
	
	/**
	 * Return the write operations for a row. 
	 * @return the write operations for a row.
	 */
	public List<Row> getWriteOperations();

	/**
	 * Returns the container for this writer. 
	 * @return the container for this writer.
	 */
	public TableWriter getTableWriter();
	
	/**
	 * Returns whether the root data object for this writer
	 * is created. 
	 * @return whether the root data object for this writer
	 * is created.
	 */
	public boolean isRootCreated();
	
	/**
	 * Returns whether the root data object for this writer
	 * is deleted. 
	 * @return whether the root data object for this writer
	 * is deleted.
	 */
	public boolean isRootDeleted();
}
