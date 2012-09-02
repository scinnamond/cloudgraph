package org.cloudgraph.hbase.filter;

import org.apache.hadoop.hbase.filter.Filter;

public interface HBaseFilterAssembler {

	public abstract Filter getFilter();

}