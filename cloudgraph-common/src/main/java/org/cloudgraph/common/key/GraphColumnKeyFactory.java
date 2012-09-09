package org.cloudgraph.common.key;

 
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;


/**
 * Generates a column key based on the configured CloudGraph column key {@link org.cloudgraph.config.ColumnKeyModel
 * model} for a specific table {@link org.cloudgraph.config.Table configuration}. 
 * @see org.cloudgraph.config.ColumnKeyModel
 * @see org.cloudgraph.config.Table
 */
public interface GraphColumnKeyFactory {
	
	/**
	 * Generates and returns a column key based on the given metadata type and property
	 * as well as the configured CloudGraph column key {@link org.cloudgraph.config.ColumnKeyModel
     * model} for a specific table {@link org.cloudgraph.config.Table configuration}. 
	 * @param type the metadata type
	 * @param property the property
	 * @return the column key bytes
	 */
	public byte[] createColumnKey( 
	    PlasmaType type, PlasmaProperty property);

}
