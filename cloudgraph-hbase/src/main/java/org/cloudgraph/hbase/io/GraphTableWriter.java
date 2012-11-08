package org.cloudgraph.hbase.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.connect.HBaseConnectionManager;
import org.cloudgraph.state.GraphTable;

/**
 * The operational, configuration and other state information
 * required for write operations on a single graph table. 
 * <p>
 * Acts as a container for one or more {@link RowWriter} elements
 * and encapsulates the HBase client <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Put.html">Put</a> 
 * and <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Delete.html">Delete</a>
 * operations for use in write operations across one or more graph rows within a
 * table. 
 * </p>
 * 
 * @see org.cloudgraph.hbase.io.RowWriter
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class GraphTableWriter extends GraphTable 
    implements TableWriter 
{
    private HTableInterface connection;
    /** maps data object UUIDs to row writers */
    private Map<String, RowWriter> rowContextMap = new HashMap<String, RowWriter>();


	public GraphTableWriter(TableConfig table) {
		super(table);
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
	@Override
	public RowWriter getRowWriter(String uuid) {
		return rowContextMap.get(uuid);
	}

	@Override
	public void addRowWriter(String uuid, RowWriter rowContext) {
		rowContextMap.put(uuid, rowContext);		
	}

	@Override
	public List<RowWriter> getAllRowWriters() {
		List<RowWriter> result = new ArrayList<RowWriter>();
		result.addAll(rowContextMap.values());
		return result;
	}

}
