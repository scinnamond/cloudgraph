package org.cloudgraph.hbase.filter;

import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaType;

/**
 * Assembles an HBase predicate filter hierarchy based on one or more
 * query predicates. 
 * <p>
 * HBase filters may be collected into 
 * lists using <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.html" target="#">FilterList</a>
 * each with a 
 * <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ALL" target="#">MUST_PASS_ALL</a> or <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ONE" target="#">MUST_PASS_ONE</a>
 *  (logical) operator. Lists may then be assembled into hierarchies 
 * used to represent complex expression trees filtering either rows
 * or columns in HBase.
 * </p> 
 */
public interface PredicateFilterAssembler extends HBaseFilterAssembler {
    /**
     * Assembles a predicate filter hierarchy based on one of more
     * given query predicates.
     * @param where the where clause
     * @param contextType the context type which may be the root type or another
     * type linked by one of more relations to the root
     */
	public void assemble(Where where, PlasmaType contextType);
}
