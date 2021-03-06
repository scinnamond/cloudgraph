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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.connect.HBaseConnectionManager;
import org.cloudgraph.state.GraphTable;
//import org.cloudgraph.state.UUID;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.core.CoreDataObject;

import commonj.sdo.DataObject;

/**
 * The operational, configuration and other state information
 * required for read operations on a single graph table. 
 * <p>
 * Acts as a container for one or more {@link RowReader} elements
 * and encapsulates the HBase client <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Get.html">Get</a> 
 * operations for use in read operations across one or more graph rows. 
 * </p>
 * 
 * @see org.cloudgraph.hbase.io.RowReader
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class GraphTableReader extends GraphTable 
    implements TableReader 
{
    private static Log log = LogFactory.getLog(GraphTableReader.class);
    private HTableInterface connection;   
    /** maps data object UUIDs strings and row key strings to row readers */
    private Map<String, RowReader> rowReaderMap = new HashMap<String, RowReader>();
    private DistributedOperation distributedOperation;
    
	public GraphTableReader(TableConfig table, 
			DistributedOperation distributedOperation) {
		super(table);
		this.distributedOperation = distributedOperation;
	}
	
	/**
	 * 
	 * Returns the table name associated with this reader. 
	 * @return the table name associated with this reader. 
	 */
	public String getTableName() {
		return this.getTable().getName();
	}

	@Override
	public HTableInterface getConnection() {
		if (this.connection == null) {
			this.connection = HBaseConnectionManager.instance().getConnection(
					table.getName());
		}
		return this.connection;
	}
	
	/**
	 * Returns whether there is an active HBase table pooled connection
	 * for this context. 
	 * @return whether there is an active HBase table pooled connection
	 * for this context.
	 */
	public boolean hasConnection() {
		return this.connection != null;
	}

	/**
	 * Returns the row reader context for the given data object or null
	 * if null exists
	 * @param dataObject the data object
	 * @return the row reader context for the given data object or null
	 * if null exists
	 */
	@Override
	public RowReader getRowReader(DataObject dataObject) {
        String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		return rowReaderMap.get(uuid);
	}
	
	/**
	 * Returns the row reader context for the given row key or null
	 * if null exists 
	 * @param rowKey the row key bytes
	 * @return the row reader context for the given row key or null
	 * if null exists 
	 */
	@Override
	public RowReader getRowReader(byte[] rowKey) {
		return rowReaderMap.get(Bytes.toString(rowKey));
	}

	/**
	 * Returns the row reader context for the given UUID 
	 * @param uuid the UUID  
	 * @return the row reader context for the given UUID 
	 */
	@Override
	public RowReader getRowReader(UUID uuid) {
		return rowReaderMap.get(uuid.toString());
	}
	
	/**
	 * Adds the given row reader context mapping it to the
	 * given UUID 
	 * @param uuid the UUID  
	 * @param rowReader the row reader context
	 * @throws IllegalArgumentException if an existing row reader is already mapped
	 * for the given data object UUID 
	 */
	@Override
	public void addRowReader(UUID uuid, RowReader rowReader)  throws IllegalArgumentException {
		if (rowReaderMap.get(uuid.toString()) != null)
			throw new IllegalArgumentException("given UUID already mapped to a row reader, "
					+ uuid);
		rowReaderMap.put(uuid.toString(), rowReader);		
	}
	
	/**
	 * Returns all row reader context values for this table context.
	 * @return all row reader context values for this table context.
	 */
	@Override
	public List<RowReader> getAllRowReaders() {
		List<RowReader> result = new ArrayList<RowReader>();
		result.addAll(rowReaderMap.values());
		return result;
	}

	/**
	 * Creates and adds a row reader based on the given
	 * data object and result row mapping it by UUID string
	 * and row key string. 
	 * @param dataObject the data object
	 * @return the row reader
	 * @throws IllegalArgumentException if an existing row reader is already mapped
	 * for the given data object UUID 
	 */
	@Override
	public RowReader createRowReader(DataObject dataObject,
		Result resultRow) throws IllegalArgumentException {
		
        byte[] rowKey = resultRow.getRow();
        String keyString = Bytes.toString(rowKey);
        UUID uuid = ((PlasmaDataObject)dataObject).getUUID();
		if (this.rowReaderMap.containsKey(uuid.toString()))
			throw new IllegalArgumentException("given UUID already mapped to a row reader, "
					+ uuid);        
        if (this.rowReaderMap.containsKey(keyString))
        	throw new IllegalArgumentException("existing row reader is already mapped for the given row key, "
        			+ keyString);
        GraphRowReader rowReader = new GraphRowReader(
        	resultRow.getRow(), resultRow,
        	dataObject, this);
        this.addRowReader(uuid, rowReader);
        this.rowReaderMap.put(keyString, rowReader);
        
        // set the row key so we can look it up on
        // modify and delete ops
    	CoreDataObject coreObject = (CoreDataObject)dataObject;
    	coreObject.getValueObject().put(
        	CloudGraphConstants.ROW_KEY, rowReader.getRowKey());
		
		return rowReader;
	}
	
	/**
	 * Returns the distributed context associated with this table
	 * operation context. 
	 * @return the distributed context associated with this table
	 * operation context. 
	 */
	@Override
	public DistributedOperation getFederatedOperation() {
		return this.distributedOperation;
	}
	
	/**
	 * Sets the distributed context associated with this table
	 * operation context. 
	 * @param distributedOperation the operation
	 */
	@Override
	public void setFederatedOperation(DistributedOperation distributedOperation) {
		this.distributedOperation = distributedOperation;
	}
	
	/**
     * Frees resources associated with this reader and any
     * component readers. 
     */
    public void clear() {
        this.rowReaderMap.clear();
    }

}
