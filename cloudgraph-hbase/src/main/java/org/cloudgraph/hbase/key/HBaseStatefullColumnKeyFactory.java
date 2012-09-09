package org.cloudgraph.hbase.key;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.key.GraphColumnKeyFactory;
import org.cloudgraph.common.key.GraphStatefullColumnKeyFactory;
import org.cloudgraph.common.service.GraphState;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;


/**
 * Generates an HBase column key based on the configured Cloudgraph column key {@link org.cloudgraph.config.ColumnKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.Table configuration}. 
 * <p>
 * In order to persist any arbitrary Data Graph in a single HBase row, 
 * and since every column key in a row must be unique in HBase, every column 
 * name must be "overridden" to uniquely identify
 * each field. Not only must the column keys for fields within a single entity, 
 * be unique, but every column key for every field where any 
 * number of the same entity type are persisted within the 
 * same data graph. E.g. Profile->Person->Contact->Address where a
 * person has multiple contacts (home, business, etc...)
 * </p>
 * <p>
 * Though each Data Object has a <a href="http://docs.oracle.com/javase/6/docs/api/java/util/UUID.html" target="#">UUID</a>, a unique numeric sequence ID 
 * is used as being far more efficient in terms of length, and also
 * allowing multiple rows to "line-up" in a columnar fashion within an 
 * HBase table. This organization is helpful when viewing an HBase table
 * columns in a relational mapping of spreadsheet based tool for
 * debugging or analysis.     
 * </p>
 * @see org.cloudgraph.config.ColumnKeyModel
 * @see org.cloudgraph.config.Table
 * @see org.cloudgraph.common.service.GraphState
 */
public class HBaseStatefullColumnKeyFactory extends HBaseCompositeColumnKeyFactory 
    implements GraphStatefullColumnKeyFactory 
{
	private static final Log log = LogFactory.getLog(HBaseStatefullColumnKeyFactory.class);
	private GraphState graphState;
		
	public HBaseStatefullColumnKeyFactory(PlasmaType rootType,
			GraphState graphState) {
		super(rootType);
	    this.graphState = graphState;
	}
	
	@Override
	public byte[] createColumnKey( 
			PlasmaDataObject dataObject, PlasmaProperty property)	
	{
 	    PlasmaType type = (PlasmaType)dataObject.getType();

		Long seqNum = this.graphState.createSequence(dataObject);
		return getKey(type, seqNum, property);
	}

	@Override
	public byte[] createColumnKey(PlasmaType type, 
			Long dataObjectSeqNum, PlasmaProperty property)	
	{
		return getKey(type, dataObjectSeqNum, property);
	}
	
	private byte[] getKey(PlasmaType type, 
			Long dataObjectSeqNum, PlasmaProperty property) 
	{
		// Use the bytes of the sequence number 
		// String representation for column names so we can read
		// the column names in third party tools. 
		byte[] seqNumBytes = Bytes.toBytes(String.valueOf(dataObjectSeqNum));
		byte[] sectionDelim = graph.getColumnKeySectionDelimiterBytes();	    	    
		byte[] prefix = super.createColumnKey(type, property);
		
		int len = prefix.length + sectionDelim.length + seqNumBytes.length;
		byte[] result = new byte[len];
		
		int destPos = 0;
		System.arraycopy(prefix, 0, result, destPos, prefix.length);
		
		destPos += prefix.length;
		System.arraycopy(sectionDelim, 0, result, destPos, sectionDelim.length);
		
		destPos += sectionDelim.length;
		System.arraycopy(seqNumBytes, 0, result, destPos, seqNumBytes.length);
 		
		if (log.isDebugEnabled())
			log.debug("key: " + Bytes.toString(result));
		
		return result;
		
	}
}
