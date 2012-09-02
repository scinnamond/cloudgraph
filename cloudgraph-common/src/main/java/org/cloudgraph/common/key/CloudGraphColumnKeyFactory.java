package org.cloudgraph.common.key;

 
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;


/**
 * Generates an HBase column key based on the configured HGraph column key {@link org.cloudgraph.config.ColumnKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.HTable configuration}. 
 * @see org.cloudgraph.config.ColumnKeyModel
 * @see org.cloudgraph.config.HTable
 */
public interface CloudGraphColumnKeyFactory {
	
	
	public byte[] createColumnKey( 
	    PlasmaType type, PlasmaProperty property);

	public byte[] createColumnKey( 
		PlasmaDataObject dataObject, PlasmaProperty property);
}
