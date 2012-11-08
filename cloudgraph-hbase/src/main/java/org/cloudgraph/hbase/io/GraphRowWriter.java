package org.cloudgraph.hbase.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.cloudgraph.common.key.GraphStatefullColumnKeyFactory;
import org.cloudgraph.hbase.key.StatefullColumnKeyFactory;
import org.cloudgraph.state.GraphRow;
import org.cloudgraph.state.GraphState;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.DataObject;

/**
 * The operational, configuration and other state information
 * required for write operations on a single graph row.
 * <p>
 * Acts as a single component within a {@link TableWriter} container
 * and encapsulates the HBase client <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Put.html">Put</a> and
 * <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Put.html">Put</a> 
 * operations for use in write operations across multiple logical
 * entities within a graph row. 
 * </p>
 * 
 * @see org.cloudgraph.hbase.io.TableWriter
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class GraphRowWriter extends GraphRow
    implements RowWriter {

	private TableWriter tableWriter;
    private Put row;
    private Delete rowDelete;
    private List<Row> operations = new ArrayList<Row>();

    public GraphRowWriter(byte[] rowKey,
    	DataObject rootDataObject,
    	TableWriter tableWriter) {
		super(rowKey, rootDataObject);
		this.tableWriter = tableWriter;
		this.row = new Put(rowKey);
		this.operations.add(this.row);
	}
    
    @Override
	public GraphState getGraphState() throws IOException {
		if (this.graphState == null) {
			OperationHelper helper = new OperationHelper();
			this.graphState = helper.createGraphState(this.rowKey, 
				this.rootDataObject, 
				this.rootDataObject.getDataGraph().getChangeSummary(), 
				this.tableWriter.getTable(), 
				this.tableWriter.getConnection());
		}
		return this.graphState;
	}

    @Override
	public GraphStatefullColumnKeyFactory getColumnKeyFactory() throws IOException  {
    	if (this.columnKeyFactory == null) {
    		this.columnKeyFactory = new StatefullColumnKeyFactory(
    			(PlasmaType)this.rootDataObject.getType(), 
    			this.getGraphState());
    	}
		return this.columnKeyFactory;
	}

	@Override
	public Put getRow() {
		return this.row;
	}

	/**
	 * Returns the row delete mutation.
	 * @return the row delete mutation.
	 */
	public Delete getRowDelete() {
		if (this.rowDelete == null) {
			this.rowDelete = new Delete(this.getRowKey());
			this.operations.add(this.rowDelete);
		}
		return this.rowDelete;
	}	
	
	@Override
	public TableWriter getTableWriter() {
		return this.tableWriter;
	}
	
	/**
	 * Returns whether the root data object for this writer
	 * is created. 
	 * @return whether the root data object for this writer
	 * is created.
	 */
	@Override
	public boolean isRootCreated() {
		return this.rootDataObject.getDataGraph().getChangeSummary().isCreated(
				this.rootDataObject);
	}
	
	/**
	 * Returns whether the root data object for this writer
	 * is deleted. 
	 * @return whether the root data object for this writer
	 * is deleted.
	 */
	@Override
	public boolean isRootDeleted() {
		return this.rootDataObject.getDataGraph().getChangeSummary().isDeleted(
				this.rootDataObject);
	}

	@Override
	public List<Row> getWriteOperations() {
		return this.operations;
	}
}
