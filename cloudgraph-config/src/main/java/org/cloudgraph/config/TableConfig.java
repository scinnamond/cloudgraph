package org.cloudgraph.config;

import java.nio.charset.Charset;

import org.cloudgraph.config.Table;
import org.plasma.sdo.core.CoreConstants;

/**
 * Encapsulates logic related to access of HTable specific
 * configuration information.  
 */
public class TableConfig {
    private Table table;
    
    @SuppressWarnings("unused")
	private TableConfig() {}
    
	public TableConfig(Table table) {
		super();
		this.table = table;
		
	}

	/**
	 * Returns the configuration for the HTable.
	 * @return the configuration for the HTable.
	 */
	public Table getTable() {
		return table;
	}
	
	/**
	 * Returns the table name for this table configuration.
	 * @return the table name for this table configuration.
	 */
	public String getName() {
		return this.table.getName();
	}
	
	public String getDataColumnFamilyName() {
		return this.table.getDataColumnFamilyName();
	}
	
	public byte[] getDataColumnFamilyNameBytes() {
		return this.table.getDataColumnFamilyName().getBytes(
			Charset.forName( CoreConstants.UTF8_ENCODING ));
	}	
	
	/**
	 * Returns true if the table has a specific hash algorithm 
	 * configured.
	 * @return true if the table has a specific hash algorithm 
	 * configured.
	 */
	public boolean hasHashAlgorithm() {
		return this.getTable().getHashAlgorithm() != null;
	}
	

}