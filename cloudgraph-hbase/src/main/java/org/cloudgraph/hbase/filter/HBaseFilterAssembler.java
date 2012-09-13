package org.cloudgraph.hbase.filter;

import org.apache.hadoop.hbase.filter.Filter;

/**
 * Common interface for HBase row and column filter assemblers.
 */
public interface HBaseFilterAssembler {

    /**
     * Returns the assembled filter, filter list or filter hierarchy root.
     * @return the assembled filter, filter list or  or filter hierarchy root.
     */
	public Filter getFilter();

}