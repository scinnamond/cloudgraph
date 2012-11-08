package org.cloudgraph.hbase.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;


/**
 * Encapsulates one or more
 * graph table writer components for federation across
 * multiple physical tables and/or physical table rows. 
 * Maps physical configured
 * table names to respective table writers. In most usage
 * scenarios, a "root" table writer is typically added 
 * initially, then other writers are incrementally added
 * as association target types are detected and found configured as
 * graph roots within another distinct table context. 
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
public class FederatedGraphWriter implements FederatedWriter {

	private TableWriter rootWriter;
	private Map<String, TableWriter> tableWriterMap = new HashMap<String, TableWriter>();
	private Map<DataObject, RowWriter> rowWriterMap;
	
	@SuppressWarnings("unused")
	private FederatedGraphWriter() {}
	
	public FederatedGraphWriter(DataGraph dataGraph) {
		TableWriterCollector collector = 
			new TableWriterCollector(dataGraph);
		this.rootWriter = collector.getRootTableWriter();
		for (TableOperation oper : collector.getTableWriters()) {
			tableWriterMap.put(oper.getTable().getName(), 
					(TableWriter)oper);
		}
		this.rowWriterMap = collector.getRowWriterMap();
	}	
	
	/**
	 * Returns the table writer for the given 
	 * configured table name, or null of not exists.
	 * @param tableName the name of the configured table. 
	 * @return the table writer for the given 
	 * configured table name, or null of not exists.
	 */
	@Override
	public TableWriter getTableWriter(String tableName) {
		return this.tableWriterMap.get(tableName);
	}

	/**
	 * Adds the given table writer to the container
	 * @param writer the table writer. 
	 */
	@Override
	public void addTableWriter(TableWriter writer) {
		String name = writer.getTable().getName();
		if (this.tableWriterMap.get(name) != null)
			throw new OperationException("table writer for '" + 
				name + "' already exists");
		this.tableWriterMap.put(name, writer);
	}

	/**
	 * Returns the count of table writers for this 
	 * container.
	 * @return the count of table writers for this 
	 * container
	 */
	@Override
	public int getTableWriterCount() {
		return this.tableWriterMap.size();
	}

    /**
     * Returns all table writers for the this container
     * @return all table writers for the this container
     */
    public List<TableWriter> getTableWriters() {
    	List<TableWriter> result = new ArrayList<TableWriter>();
    	result.addAll(this.tableWriterMap.values());
    	return result;
    }
    
    /**
     * Returns the table writer associated with the
     * data graph root. 
     * @return the table writer associated with the
     * data graph root.
     */
    public TableWriter getRootTableWriter() {
    	return this.rootWriter;
    }

    /**
     * Sets the table writer associated with the
     * data graph root. 
     * @param writer the table writer
     */
    public void setRootTableWriter(TableWriter writer) {
    	this.rootWriter = writer;
    	this.tableWriterMap.put(rootWriter.getTable().getName(), rootWriter);
    }
    
    
    /**
     * Returns the row writer associated with the given data object
     * @param dataObject the data object
     * @return the row writer associated with the given data object
     * @throws IllegalArgumentException if the given data object
     * is not associated with any row writer.
     */
    public RowWriter getRowWriter(DataObject dataObject) {
    	RowWriter result = rowWriterMap.get(dataObject);
    	if (result == null)
    		throw new IllegalArgumentException("the given data object of type "
    		   + dataObject.getType().getURI() + "#" + dataObject.getType().getName()		
    	       + " is not associated with any row writer");
    	return result;
    }

}