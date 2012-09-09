package org.cloudgraph.hbase.service;

import org.apache.hadoop.hbase.client.Result;
import org.plasma.sdo.access.DataGraphAssembler;

/**
 * Constructs a data graph starting with a given root SDO type based on 
 * a given HBase client <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Result.html">result</a> row
 * the entire graph being found in the result row or potentially requiring additional scans depending on the
 * assembler implementation.
 * <p>
 * Since every column key in HBase must be unique, and a data graph
 * may contain any number of nodes, a column key factory is used both 
 * to persist as well as re-constitute a graph. A minimal amount of
 * "state" information is therefore stored with each graph which maps
 * user readable sequence numbers (which are used in column keys) to
 * UUID values. The nodes of the resulting data graph are re-created with
 * the original UUID values.       
 * </p>
 * @see org.cloudgraph.hbase.key.HBaseStatefullColumnKeyFactory
 * @see org.plasma.sdo.PlasmaDataGraph
 * @see org.apache.hadoop.hbase.client.Result
 */
public interface HBaseGraphAssembler extends DataGraphAssembler{
	/**
     * Re-constitutes a data graph from the given HBase client
     * result (row). 
	 * @param resultRow the HBase client
     * result (row).
	 */
	public void assemble(Result resultRow);

}