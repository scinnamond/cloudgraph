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
    private FederatedOperation federatedOperation;

	public GraphTableWriter(TableConfig table) {
		super(table);
	}

	public GraphTableWriter(TableConfig table,
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
}
