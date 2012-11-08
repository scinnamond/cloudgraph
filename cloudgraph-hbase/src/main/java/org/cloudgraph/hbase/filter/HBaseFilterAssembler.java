package org.cloudgraph.hbase.filter;

import org.apache.hadoop.hbase.filter.Filter;
import org.cloudgraph.common.filter.FilterAssembler;

/**
 * Common interface for HBase row and column filter assemblers.
 * <p>
 * HBase filters may be collected into 
 * lists using <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.html" target="#">FilterList</a>
 * each with a 
 * <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ALL" target="#">MUST_PASS_ALL</a> or <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ONE" target="#">MUST_PASS_ONE</a>
 *  (logical) operator. Lists may then be assembled into hierarchies 
 * used to represent complex expression trees filtering either rows
 * or columns in HBase.
 * </p> 
 * @author Scott Cinnamond
 * @since 0.5
 */
public interface HBaseFilterAssembler extends FilterAssembler {

    /**
     * Returns the assembled filter, filter list or filter hierarchy root.
     * @return the assembled filter, filter list or  or filter hierarchy root.
     */
	public Filter getFilter();
	
}