package org.cloudgraph.hbase.scan;

import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaType;

/**
 * Assembles a composite partial row (start/stop) key pair where each
 * field within the composite start and stop row keys are constructed 
 * based a set of query predicates.   
 * @author Scott Cinnamond
 * @since 0.5
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
    
	/**
     * Assemble row key scan information based only on the
     * data graph root type information such as the URI
     * and type logical or physical name. 
     */
	public void assemble();
	
	
	/**
	 * Assemble row key scan information based on the given
	 * scan literals.
	 * @param literals the scan literals
	 */
	public void assemble(ScanLiterals literals);
}
