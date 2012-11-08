package org.cloudgraph.hbase.io;

import org.cloudgraph.hbase.service.ColumnMap;


/**
 * Provides access to the operational, configuration and other state information
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
public interface RowReader extends RowOperation {
	public ColumnMap getRow();
	public TableReader getTableReader();

    /**
     * Frees resources associated with this. 
     */
    public void clear();
}
