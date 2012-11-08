package org.cloudgraph.hbase.io;

import java.util.List;

import commonj.sdo.DataObject;


/**
 * Encapsulates one or more
 * graph table writer components for federation across
 * multiple physical tables and/or physical table rows. 
 * <p>
 * Acts as a container for one or more {@link TableWriter} elements
 * encapsulating a set of component table write operations 
 * for federation across multiple tables, or a single table
 * in the most simple (degenerate) case.  
 * </p>
 * @see org.cloudgraph.hbase.io.GraphTableWriter
 * @see org.cloudgraph.state.GraphTable
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public interface FederatedWriter {
	
	/**
	 * Returns the table writer for the given 
	 * configured table name.
	 * @param tableName the name of the configured table. 
	 * @return the table writer for the given 
	 * configured table name.
	 */
    public TableWriter getTableWriter(String tableName);
    
    
	/**
	 * Adds the given table writer to the container
	 * @param writer the table writer. 
	 */
    public void addTableWriter(TableWriter writer);

	/**
	 * Returns the count of table writers for this 
	 * container.
	 * @return the count of table writers for this 
	 * container
	 */
    public int getTableWriterCount();
    
    /**
     * Returns all table writers for the this container
     * @return all table writers for the this container
     */
    public List<TableWriter> getTableWriters();
    
    /**
     * Returns the table writer associated with the
     * data graph root. 
     * @return the table writer associated with the
     * data graph root.
     */
    public TableWriter getRootTableWriter();

    /**
     * Sets the table writer associated with the
     * data graph root. 
     * @param writer the table writer
     */
    public void setRootTableWriter(TableWriter writer);


    /**
     * Returns the row writer associated with the given data object
     * @param dataObject the data object
     * @return the row writer associated with the given data object
     * @throws IllegalArgumentException if the given data object
     * is not associated with any row writer.
     */
    public RowWriter getRowWriter(DataObject dataObject);
}
