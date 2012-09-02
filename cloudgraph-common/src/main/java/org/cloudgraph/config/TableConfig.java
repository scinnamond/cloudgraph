package org.cloudgraph.config;

import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.CloudGraphConstants;
import org.cloudgraph.config.HTable;
import org.cloudgraph.connect.CloudGraphContext;

/**
 * Encapsulates logic related to access of HTable specific
 * configuration information.  
 */
public class TableConfig {
    private HTable table;
    private Hash hash;
    
    @SuppressWarnings("unused")
	private TableConfig() {}
    
	public TableConfig(HTable table) {
		super();
		this.table = table;
		
	}

	/**
	 * Returns the configuration for the HTable.
	 * @return the configuration for the HTable.
	 */
	public HTable getTable() {
		return table;
	}
	
	/**
	 * Returns the HTable name for this table configuration.
	 * @return the HTable name for this table configuration.
	 */
	public String getName() {
		return this.table.getName();
	}
	
	/**
	 * Returns true if the HTable has a specific hash algorithm 
	 * configured.
	 * @return true if the HTable has a specific hash algorithm 
	 * configured.
	 */
	public boolean hasHashAlgorithm() {
		return this.getTable().getHashAlgorithm() != null;
	}
	
	/**
	 * Returns the specific configured hash algorithm 
	 * configured for an HTable, or if not configured
	 * returns the configured HBase hash algorithm as
	 * configured within HBase using the 'hbase.hash.type'
	 * property.
	 * @return the specific configured hash algorithm 
	 * configured for an HTable.
	 */
	public Hash getHashAlgorithm() {
		if (this.hash== null) {
			if (hasHashAlgorithm()) {
				String hashName = this.getTable().getHashAlgorithm().getName().value();
				this.hash = Hash.getInstance(Hash.parseHashType(hashName));
			}
			else {
			    String algorithm = CloudGraphContext.instance().getConfig().get(
			    		CloudGraphConstants.PROPERTY_HBASE_CONFIG_HASH_TYPE);
			    this.hash = Hash.getInstance(Hash.parseHashType(algorithm));			
			}
		}
		return this.hash;
	}

}