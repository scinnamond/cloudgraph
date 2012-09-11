package org.cloudgraph.hbase.key;

import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.plasma.sdo.PlasmaType;

/**
 * A key factory which helps implementations leverage 
 * table and data graph specific configuration information,
 * such as the hashing algorithm and field level row and column
 * model settings. 
 * @see org.cloudgraph.config.CloudGraphConfig
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.config.DataGraphConfig
*/
public interface ConfigurableKeyFactory {
	public TableConfig getTable();
	public DataGraphConfig getGraph();
	public PlasmaType getRootType();
}
