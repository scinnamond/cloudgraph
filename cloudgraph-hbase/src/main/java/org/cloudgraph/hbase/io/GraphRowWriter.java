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
package org.cloudgraph.hbase.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.key.GraphStatefullColumnKeyFactory;
import org.cloudgraph.common.service.DuplicateRowException;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.common.service.MissingRowException;
import org.cloudgraph.common.service.ToumbstoneRowException;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.key.StatefullColumnKeyFactory;
import org.cloudgraph.state.GraphRow;
import org.cloudgraph.state.GraphState;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.ChangeSummary;
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

    private static Log log = LogFactory.getLog(GraphRowWriter.class);
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
			this.graphState = createGraphState(this.rowKey, 
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
	 * Returns the existing (or creates a new) row delete mutation.
	 * @return the existing (or creates a new) row delete mutation.
	 */
	@Override
	public Delete getRowDelete() {
		if (this.rowDelete == null) {
			this.rowDelete = new Delete(this.getRowKey());
			this.operations.add(this.rowDelete);
		}
		return this.rowDelete;
	}
	
	/**
	 * Returns whether there is an existing row delete mutation.
	 * @return whether there is an existing row delete mutation.
	 */
	@Override
	public boolean hasRowDelete() {
		return this.rowDelete != null;
	}
	
	/**
	 * Returns a single column value for this row given a context
	 * data object and property. Uses a statefull column key factory
	 * to generate a column key based on the given context data object 
	 * and property.
	 * @param dataObject the context data object
	 * @param property the context property
	 * @return the column value bytes
	 * @throws IOException
	 * 
	 * @see StatefullColumnKeyFactory
	 */
	@Override
	public byte[] fetchColumnValue(PlasmaDataObject dataObject, 
			PlasmaProperty property) throws IOException {
    	byte[] qualifier = this.getColumnKeyFactory().createColumnKey(
    		dataObject, property);
    	
		Get existing = new Get(this.rowKey);
		
		byte[] family = tableWriter.getTable().getDataColumnFamilyNameBytes();
		existing.addColumn(family, qualifier);
		
		Result result = this.getTableWriter().getConnection().get(existing);
		return result.getValue(family, qualifier);
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
	
	/**
     * Initializes a graph state by querying for a row
     * based on the given row key and either creating a new (empty)
     * graph state for an entirely new graph, or otherwise initializing
     * a graph state based on state or state and management columns in
     * the existing returned row.   
     * 
     * @param rowKey the row key
     * @param dataGraph the data graph
     * @param changeSummary the change summary
     * @return the graph state
     * @throws IOException
     * @throws DuplicateRowException for a new graph if a row already exists
     * for the given row key
     * @throws GraphServiceException where except for a new graph, if no row
     * exists for the given row key
     */
    protected GraphState createGraphState(byte[] rowKey, 
    		DataObject dataObject,
    		ChangeSummary changeSummary,
    		TableConfig tableConfig,
    		HTableInterface con) throws IOException
    {
    	GraphState graphState;
		// --ensure row exists unless a new row/graph
		// --use empty get with only necessary "state" management columns
		
		// if entirely new graph for the given 
		// federated or sub-graph root
		if (changeSummary.isCreated(dataObject)) {
			
			if (tableConfig.uniqueChecks()) {
				Result result = getMinimalRow(rowKey, tableConfig, con);
	    		if (!result.isEmpty()) {
	    			if (!result.containsColumn(
	    					tableConfig.getDataColumnFamilyNameBytes(), 
	    					Bytes.toBytes(GraphState.TOUMBSTONE_COLUMN_NAME))) {
	    			    throw new DuplicateRowException("no row for id '"
	    				    + Bytes.toString(rowKey) + "' expected when creating new row for table '"
	    				    + tableConfig.getTable().getName() + "'"); 
	    			}
	    			else {
	    			    throw new ToumbstoneRowException("no toumbstone row for id '"
	        				    + Bytes.toString(rowKey) + "' expected when creating new row for table '"
	        				    + tableConfig.getTable().getName() + "' - cannot overwrite toumbstone row"); 
	    			}
	    		}
			}
    		graphState = new GraphState(
    			this.tableWriter.getFederatedOperation().getMarshallingContext());
        }
		else {
			Result result = getStateRow(rowKey, tableConfig, con);
    		if (result.isEmpty()) {
    			throw new MissingRowException(tableConfig.getTable().getName(),
    				Bytes.toString(rowKey));  
    		}
			if (result.containsColumn(
					tableConfig.getDataColumnFamilyNameBytes(), 
					Bytes.toBytes(GraphState.TOUMBSTONE_COLUMN_NAME))) {
			    throw new ToumbstoneRowException("no row for id '"
    				    + Bytes.toString(rowKey) + "' expected when modifying row for table '"
    				    + tableConfig.getTable().getName() 
    				    + "' - cannot overwrite toumbstone row"); 
			}
    		byte[] state = result.getValue(Bytes.toBytes(tableConfig.getDataColumnFamilyName()), 
    				Bytes.toBytes(GraphState.STATE_COLUMN_NAME));
            if (state != null) {
            	if (log.isDebugEnabled())
            		log.debug(GraphState.STATE_COLUMN_NAME
            			+ ": " + Bytes.toString(state));
            }
            else
    			throw new OperationException("expected column '"
    				+ GraphState.STATE_COLUMN_NAME + " for row " 
    				+ Bytes.toString(rowKey) + "'"); 
            graphState = new GraphState(Bytes.toString(state), 
            		this.tableWriter.getFederatedOperation().getMarshallingContext());
    	}   		
    	return graphState;
    }
    
    private Result getMinimalRow(byte[] rowKey, TableConfig tableConfig, HTableInterface con) throws IOException {
		Get existing = new Get(rowKey);
		existing.addColumn(tableConfig.getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.ROOT_UUID_COLUMN_NAME));
		existing.addColumn(tableConfig.getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.TOUMBSTONE_COLUMN_NAME));		
		return con.get(existing);    	
    }
    
    private Result getStateRow(byte[] rowKey, TableConfig tableConfig, HTableInterface con) throws IOException {
		Get existing = new Get(rowKey);
		existing.addColumn(tableConfig.getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.STATE_COLUMN_NAME));
		existing.addColumn(tableConfig.getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.TOUMBSTONE_COLUMN_NAME));		
		return con.get(existing);    	
    }
}
