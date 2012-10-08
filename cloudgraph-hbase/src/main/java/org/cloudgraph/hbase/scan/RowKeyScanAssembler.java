package org.cloudgraph.hbase.scan;

import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaType;

/**
 * Assembles a composite partial row (start/stop) key scan where each
 * field within the composite start and stop row keys are constructed 
 * based a set of query predicates.   
 */
public interface RowKeyScanAssembler extends PartialRowKeyScan {
    /**
     * Assemble row key scan information based on one or more
     * given query predicates.
     * @param where the where predicate hierarchy
     * @param contextType the context type which may be the root type or another
     * type linked by one or more relations to the root
     */
	public void assemble(Where where, PlasmaType contextType);
}
