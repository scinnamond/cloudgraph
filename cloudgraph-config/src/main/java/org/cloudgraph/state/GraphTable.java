package org.cloudgraph.state;

import org.cloudgraph.config.TableConfig;

/**
 * Encapsulates the configuration and state related
 * context information for a specific table. 
 * @see org.cloudgraph.config.TableConfig
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public abstract class GraphTable implements TableState {
    protected TableConfig table;

    @SuppressWarnings("unused")
	private GraphTable() {}
    public GraphTable(TableConfig table) 
    {
    	if (table == null)
    		throw new IllegalArgumentException("unexpected null value for 'table'");
    	this.table = table;
    }
    
	@Override
	public TableConfig getTable() {
		return this.table;
	}
	
}
