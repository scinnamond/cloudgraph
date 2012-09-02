package org.cloudgraph.common.key;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * Generates an HBase row key based on the configured Cloudgraph row key {@link org.cloudgraph.config.RowKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.HTable configuration}. 
 * @see org.cloudgraph.config.RowKeyModel
 * @see org.cloudgraph.config.HTable
 */
public interface CloudGraphRowKeyFactory {
	
	/**
	 * Creates and returns a composite row key capable
	 * of locating the given Data Graph. Implementations are
	 * typically driven by the Cloudgraph configuration section 
	 * specified for the given Data Graph URI and Type name.  
	 * @param dataGraph the Data Graph
	 * @return a composite row key capable
	 * of locating the given Data Graph
	 */
	//public String createRowKey(DataGraph dataGraph); 
	public byte[] createRowKeyBytes(DataGraph dataGraph); 
	
	/**
	 * Generates a row key based on the given root data type
	 * @param type the root type for the target data graph
	 * @return the row key
	 */
	//public String createRowKey(Type type);
	public byte[] createRowKeyBytes(Type type);
		
}
