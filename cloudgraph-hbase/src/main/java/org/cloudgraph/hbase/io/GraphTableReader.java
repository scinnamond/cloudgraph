package org.cloudgraph.hbase.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.connect.HBaseConnectionManager;
import org.cloudgraph.state.GraphTable;
import org.plasma.sdo.PlasmaDataObject;

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
    /** maps data object UUIDs to row readers */
    private Map<String, RowReader> rowReaderMap = new HashMap<String, RowReader>();
    private FederatedOperation federatedOperation;
    
	public GraphTableReader(TableConfig table, 
			FederatedOperation federatedOperation) {
		super(table);
		this.federatedOperation = federatedOperation;
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
	 * Returns the row reader context for the given data object
	 * @param dataObject the data object
	 * @return the row reader context for the given data object
	 */
	@Override
	public RowReader getRowReader(DataObject dataObject) {
        String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		return rowReaderMap.get(uuid);
	}

	/**
	 * Returns the row reader context for the given UUID string
	 * @param uuid the UUID string
	 * @return the row reader context for the given UUID string
	 */
	@Override
	public RowReader getRowReader(String uuid) {
		return rowReaderMap.get(uuid);
	}
	
	/**
	 * Adds the given row reader context mapping it to the
	 * given UUID string.
	 * @param uuid the UUID string
	 * @param rowContext the row reader context
	 */
	@Override
	public void addRowReader(String uuid, RowReader rowContext) {
		if (rowReaderMap.get(uuid) != null)
			throw new IllegalArgumentException("given UUID already mapped to a row reader, "
					+ uuid);
		rowReaderMap.put(uuid, rowContext);		
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
	 * data object. If a reader else already mapped for the
	 * given data object, the existing reader is returned. 
	 * @param dataObject
	 * @return the row reader
	 */
	@Override
	public RowReader createRowReader(DataObject dataObject,
		Result resultRow) {
		
        GraphRowReader rowReader = new GraphRowReader(
        	resultRow.getRow(), resultRow,
        	dataObject, this);
        String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
        this.addRowReader(uuid, rowReader);
		
		return rowReader;
	}
	
	/**
	 * Returns the federated context associated with this table
	 * operation context. 
	 * @return the federated context associated with this table
	 * operation context. 
	 */
	@Override
	public FederatedOperation getFederatedOperation() {
		return this.federatedOperation;
	}
	
	/**
	 * Sets the federated context associated with this table
	 * operation context. 
	 * @param federatedOperation the operation
	 */
	@Override
	public void setFederatedOperation(FederatedOperation federatedOperation) {
		this.federatedOperation = federatedOperation;
	}
	
	/**
     * Frees resources associated with this reader and any
     * component readers. 
     */
    public void clear() {
        this.rowReaderMap.clear();
    }
}
