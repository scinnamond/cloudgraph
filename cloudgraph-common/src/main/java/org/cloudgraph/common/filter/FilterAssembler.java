package org.cloudgraph.common.filter;

/**
 * Common interface for row and column filter assemblers.
 * @author Scott Cinnamond
 * @since 0.5
 */
public interface FilterAssembler {
	
	/**
	 * Frees associated resources 
	 */
    public void clear();
}
