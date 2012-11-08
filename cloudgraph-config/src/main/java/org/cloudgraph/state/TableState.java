package org.cloudgraph.state;

import org.cloudgraph.config.TableConfig;

/**
 * Provides access to the configuration and state related
 * context information for a specific table. 
 * @see org.cloudgraph.config.TableConfig
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public interface TableState extends State {
	
	/**
	 * Returns the table configuration for this context. 
	 * @return the table configuration for this context.
	 */
	public TableConfig getTable();
	
	
}
