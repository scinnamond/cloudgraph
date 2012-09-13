package org.cloudgraph.common.key;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * Generates an HBase row key based on the configured CloudGraph row key {@link org.cloudgraph.config.RowKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.Table configuration}. 
 * <p>
 * The initial creation and subsequent reconstitution for query retrieval
 * purposes of both row and column keys in CloudGraph&#8482; is efficient, 
 * as it leverages byte array level API in both Java and the current 
 * underlying SDO 2.1 implementation, <a target="#" href="http://plasma-sdo.org">PlasmaSDO&#8482;</a>. Both composite row and 
 * column keys are composed in part of structural metadata, and the 
 * lightweight metadata API within <a target="#" href="http://plasma-sdo.org">PlasmaSDO&#8482;</a> contains byte-array level, 
 * cached lookup of all basic metadata elements including logical and 
 * physical type and property names.  
 * </p>
 * @see org.cloudgraph.config.RowKeyModel
 * @see org.cloudgraph.config.Table
 */
public interface GraphRowKeyFactory {
	
	/**
	 * Creates and returns a composite row key capable
	 * of locating the given Data Graph. Implementations are
	 * typically driven by the CloudGraph configuration section 
	 * specified for the given Data Graph URI and Type name.  
	 * @param dataGraph the Data Graph
	 * @return a composite row key capable
	 * of locating the given Data Graph
	 */
	public byte[] createRowKeyBytes(DataGraph dataGraph); 
	
	/**
	 * Generates a row key based on the given root data type
	 * @param type the root type for the target data graph
	 * @return the row key
	 */
	public byte[] createRowKeyBytes(Type type);
		
}
