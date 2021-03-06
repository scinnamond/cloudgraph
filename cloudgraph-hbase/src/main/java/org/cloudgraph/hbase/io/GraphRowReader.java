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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.hbase.key.StatefullColumnKeyFactory;
import org.cloudgraph.hbase.service.ColumnMap;
import org.cloudgraph.state.GraphRow;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.store.service.ToumbstoneRowException;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.DataObject;

/**
 * The operational, configuration and other state information
 * required for read operations on a single graph row.
 * <p>
 * Acts as a single component within a {@link TableReader} container
 * and encapsulates the HBase client <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Get.html">Get</a>
 * operation for use in read operations across multiple logical
 * entities within a graph row. 
 * </p>
 * 
 * @see org.cloudgraph.hbase.io.TableReader
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class GraphRowReader extends GraphRow
    implements RowReader {
    private static Log log = LogFactory.getLog(GraphRowReader.class);

    private ColumnMap row;
	private TableReader tableReader;
	
	public GraphRowReader(byte[] rowKey,
			Result result,
			DataObject rootDataObject,
			TableReader tableReader) {
		super(rowKey, rootDataObject);
		this.row = new ColumnMap(result);
		this.tableReader = tableReader;
		byte[] state = this.row.getColumnValue(
				this.tableReader.getTable().getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.STATE_COLUMN_NAME));
        if (state != null) {
        	if (log.isDebugEnabled())
        		log.debug(GraphState.STATE_COLUMN_NAME
        			+ ": " + new String(state));
        }
        else
			throw new OperationException("expected column '"
				+ GraphState.STATE_COLUMN_NAME + "' for row " 
				+ Bytes.toString(rowKey) + "'"); 
		byte[] toumbstone = this.row.getColumnValue(
				this.tableReader.getTable().getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.TOUMBSTONE_COLUMN_NAME));
        if (toumbstone  != null)
        	throw new ToumbstoneRowException("cannot read toumbstone row for root, "
        			+ rootDataObject.toString());
        	
		this.graphState = new GraphState(Bytes.toString(state),
        		this.tableReader.getFederatedOperation().getMarshallingContext());

        this.columnKeyFactory = new StatefullColumnKeyFactory(
        		(PlasmaType)rootDataObject.getType(),
        		graphState);
	}

	@Override
	public ColumnMap getRow() {
		return this.row;
	}
	
	@Override
	public TableReader getTableReader() {
		return this.tableReader;
	}
	
    /**
     * Frees resources associated with this reader. 
     */
    public void clear() {
    	
    }
}
