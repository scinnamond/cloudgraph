package org.cloudgraph.common.key;

 
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;


/**
 * Generates a column key based on the configured CloudGraph column key {@link org.cloudgraph.config.ColumnKeyModel
 * model} for a specific table {@link org.cloudgraph.config.Table configuration}. 
 * @see org.cloudgraph.config.ColumnKeyModel
 * @see org.cloudgraph.config.Table
 */
public interface GraphStatefullColumnKeyFactory {
	
	/**
	 * Generates and returns a column key based on data graph specific state information
	 * within the given data object, such as its UUID, and using the given metadata property
	 * as well as the configured CloudGraph column key {@link org.cloudgraph.config.ColumnKeyModel
     * model} for a specific table {@link org.cloudgraph.config.Table configuration}. 
	 * @param dataObject the data object
	 * @param property the property
	 * @return the column key bytes
	 */
	public byte[] createColumnKey( 
		PlasmaDataObject dataObject, PlasmaProperty property);
	
	
	/**
	 * Generates and returns a column key based on data graph specific sequence number
	 * for a given data object, and using the given metadata property
	 * as well as the configured CloudGraph column key {@link org.cloudgraph.config.ColumnKeyModel
     * model} for a specific table {@link org.cloudgraph.config.Table configuration}. 
	 * @param type the metadata type
	 * @param dataObjectSeqNum the data graph specific sequence number
	 * for a data object
	 * @param property the property
	 * @return the column key bytes
	 */
	public byte[] createColumnKey(PlasmaType type, 
			Long dataObjectSeqNum, PlasmaProperty property);
}
