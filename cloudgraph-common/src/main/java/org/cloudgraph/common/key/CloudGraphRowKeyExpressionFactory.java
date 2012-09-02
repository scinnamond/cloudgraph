package org.cloudgraph.common.key;

import java.util.List;

import commonj.sdo.Type;

/**
 * Generates an HBase row key based on the configured HGraph row key {@link org.cloudgraph.config.RowKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.HTable configuration}. 
 * @see org.cloudgraph.config.RowKeyModel
 * @see org.cloudgraph.config.HTable
 */
public interface CloudGraphRowKeyExpressionFactory {
	

	
	/**
	 * Generates a regular expression matching a row key based on 
	 * the given list of token value pairs.
	 * @param type the root type for the target data graph
	 * @param values the token value list
	 * @return the row key expression
	 */
	public String createRowKeyExpr(List<TokenValue> values);
	public byte[] createRowKeyExprBytes(List<TokenValue> values);
	
	/**
	 * Returns true if the data graph configured for the given 
	 * {@link commonj.sdo.Type type} has a user defined token which
	 * maps to the given property path. 
	 * @param type the SDO type
	 * @param path the property path
	 * @return true if the data graph configured for the given 
	 * {@link commonj.sdo.Type type} has a user defined token which
	 * maps to the given property path. 
	 */
    public boolean hasUserDefinedRowKeyToken(Type type, String path);
}
