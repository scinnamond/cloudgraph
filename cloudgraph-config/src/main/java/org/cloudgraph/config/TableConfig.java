package org.cloudgraph.config;

import java.nio.charset.Charset;

import org.plasma.sdo.core.CoreConstants;

/**
 * Encapsulates logic related to access of HTable specific
 * configuration information.  
 * @author Scott Cinnamond
 * @since 0.5
 */
public class TableConfig {
    private Table table;
    private Charset charset;
    
    @SuppressWarnings("unused")
	private TableConfig() {}
    
	public TableConfig(Table table) {
		super();
		this.table = table;
		this.charset = Charset.forName( CoreConstants.UTF8_ENCODING );
		
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
				this.charset);
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

	public Charset getCharset() {
		return charset;
	}
	

}